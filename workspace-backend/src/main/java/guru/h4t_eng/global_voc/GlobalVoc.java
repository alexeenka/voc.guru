package guru.h4t_eng.global_voc;

import org.apache.commons.lang3.StringUtils;

import java.util.*;

import static guru.h4t_eng.util.FormDataUtil.formatText;

/**
 * Singleton object.
 *
 * Contains all words with values.
 *
 * Created by aalexeenka on 12.01.2017.
 */
public class GlobalVoc {
    private static GlobalVoc instance = new GlobalVoc();
    private GlobalVoc() {
    }

    public static GlobalVoc getInstance() {
        return instance;
    }

    private List<String> words = new ArrayList<>();
    private Map<String, LinkedList<GlobalVocWord>> wordValues = new HashMap<>();
    private Map<UUID, GlobalVocAuthor> authors = new HashMap<>();

    public void newValues(HashMap<String, LinkedList<GlobalVocWord>> pWordValues, HashMap<UUID, GlobalVocAuthor> pAuthors) {
        final List<String> vWords = new ArrayList<>(pWordValues.keySet());
        Collections.sort(vWords);

        synchronized (GlobalVoc.class) {
            this.words = Collections.unmodifiableList(vWords);
            this.authors = Collections.unmodifiableMap(pAuthors);
            this.wordValues = Collections.unmodifiableMap(pWordValues);
        }
    }

    public List<String> getWords() {
        return words;
    }

    public int getSize() {
        return words.size();
    }

    public LinkedList<GlobalVocWord> getWordValues(String word) {
        return wordValues.get(word);
    }

    public Map<UUID, GlobalVocAuthor> getAuthors() {
        return authors;
    }


    public final static int MAX_FIND_WORD = 10;

    /**
     * Binary search for part of word.
     *
     * Return sorted result.
     *
     * @param findPart findPart
     *
     * @return index of match word
     */
    public List<String> findWords(String findPart) {
        if (StringUtils.isBlank(findPart)) return new ArrayList<>();
        findPart = formatText(findPart);
        int findPartLength = findPart.length();

        int low = 0;
        int high = words.size()-1;

        int resultIndex = -1;
        while (low <= high) {
            int mid = (low + high) / 2;
            String midVal = words.get(mid);

            String iPart = midVal.substring(0, Math.min(findPartLength, midVal.length()));
            int cmp = iPart.compareTo(findPart);

            if (cmp < 0) {
                low = mid + 1;
            } else if (cmp > 0) {
                high = mid - 1;
            } else {
                resultIndex = mid; // key found
                break;
            }
        }

        if (resultIndex == -1) {
            return new ArrayList<>();
        }

        List<String> bR = new ArrayList<>();
        List<String> fR = new ArrayList<>();
        bR.add(words.get(resultIndex));

        int backward = resultIndex - 1;
        int forward = resultIndex + 1;
        while (backward >= 0 || forward < words.size()) {
            if (backward >= 0) {
                final String backwardWord = words.get(backward);
                if (backwardWord.length() < findPartLength) {
                    backward = -1;
                } else {
                    final String wordPart = backwardWord.substring(0, findPartLength);
                    if (findPart.equals(wordPart)) {
                        bR.add(backwardWord);
                        if ((bR.size() + fR.size()) == MAX_FIND_WORD) break;
                        backward--;
                    } else {
                        backward = -1;
                    }
                }
            }
            if (forward < words.size()) {
                final String forwardWord = words.get(forward);
                if (forwardWord.length() < findPartLength) {
                    forward = words.size();
                } else {
                    final String wordPart = forwardWord.substring(0, findPartLength);
                    if (findPart.equals(wordPart)) {
                        fR.add(forwardWord);
                        if ((bR.size() + fR.size()) == MAX_FIND_WORD) break;
                        forward++;
                    } else {
                        forward = words.size();
                    }
                }
            }
        }
        // Merge two list
        Collections.reverse(bR);
        bR.addAll(fR);
        return bR;
    }

}
