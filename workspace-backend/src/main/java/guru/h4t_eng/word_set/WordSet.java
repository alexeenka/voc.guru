package guru.h4t_eng.word_set;

/**
 * System Sets.
 */
public enum WordSet {
    QUALITIES_50(0),
    THINGS_200(1);

    private final int id;

    WordSet(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
