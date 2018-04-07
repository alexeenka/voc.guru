package guru.h4t_eng.service.friends;

import com.google.gson.annotations.SerializedName;

/**
 *
 * FriendsSummary.
 *
 * For facebook it's in json:
 *
 * "summary": {
 *  "total_count": 11
 * }
 *
 * Created by aalexeenka on 21.11.2016.
 */
public class FriendsSummary {

    @SerializedName("total_count")
    private int totalCount;

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }
}
