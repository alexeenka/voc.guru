package guru.h4t_eng.model;

/**
 * WordType.
 *
 * 1. Avoid Enums
 *
 * Enums are typically used to represent constants, but they are much more expensive than primitive-type representations,
 * in terms of the code size and the memory allocated for the enum objects.
 * https://medium.com/google-developers/developing-for-android-ii-bb9a51f8c8b9#.ab7t3c1g4
 *
 * 2. Enums often require more than twice as much memory as static constants. You should strictly avoid using enums on Android.
 *
 * https://developer.android.com/training/articles/memory.html#Overhead
 *
 *
 * Created by aalexeenka on 29.07.2016.
 */
public final class WordType {

    private WordType() {
    }

    public static final int NOUN = 0;

    public static final int VERB = 10;

    public static final int ADJECTIVE = 20;

    public static final int ADVERB = 30;

    public static final int PHRASAL_VERB = 40;

    public static final int IDIOM = 50;

    public static final int OTHER = 60;

    public static String evalStringValue(int wordType)
    {
        switch (wordType)
        {
            case NOUN:
                return "Noun";
            case VERB:
                return "Verb";
            case ADJECTIVE:
                return "Adjective";
            case ADVERB:
                return "Adverb";
            case PHRASAL_VERB:
                return "Phrasal verb";
            case IDIOM:
                return "Idiom";
            case OTHER:
                return "Other";
        }

        return "Other+";
    }
}
