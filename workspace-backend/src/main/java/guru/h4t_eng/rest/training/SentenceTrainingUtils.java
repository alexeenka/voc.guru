package guru.h4t_eng.rest.training;

import guru.h4t_eng.logs.AppLoggerFactory;
import guru.h4t_eng.model.training.EngSenTrainingExt;
import guru.h4t_eng.model.training.TrainingWord;
import guru.h4t_eng.util.training.WordFormUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;

import static org.apache.commons.lang3.StringUtils.containsIgnoreCase;

/**
 * SentenceTrainingUtils.
 *
 * Created by aalexeenka on 23.08.2016.
 */
public final class SentenceTrainingUtils {

    private static final Logger LOG = AppLoggerFactory.getH4TLog(SentenceTrainingUtils.class);

    public static final String REPLACE_STR = "[...]";

    public static final String SPEAK_PART = ",word,";

    private SentenceTrainingUtils(){
    }

    private final static Pattern SPLIT_PATTERN = Pattern.compile("[\\p{Punct}[“][”]\\s&&[^-]]+");

    public static EngSenTrainingExt createSenEngExt(TrainingWord trainingWord, UUID debugUserId) {
        final List<String> engSentences = trainingWord.getEngSentences();
        int index = ThreadLocalRandom.current().nextInt(0, engSentences.size());

        String sentence = engSentences.get(index);
        final String[] trainingPair = SentenceTrainingUtils.makeTrainingPair(trainingWord.getEngVal(), sentence);

        if (trainingPair == null) {
            final String msg = "Incorrect sentence for word: " + trainingWord.getEngVal() + ", userid: [" + debugUserId + "]";
            LOG.error(msg);
            throw new RuntimeException(msg);
        }
        // Sometimes in senteces are exist: Unicode Character 'ZERO WIDTH SPACE' (U+200B): http://www.fileformat.info/info/unicode/char/200b/index.htm
        trainingPair[1] = StringUtils.strip(trainingPair[1].trim());
        trainingPair[1] = StringUtils.strip(trainingPair[1], "\u200B");

        return new EngSenTrainingExt(sentence, trainingPair[0], trainingPair[0].replace(REPLACE_STR, SPEAK_PART), trainingPair[1]);
    }

    public static String[] makeTrainingPair(final String engVal, final String sentence) {
        // invalid cases
        if (
                StringUtils.isBlank(engVal) ||
                        StringUtils.isBlank(sentence) ||
                        sentence.equalsIgnoreCase(engVal) || sentence.length() < engVal.length()
                ) return null;

        // try straightforward approach, find the whole phrase
        {
            final String resultSentence = straightForwardApproach(engVal, sentence);
            if (resultSentence != null) return new String[] {resultSentence, engVal.toLowerCase()};
        }

        // for idioms
        {
            final String[] result = findIdiom(engVal, sentence);
            if (result != null) return result;
        }


        // more profound approach, find the word form
        final String[] sentencePart = SPLIT_PATTERN.split(sentence);
        List<String> searchFor = formSearchFor(engVal);

        int index = -1;
        for (String engValKey : searchFor) {
            // ignore words with length less or equal to 2
            if (engValKey.length() <= 2) break;
            index = findIndex(engValKey, sentencePart);
            // word is found
            if (index != -1) break;
        }

        if (index == -1) {
            return null;
        }

        String neededWord = sentencePart[index];
        int replaceIndex = StringUtils.indexOfIgnoreCase(sentence, neededWord);
        String resultSentence = produceQSentence(sentence, neededWord, replaceIndex);

        return new String[] {resultSentence, neededWord};

    }

    public static String[] findIdiom(String engVal, String sentence) {
        final String engValInsensitive = engVal.toLowerCase();
        if (engValInsensitive.startsWith("to be ")) {
            engVal = engVal.substring("to be ".length());
        } else if (engValInsensitive.startsWith("to have ")) {
            engVal = engVal.substring("to have ".length());
        }

        final String[] words = SPLIT_PATTERN.split(engVal);
        try {
            return findIdiom(new StringBuilder(), -1, words, null, sentence, 1);
        } catch (TooManyIterationException th) {
            LOG.error("TooManyIterationException: Idioms: engVal [" + engVal + "], sentence [" + sentence + "]");
        }
        return null;
    }

    private static class TooManyIterationException extends RuntimeException {
    }

    private static String[] findIdiom(StringBuilder str, int index, String[] parts, String currentWordForm, String sentence, int itNumber) {
        if (itNumber > 1000) {
            throw new TooManyIterationException();
        }

        if (currentWordForm != null) {
            str.append(currentWordForm);
            str.append(" ");
        }

        if (index + 1 == parts.length) {
            // System.out.println(str.toString());

            final String search = str.toString().trim();
            final String resultSentence = straightForwardApproach(search, sentence);
            if (resultSentence != null) return new String[] {resultSentence, search.toLowerCase()};

            str.delete(str.length() - currentWordForm.length() - 1, str.length());

            return null;
        }


        String[] wordForms;
        {
            String[] tWordForms = WordFormUtils.findWordForms(parts[index + 1]);
            if (tWordForms == null) {
                wordForms = new String[]{parts[index + 1]};
            } else {
                wordForms = new String[tWordForms.length + 1];
                wordForms[0] = parts[index + 1];

                System.arraycopy(tWordForms, 0, wordForms, 1, tWordForms.length);
            }
        }

        for (int i = 0; i < wordForms.length; i++) {
            final String[] result = findIdiom(str, index + 1, parts, wordForms[i], sentence, itNumber * wordForms.length);
            if (result != null) {
                return result;
            }
        }

        if (currentWordForm != null) {
            str.delete(str.length() - currentWordForm.length() - 1, str.length());
        }

        return null;
    }

    private static String straightForwardApproach(final String engVal, final String original) {
        String upperEngVal = engVal.toUpperCase();
        String iOriginal = original;
        do {
            String upperSentence = iOriginal.toUpperCase();
            final int index = upperSentence.indexOf(upperEngVal);

            if (index == -1 || !isWord(iOriginal, engVal, index)) break;

            iOriginal = produceQSentence(iOriginal, upperEngVal, index);

        } while (true);

        if (iOriginal.equals(original)) {
            return null;
        } else {
            return iOriginal;
        }
    }

    private static boolean isWord(final String original, final String word, int index) {
        // beginning of string
        if (index == 0) {
            final String ch = Character.toString(original.charAt(index + word.length()));
            return SPLIT_PATTERN.split(ch).length == 0;
        }
        // end of string
        if (index + word.length() >= original.length()) {
            final String ch = Character.toString(original.charAt(index - 1));
            return SPLIT_PATTERN.split(ch).length == 0;
        }
        // middle
        {
            final String beginCh= Character.toString(original.charAt(index - 1));
            final String endCh = Character.toString(original.charAt(index + word.length()));
            return SPLIT_PATTERN.split(beginCh).length == 0 && SPLIT_PATTERN.split(endCh).length == 0;
        }
    }

    private static String produceQSentence(String sentence, String neededWord, int replaceIndex) {
        return sentence.substring(0, replaceIndex)
                    + REPLACE_STR
                    + sentence.substring(replaceIndex + neededWord.length(), sentence.length());
    }

    private static List<String> formSearchFor(String engVal) {
        final String[] words = engVal.split(" ");
        ArrayList<String> searchFor = new ArrayList<>(Arrays.asList(words));

        // add irregular verbs if exist
        {
            ArrayList<String> irregular = new ArrayList<>();
            for (String iWord : searchFor) {
                final String[] irregularVerbForms = WordFormUtils.findWordForms(iWord);
                if (irregularVerbForms != null) {
                    irregular.addAll(Arrays.asList(irregularVerbForms));
                }
            }
            searchFor.addAll(irregular);
        }

        // sort by length
        Collections.sort(searchFor, (o1, o2) -> Integer.valueOf(o2.length()).compareTo(o1.length()));

        return searchFor;
    }

    private static int findIndex(String engValKey, String[] sentencePart) {
        while (engValKey.length() >= 3)
        {
            for (int i = 0, N = sentencePart.length; i < N; i++) {
                if (sentencePart[i].length() < engValKey.length()) {
                    continue;
                }

                if (containsIgnoreCase(sentencePart[i], engValKey)) {
                    return i;
                }
            }

            engValKey = engValKey.substring(0, engValKey.length() - 1);
        }

        return -1;
    }

}
