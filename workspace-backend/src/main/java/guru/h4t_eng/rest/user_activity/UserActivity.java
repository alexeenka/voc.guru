package guru.h4t_eng.rest.user_activity;

import java.util.List;

/**
 * UserActivity.
 *
 * Created by aalexeenka on 22.02.2017.
 */
public class UserActivity {

    private String id;
    private List<String> data;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<String> getData() {
        return data;
    }

    public void setData(List<String> data) {
        this.data = data;
    }
}
