package guru.h4t_eng.datasource;

/**
 * WorkoutType.
 *
 * Created by aalexeenka on 11/2/2015.
 */
public enum WorkoutType {
    ENG_RUS(
            "ENG-RUS",
            "eng_rus",
            "r_eng_rus"
    ),
    DEF_ENG(
            "DEF-ENG",
            "def_eng",
            "r_def_eng"
    ),
    RUS_ENG(
            "RUS-ENG",
            "rus_eng",
            "r_rus_eng"
    ),
    IMG_ENG(
            "IMG-ENG",
            "img_eng",
            "r_img_eng"
    ),
    SEN_ENG(
            "SEN-ENG",
            "sen_eng",
            "r_sen_eng"
    );

    public String uiName;
    public String columnName;
    public String repeatColumnName;

    WorkoutType(String uiName, String columnName, String repeatColumnName) {
        this.uiName = uiName;
        this.columnName = columnName;
        this.repeatColumnName = repeatColumnName;
    }
}
