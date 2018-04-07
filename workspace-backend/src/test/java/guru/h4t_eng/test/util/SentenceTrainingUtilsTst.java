package guru.h4t_eng.test.util;

import com.datastax.driver.core.Row;
import guru.h4t_eng.datasource.CassandraDataSource;
import guru.h4t_eng.test.WithLogging;
import guru.h4t_eng.util.training.IrregularVerbUtils;
import guru.h4t_eng.util.training.WordFormUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static guru.h4t_eng.rest.training.SentenceTrainingUtils.makeTrainingPair;

/**
 * SentenceTrainingUtilsTst.
 * <p>
 * Created by aalexeenka on 23.08.2016.
 */
public class SentenceTrainingUtilsTst extends WithLogging {
    private static CassandraDataSource mds = CassandraDataSource.getInstance();

    @Test
    public void checkAllUsersHaveCorrectSentences() {
        final List<Row> rows = mds.runQuery("select user_id, last_name from user", false).all();
        for (Row row : rows) {
            final UUID user_id = row.getUUID("user_id");
            final List<String> errors = produceSentences(user_id, false);
            //noinspection unchecked
            Assert.assertEquals("UserId: " + row.getUUID("user_id") + ", LastName: " + row.getString("last_name") + ", Errors: " + StringUtils.join(errors), 0, errors.size());
        }
    }

    public static void main(String args[]) {
        // performance tests
        final int count = 10000;
        for (int j=0; j<4; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < count; i++) {
                final String[] trainingPair = makeTrainingPair(
                        "Do smb a service", "You've done me a service - thank you"
                );
                if (trainingPair == null) throw new RuntimeException();
            }
            System.out.println("j: " + j + ", Records: " + count + ", total-time: " + (System.currentTimeMillis() - start) + "ms, Per Query: " + (((double) (System.currentTimeMillis() - start)) / count));
        }

        System.exit(0);
        // angular.element(document.body).injector().get('speechSynthesisService').sayEngText("We had to, word, joan down, even though her proposal was okay")
    }

    private static List<String> produceSentences(final UUID userId, boolean printSentences) {
        List<String> errors = new ArrayList<>();
        final List<Row> rows = mds.runQuery("select user_id, eng_val, eng_sntnc_0, eng_sntnc_1, eng_sntnc_2, eng_sntnc_3, " +
                "eng_sntnc_4  from word3 where user_id = ?", false, userId).all();
        int index = 0;
        for (Row row : rows) {
            final UUID uuid = row.getUUID("user_id");
            final String engVal = row.getString("eng_val");

            final List<String> sentences = new ArrayList<>();
            sentences.addAll(row.getList("eng_sntnc_0", String.class));
            sentences.addAll(row.getList("eng_sntnc_1", String.class));
            sentences.addAll(row.getList("eng_sntnc_2", String.class));
            sentences.addAll(row.getList("eng_sntnc_3", String.class));
            sentences.addAll(row.getList("eng_sntnc_4", String.class));

            for (String sentence : sentences) {
                final String[] trainingPair = makeTrainingPair(engVal, sentence);
                if (trainingPair == null) {
                    errors.add("Error: " + "USERID: " + uuid + " EngVal: " + engVal + " Sentence: " + sentence);
                } else {
                    if (printSentences) {
                        System.out.println();
                        System.out.println();
                        System.out.println();
                        System.out.println("#" + index + ": $begin$ For word: [" + engVal + "] and sentence: [" + sentence + "], we have: ");
                        System.out.println("Answer must be: [" + trainingPair[1] + "] for sentence: " + trainingPair[0]);
                        System.out.println("$end$");
                    }
                    index++;
                }
            }
        }
        return errors;
    }

    @Test
    public void test1_valid() {
        String engVal = "Adversity";
        String sentence = "We don't develop courage by being happy every day. we develop it by surviving difficult times and challenging adversity.";

        final String[] trainingPair = makeTrainingPair(engVal, sentence);
        Assert.assertNotNull(trainingPair);

        Assert.assertEquals("We don't develop courage by being happy every day. we develop it by surviving difficult times and challenging [...].", trainingPair[0]);
        Assert.assertEquals("adversity", trainingPair[1]);
    }

    @Test
    public void test2_valid() {
        String engVal = "Be in debt";
        String sentence = "God bless you—i am forever in your debt";

        final String[] trainingPair = makeTrainingPair(engVal, sentence);
        Assert.assertNotNull(trainingPair);

        Assert.assertEquals("God bless you—i am forever in your [...]", trainingPair[0]);
        Assert.assertEquals("debt", trainingPair[1]);
    }

    @Test
    public void test3_valid() {
        String engVal = "Watch out";
        String sentence = "“watch out!” i screamed at him.";

        final String[] trainingPair = makeTrainingPair(engVal, sentence);
        Assert.assertNotNull(trainingPair);

        Assert.assertEquals("“[...]!” i screamed at him.", trainingPair[0]);
        Assert.assertEquals("watch out", trainingPair[1]);
    }

    @Test
    public void test4_valid() {
        String engVal = "Break";
        String sentence = "Breeeek the chocolate bar into pieces so that everyone can have some";

        final String[] trainingPair = makeTrainingPair(engVal, sentence);
        Assert.assertNotNull(trainingPair);

        Assert.assertEquals("[...] the chocolate bar into pieces so that everyone can have some", trainingPair[0]);
        Assert.assertEquals("Breeeek", trainingPair[1]);
    }

    @Test
    public void test5_valid() {
        String engVal = "Low-key";
        String sentence = "In south india, a low-key and cheap alternative to goa";

        final String[] trainingPair = makeTrainingPair(engVal, sentence);
        Assert.assertNotNull(trainingPair);

        Assert.assertEquals("In south india, a [...] and cheap alternative to goa", trainingPair[0]);
        Assert.assertEquals("low-key", trainingPair[1]);
    }

    @Test
    public void test6_valid() {
        String engVal = "Self-compassion";
        String sentence = "Self-compassion entails being warm towards oneself when encountering pain and personal shortcomings, rather than ignoring them or hurting oneself with self-criticism";

        final String[] trainingPair = makeTrainingPair(engVal, sentence);
        Assert.assertNotNull(trainingPair);

        Assert.assertEquals("[...] entails being warm towards oneself when encountering pain and personal shortcomings, rather than ignoring them or hurting oneself with self-criticism", trainingPair[0]);
        Assert.assertEquals("self-compassion", trainingPair[1]);
    }

    @Test
    public void test7_valid() {
        String engVal = "Benevolence";
        String sentence = "When virtue is lost, benevolence appears, when benevolence is lost right conduct appears, when right conduct is lost, expedience appears. expediency is the mere shadow of right and truth; it is the beginning of disorder. lao tzu";

        final String[] trainingPair = makeTrainingPair(engVal, sentence);
        Assert.assertNotNull(trainingPair);

        Assert.assertEquals("When virtue is lost, [...] appears, when [...] is lost right conduct appears, when right conduct is lost, expedience appears. expediency is the mere shadow of right and truth; it is the beginning of disorder. lao tzu", trainingPair[0]);
        Assert.assertEquals("benevolence", trainingPair[1]);
    }

    @Test
    public void test8_valid() {
        String engVal = "Inferior";
        String sentence = "Society, being codified by man, decrees that woman is inferior; she can do away with this inferiority only by destroying the male's superiority.";

        final String[] trainingPair = makeTrainingPair(engVal, sentence);
        Assert.assertNotNull(trainingPair);

        Assert.assertEquals("Society, being codified by man, decrees that woman is [...]; she can do away with this inferiority only by destroying the male's superiority.", trainingPair[0]);
        Assert.assertEquals("inferior", trainingPair[1]);
    }

    @Test
    public void test9_valid() {
        String engVal = "to keep someone in the loop";
        String sentence = "We've hired a new intern to help you with data entry, so be sure to keep her in the loop about the project.";

        final String[] trainingPair = makeTrainingPair(engVal, sentence);
        Assert.assertNotNull(trainingPair);

        Assert.assertEquals("We've hired a new intern to help you with data entry, so be sure [...] about the project.", trainingPair[0]);
        Assert.assertEquals("to keep her in the loop", trainingPair[1]);
    }

    @Test
    public void test_straightforward_approach_valid() {
        // word at beginning
        {
            String engVal = "B2aaseasdrt";
            String sentence = "B2aaseasdrt, sdsdsad ddfwere!";

            final String[] trainingPair = makeTrainingPair(engVal, sentence);
            Assert.assertNotNull(trainingPair);

            Assert.assertEquals("[...], sdsdsad ddfwere!", trainingPair[0]);
            Assert.assertEquals("b2aaseasdrt", trainingPair[1]);
        }
        {
            String engVal = "B2aaseasdrt";
            String sentence = "B2aaseasdrt sdsdsad ddfwere!";

            final String[] trainingPair = makeTrainingPair(engVal, sentence);
            Assert.assertNotNull(trainingPair);

            Assert.assertEquals("[...] sdsdsad ddfwere!", trainingPair[0]);
            Assert.assertEquals("b2aaseasdrt", trainingPair[1]);
        }
        // word at the end
        {
            String engVal = "B2aaseasdrt";
            String sentence = "Hey, sdsdsad ddfwere! B2aaseasdrt";

            final String[] trainingPair = makeTrainingPair(engVal, sentence);
            Assert.assertNotNull(trainingPair);

            Assert.assertEquals("Hey, sdsdsad ddfwere! [...]", trainingPair[0]);
            Assert.assertEquals("b2aaseasdrt", trainingPair[1]);
        }
        {
            String engVal = "B2aaseasdrt";
            String sentence = "Hey, sdsdsad ddfwere,B2aaseasdrt";

            final String[] trainingPair = makeTrainingPair(engVal, sentence);
            Assert.assertNotNull(trainingPair);

            Assert.assertEquals("Hey, sdsdsad ddfwere,[...]", trainingPair[0]);
            Assert.assertEquals("b2aaseasdrt", trainingPair[1]);
        }
        // word at the middle
        {
            String engVal = "B2aaseasdrt";
            String sentence = "aaaa, b2aaseasdrt sdsdsad ddfwere!";

            final String[] trainingPair = makeTrainingPair(engVal, sentence);
            Assert.assertNotNull(trainingPair);

            Assert.assertEquals("aaaa, [...] sdsdsad ddfwere!", trainingPair[0]);
            Assert.assertEquals("b2aaseasdrt", trainingPair[1]);
        }
        {
            String engVal = "B2aaseasdrt";
            String sentence = "aaaa, b2aaseasdrt! Sdsdsad ddfwere!";

            final String[] trainingPair = makeTrainingPair(engVal, sentence);
            Assert.assertNotNull(trainingPair);

            Assert.assertEquals("aaaa, [...]! Sdsdsad ddfwere!", trainingPair[0]);
            Assert.assertEquals("b2aaseasdrt", trainingPair[1]);
        }
        // word as verb in past tense
        {
            String engVal = "B2aaseasdrt";
            String sentence = "Hey, sdsdsad ddfwere! B2aaseasdrted";

            final String[] trainingPair = makeTrainingPair(engVal, sentence);
            Assert.assertNotNull(trainingPair);

            Assert.assertEquals("Hey, sdsdsad ddfwere! [...]", trainingPair[0]);
            Assert.assertEquals("B2aaseasdrted", trainingPair[1]);
        }
    }

    @Test
    public void irregularVerb() {
        String engVal = "Buy";
        String sentence = "I bought a new pair of jeans";

        final String[] trainingPair = makeTrainingPair(engVal, sentence);
        Assert.assertNotNull(trainingPair);

        Assert.assertEquals("I [...] a new pair of jeans", trainingPair[0]);
        Assert.assertEquals("bought", trainingPair[1]);

    }

    @Test
    public void test_invalid() {
        // Word == sentence
        {
            String engVal = "B2aaseasdrt";
            String sentence = "B2aaseAsdrt";

            final String[] trainingPair = makeTrainingPair(engVal, sentence);
            Assert.assertNull(trainingPair);
        }

        {
            String engVal = "";
            String sentence = "B2aaseAsdrt";

            final String[] trainingPair = makeTrainingPair(engVal, sentence);
            Assert.assertNull(trainingPair);
        }

        {
            String engVal = "aaa";
            String sentence = "";

            final String[] trainingPair = makeTrainingPair(engVal, sentence);
            Assert.assertNull(trainingPair);
        }

        {
            String engVal = "aaa";
            String sentence = "aa";

            final String[] trainingPair = makeTrainingPair(engVal, sentence);
            Assert.assertNull(trainingPair);
        }
    }


    @Test
    public void test1_invalid_case() {
        String engVal = "Break";
        String sentence = "Beeeek the chocolate bar into pieces so that everyone can have some";

        final String[] trainingPair = makeTrainingPair(engVal, sentence);
        Assert.assertNull(trainingPair);
    }

    /**
     * Idioms max number
     */
    @Test
    public void test2_invalid_case() {
        String engVal = "sb, sb someone someone";
        String sentence = "sb, sb ssssssssssssssssssssss";

        final String[] trainingPair = makeTrainingPair(engVal, sentence);
        Assert.assertNull(trainingPair);
    }

    @Test
    public void test10_valid() {
        String engVal = "Give somebody the low-down";
        String sentence = "Give me the low-down on what the boss is planning to do about the company's falling profits.";

        final String[] trainingPair = makeTrainingPair(engVal, sentence);
        Assert.assertNotNull(trainingPair);

        Assert.assertEquals("[...] on what the boss is planning to do about the company's falling profits.", trainingPair[0]);
        Assert.assertEquals("give me the low-down", trainingPair[1]);
    }

    @Test
    public void test11_valid() {
        String engVal = "Drop a hint";
        String sentence = "I was hoping to see her again, so i dropped a hint, saying i wasn't doing anything this weekend.";

        final String[] trainingPair = makeTrainingPair(engVal, sentence);
        Assert.assertNotNull(trainingPair);

        Assert.assertEquals("I was hoping to see her again, so i [...], saying i wasn't doing anything this weekend.", trainingPair[0]);
        Assert.assertEquals("dropped a hint", trainingPair[1]);
    }

    @Test
    public void test12_valid() {
        String engVal = "Be at a loss for words";
        String sentence = "I was at a loss for words when i met my friend after many years.";

        final String[] trainingPair = makeTrainingPair(engVal, sentence);
        Assert.assertNotNull(trainingPair);

        Assert.assertEquals("I [...] when i met my friend after many years.", trainingPair[0]);
        Assert.assertEquals("was at a loss for words", trainingPair[1]);
    }

    @Test
    public void test13_valid() {
        String engVal = "Keep someone in the loop";
        String sentence = "We've hired a new intern to help you with data entry, so be sure to keep her in the loop about the project.";

        final String[] trainingPair = makeTrainingPair(engVal, sentence);
        Assert.assertNotNull(trainingPair);

        Assert.assertEquals("We've hired a new intern to help you with data entry, so be sure to [...] about the project.", trainingPair[0]);
        Assert.assertEquals("keep her in the loop", trainingPair[1]);
    }

    @Test
    public void test14_valid() {
        String engVal = "To be like talking to a brick wall";
        String sentence = "I've tried to discuss my feelings with her, but it's like talking to a brick wall.";

        final String[] trainingPair = makeTrainingPair(engVal, sentence);
        Assert.assertNotNull(trainingPair);

        Assert.assertEquals("I've tried to discuss my feelings with her, but it's [...].", trainingPair[0]);
        Assert.assertEquals("like talking to a brick wall", trainingPair[1]);
    }

    @Test
    public void test15_valid() {
        String engVal = "Beat about the bush";
        String sentence = "Please, stop beating about the bush and get back to the point.";

        final String[] trainingPair = makeTrainingPair(engVal, sentence);
        Assert.assertNotNull(trainingPair);

        Assert.assertEquals("Please, stop [...] and get back to the point.", trainingPair[0]);
        Assert.assertEquals("beating about the bush", trainingPair[1]);
    }

    @Test
    public void test16_valid() {
        String engVal = "Talk at cross-purposes";
        String sentence = "Oh, i see now. we are talking at cross-purposes.";

        final String[] trainingPair = makeTrainingPair(engVal, sentence);
        Assert.assertNotNull(trainingPair);

        Assert.assertEquals("Oh, i see now. we are [...].", trainingPair[0]);
        Assert.assertEquals("talking at cross-purposes", trainingPair[1]);
    }

    @Test
    public void test17_valid() {
        String engVal = "To be on the same wavelength";
        String sentence = "Yeah, i agree with you - we're on the same wavelength.";

        final String[] trainingPair = makeTrainingPair(engVal, sentence);
        Assert.assertNotNull(trainingPair);

        Assert.assertEquals("Yeah, i agree with you - we're [...].", trainingPair[0]);
        Assert.assertEquals("on the same wavelength", trainingPair[1]);
    }

    @Test
    public void test18_valid() {
        String engVal = "Get the wrong end of the stick";
        String sentence = "I said how nice he was and julie got the wrong end of the stick and thought i wanted to go out with him.";

        final String[] trainingPair = makeTrainingPair(engVal, sentence);
        Assert.assertNotNull(trainingPair);

        Assert.assertEquals("I said how nice he was and julie [...] and thought i wanted to go out with him.", trainingPair[0]);
        Assert.assertEquals("got the wrong end of the stick", trainingPair[1]);
    }

    @Test
    public void test19_valid() {
        String engVal = "Put someone in the picture";
        String sentence = "His lawyer put him in the picture about what had happened since his arrest.";

        final String[] trainingPair = makeTrainingPair(engVal, sentence);
        Assert.assertNotNull(trainingPair);

        Assert.assertEquals("His lawyer [...] about what had happened since his arrest.", trainingPair[0]);
        Assert.assertEquals("put him in the picture", trainingPair[1]);
    }

    @Test
    public void test20_valid() {
        String engVal = "Hear it on the grapevine";
        String sentence = "I heard something on the grapevine - are you going to resign?";

        final String[] trainingPair = makeTrainingPair(engVal, sentence);
        Assert.assertNotNull(trainingPair);

        Assert.assertEquals("I [...] - are you going to resign?", trainingPair[0]);
        Assert.assertEquals("heard something on the grapevine", trainingPair[1]);
    }

    @Test
    public void test21_valid() {
        String engVal = "Get straight to the point";
        String sentence = "Let's not speak about secondary issues and get straight to the point.";

        final String[] trainingPair = makeTrainingPair(engVal, sentence);
        Assert.assertNotNull(trainingPair);

        Assert.assertEquals("Let's not speak about secondary issues and [...].", trainingPair[0]);
        Assert.assertEquals("get straight to the point", trainingPair[1]);
    }

    @Test
    public void test22_valid() {
        String engVal = "Put something in a nutshell";
        String sentence = "The explanation is long and involved, but let me put it in a nutshell for you.";

        final String[] trainingPair = makeTrainingPair(engVal, sentence);
        Assert.assertNotNull(trainingPair);

        Assert.assertEquals("The explanation is long and involved, but let me [...] for you.", trainingPair[0]);
        Assert.assertEquals("put it in a nutshell", trainingPair[1]);
    }

    @Test
    public void test23_valid() {
        String engVal = "Make smth up";
        String sentence = "That's not true! you just made that up!";

        final String[] trainingPair = makeTrainingPair(engVal, sentence);
        Assert.assertNotNull(trainingPair);

        Assert.assertEquals("That's not true! you just [...]!", trainingPair[0]);
        Assert.assertEquals("made that up", trainingPair[1]);
    }

    @Test
    public void test24_valid() {
        String engVal = "Cause offence";
        String sentence = "We have had our differences and i'm sorry if it has caused offence.";

        final String[] trainingPair = makeTrainingPair(engVal, sentence);
        Assert.assertNotNull(trainingPair);

        Assert.assertEquals("We have had our differences and i'm sorry if it has [...].", trainingPair[0]);
        Assert.assertEquals("caused offence", trainingPair[1]);
    }

    @Test
    public void test25_valid() {
        String engVal = "Acquaint with";
        String sentence = "Are you acquainted with him?";

        final String[] trainingPair = makeTrainingPair(engVal, sentence);
        Assert.assertNotNull(trainingPair);

        Assert.assertEquals("Are you [...] him?", trainingPair[0]);
        Assert.assertEquals("acquainted with", trainingPair[1]);
    }

    @Test
    public void test_idioms_bunch() {
        {
            final String[] trainingPair = makeTrainingPair(
                    "Get rid of", "Let's get rid of that broken chair."
            );
            Assert.assertNotNull(trainingPair);
            Assert.assertEquals("Let's [...] that broken chair.", trainingPair[0]);
            Assert.assertEquals("get rid of", trainingPair[1]);
        }
        {
            final String[] trainingPair = makeTrainingPair(
                    "Obey", "He always obeys his parents"
            );
            Assert.assertNotNull(trainingPair);
            Assert.assertEquals("He always [...] his parents", trainingPair[0]);
            Assert.assertEquals("obeys", trainingPair[1]);
        }
        {
            final String[] trainingPair = makeTrainingPair(
                    "Malfeasance", "Should i have reported oscar's malfeasance?"
            );
            Assert.assertNotNull(trainingPair);
            Assert.assertEquals("Should i have reported oscar's [...]?", trainingPair[0]);
            Assert.assertEquals("malfeasance", trainingPair[1]);
        }
        {
            final String[] trainingPair = makeTrainingPair(
                    "To have a crush on sb", "I told michael that i had had a crush on you"
            );
            Assert.assertNotNull(trainingPair);
            Assert.assertEquals("I told michael that i had had [...]", trainingPair[0]);
            Assert.assertEquals("a crush on you", trainingPair[1]);
        }
        {
            final String[] trainingPair = makeTrainingPair(
                    "Damn it", "Damn it! i forgot my keys!"
            );
            Assert.assertNotNull(trainingPair);
            Assert.assertEquals("[...]! i forgot my keys!", trainingPair[0]);
            Assert.assertEquals("damn it", trainingPair[1]);
        }
        {
            final String[] trainingPair = makeTrainingPair(
                    "Hang out", "I don't know why he hangs out with james, they've got nothing in common. they spent the whole day hanging out by the pool."
            );
            Assert.assertNotNull(trainingPair);
            Assert.assertEquals("I don't know why he [...] with james, they've got nothing in common. they spent the whole day hanging out by the pool.", trainingPair[0]);
            Assert.assertEquals("hangs out", trainingPair[1]);
        }
        {
            final String[] trainingPair = makeTrainingPair(
                    "Think better of it", "He looked back at the whisperers as if he wanted to say something to them, but thought better of it."
            );
            Assert.assertNotNull(trainingPair);
            Assert.assertEquals("He looked back at the whisperers as if he wanted to say something to them, but [...].", trainingPair[0]);
            Assert.assertEquals("thought better of it", trainingPair[1]);
        }
        {
            // todo: think about replacing with wild card
            final String[] trainingPair = makeTrainingPair(
                    "Ask smb out", "How to ask a girl out"
            );
            Assert.assertNotNull(trainingPair);
//        Assert.assertEquals("You have to [...] in some distinctive way?", trainingPair[0]);
//        Assert.assertEquals("stand out", trainingPair[1]);
        }
        {
            final String[] trainingPair = makeTrainingPair(
                    "Be in labor", "I was in labor for 72 hours"
            );
            Assert.assertNotNull(trainingPair);
            Assert.assertEquals("I [...] for 72 hours", trainingPair[0]);
            Assert.assertEquals("was in labor", trainingPair[1]);
        }
        {
            final String[] trainingPair = makeTrainingPair(
                    "Being sacked", "How i got sacked from the cambridge"
            );
            Assert.assertNotNull(trainingPair);
            Assert.assertEquals("How i [...] from the cambridge", trainingPair[0]);
            Assert.assertEquals("got sacked", trainingPair[1]);
        }
        {
            final String[] trainingPair = makeTrainingPair(
                    "Get together", "A couple getting together"
            );
            Assert.assertNotNull(trainingPair);
            Assert.assertEquals("A couple [...]", trainingPair[0]);
            Assert.assertEquals("getting together", trainingPair[1]);
        }
        {
            final String[] trainingPair = makeTrainingPair(
                    "Mess sb about", "I wish he'd stop messing us about!"
            );
            Assert.assertNotNull(trainingPair);
            Assert.assertEquals("I wish he'd stop [...]!", trainingPair[0]);
            Assert.assertEquals("messing us about", trainingPair[1]);
        }
        {
            final String[] trainingPair = makeTrainingPair(
                    "Play up", "The starter motor was playing up again."
            );
            Assert.assertNotNull(trainingPair);
            Assert.assertEquals("The starter motor was [...] again.", trainingPair[0]);
            Assert.assertEquals("playing up", trainingPair[1]);
        }
        {
            // todo: think about replacing with wild card
            final String[] trainingPair = makeTrainingPair(
                    "Ask around", "I asked mary around to watch a movie, but she was busy."
            );
            Assert.assertNotNull(trainingPair);
//        Assert.assertEquals("You have to [...] in some distinctive way?", trainingPair[0]);
//        Assert.assertEquals("stand out", trainingPair[1]);
        }
        {
            final String[] trainingPair = makeTrainingPair(
                    "Take down", "Took down some notes"
            );
            Assert.assertNotNull(trainingPair);
            Assert.assertEquals("[...] some notes", trainingPair[0]);
            Assert.assertEquals("took down", trainingPair[1]);
        }
        {
            final String[] trainingPair = makeTrainingPair(
                    "Come across", "John came across a book he had been looking for."
            );
            Assert.assertNotNull(trainingPair);
            Assert.assertEquals("John [...] a book he had been looking for.", trainingPair[0]);
            Assert.assertEquals("came across", trainingPair[1]);
        }
        {
            final String[] trainingPair = makeTrainingPair(
                    "Turn up", "We expected guests in the evening but they turned up while we were having breakfast"
            );
            Assert.assertNotNull(trainingPair);
            Assert.assertEquals("We expected guests in the evening but they [...] while we were having breakfast", trainingPair[0]);
            Assert.assertEquals("turned up", trainingPair[1]);
        }
        {
            final String[] trainingPair = makeTrainingPair(
                    "Drop off", "I dropped off the package at her house"
            );
            Assert.assertNotNull(trainingPair);
            Assert.assertEquals("I [...] the package at her house", trainingPair[0]);
            Assert.assertEquals("dropped off", trainingPair[1]);
        }
        {
            // todo: think about replacing with wild card
            final String[] trainingPair = makeTrainingPair(
                    "Bring back smth", "That music always brings back happy memories."
            );
            Assert.assertNotNull(trainingPair);
//        Assert.assertEquals("You have to [...] in some distinctive way?", trainingPair[0]);
//        Assert.assertEquals("stand out", trainingPair[1]);
        }
        {
            final String[] trainingPair = makeTrainingPair(
                    "Make sb out", "I just can't make him out at all."
            );
            Assert.assertNotNull(trainingPair);
            Assert.assertEquals("I just can't [...] at all.", trainingPair[0]);
            Assert.assertEquals("make him out", trainingPair[1]);
        }
        {
            final String[] trainingPair = makeTrainingPair(
                    "Do you mind", "I'm going for a walk - do you mind?"
            );
            Assert.assertNotNull(trainingPair);
            Assert.assertEquals("I'm going for a walk - [...]?", trainingPair[0]);
            Assert.assertEquals("do you mind", trainingPair[1]);
        }
        {
            final String[] trainingPair = makeTrainingPair(
                    "Chalk up", "Chalk up another goal for sarah."
            );
            Assert.assertNotNull(trainingPair);
            Assert.assertEquals("[...] another goal for sarah.", trainingPair[0]);
            Assert.assertEquals("chalk up", trainingPair[1]);
        }
        {
            // todo: think about replacing with wild card
            final String[] trainingPair = makeTrainingPair(
                    "Fall for someone", "Mike has fallen for heather."
            );
            Assert.assertNotNull(trainingPair);
//        Assert.assertEquals("You have to [...] in some distinctive way?", trainingPair[0]);
//        Assert.assertEquals("stand out", trainingPair[1]);
        }
        {
            final String[] trainingPair = makeTrainingPair(
                    "to be over sb", "A woman says to a man: \"i'm over you\"."
            );
            Assert.assertNotNull(trainingPair);
            Assert.assertEquals("A woman says to a man: \"i'm [...]\".", trainingPair[0]);
            Assert.assertEquals("over you", trainingPair[1]);
        }
        {
            final String[] trainingPair = makeTrainingPair(
                    "Do smb a service", "You've done me a service - thank you"
            );
            Assert.assertNotNull(trainingPair);
            Assert.assertEquals("You've [...] - thank you", trainingPair[0]);
            Assert.assertEquals("done me a service", trainingPair[1]);
        }
        {
            final String[] trainingPair = makeTrainingPair(
                    "over the top", "I know he was angry, but attacking the waiter was way over the top"
            );
            Assert.assertNotNull(trainingPair);
            Assert.assertEquals("I know he was angry, but attacking the waiter was way [...]", trainingPair[0]);
            Assert.assertEquals("over the top", trainingPair[1]);
        }
        {
            final String[] trainingPair = makeTrainingPair(
                    "Covered by", "Do you think women's rights are well covered by the media?"
            );
            Assert.assertNotNull(trainingPair);
            Assert.assertEquals("Do you think women's rights are well [...] the media?", trainingPair[0]);
            Assert.assertEquals("covered by", trainingPair[1]);
        }
        {
            final String[] trainingPair = makeTrainingPair(
                    "in sb debt", "God bless you—i am forever in your debt"
            );
            Assert.assertNotNull(trainingPair);
            Assert.assertEquals("God bless you—i am forever [...]", trainingPair[0]);
            Assert.assertEquals("in your debt", trainingPair[1]);
        }
        {
            final String[] trainingPair = makeTrainingPair(
                    "Get away with", "They have repeatedly broken the law and got away with it."
            );
            Assert.assertNotNull(trainingPair);
            Assert.assertEquals("They have repeatedly broken the law and [...] it.", trainingPair[0]);
            Assert.assertEquals("got away with", trainingPair[1]);
        }
        {
            final String[] trainingPair = makeTrainingPair(
                    "Get on with someone", "I am ready to give you a testimonial that one can get on with you"
            );
            Assert.assertNotNull(trainingPair);
            Assert.assertEquals("I am ready to give you a testimonial that one can [...]", trainingPair[0]);
            Assert.assertEquals("get on with you", trainingPair[1]);
        }
        {
            final String[] trainingPair = makeTrainingPair(
                    "Break down", "John's car broke down so he had to take the bus"
            );
            Assert.assertNotNull(trainingPair);
            Assert.assertEquals("John's car [...] so he had to take the bus", trainingPair[0]);
            Assert.assertEquals("broke down", trainingPair[1]);
        }
    }

    @Test
    public void eludeIrregularVerbUtils() {
        for (String verb : IrregularVerbUtils.getFullList()) {
            final String[] wordForms = WordFormUtils.findWordForms(verb);
            if (wordForms == null || wordForms.length == 0) {
                throw new RuntimeException(verb);
            }
        }

    }
}
