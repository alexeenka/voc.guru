package guru.h4t_eng.rest.friends;

import guru.h4t_eng.rest.json.FastJsonBuilder;
import guru.h4t_eng.rest.json.JsonDto;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * FriendsInfoDto.
 *
 * Created by aalexeenka on 18.05.2016.
 */
public class FriendsInfoDto implements JsonDto {

    private String friendUid;
    private String photoUrl;
    private String firstName;
    private Long wordCount;
    private long todaySpentTime;
    private long vkUid;
    private long fbUid;
    private Long[] trainingCalendar;

    public String getFriendUid() {
        return friendUid;
    }

    public void setFriendUid(String friendUid) {
        this.friendUid = friendUid;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setWordCount(Long wordCount) {
        this.wordCount = wordCount;
    }

    public Long getWordCount() {
        return wordCount;
    }

    public void setTodaySpentTime(long todaySpentTime) {
        this.todaySpentTime = todaySpentTime;
    }

    public long getTodaySpentTime() {
        return todaySpentTime;
    }

    public Long[] getTrainingCalendar() {
        return trainingCalendar;
    }

    public void setTrainingCalendar(Long[] trainingCalendar) {
        this.trainingCalendar = trainingCalendar;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("friendUid", friendUid)
                .append("photoUrl", photoUrl)
                .append("firstName", firstName)
                .append("wordCount", wordCount)
                .append("todaySpentTime", todaySpentTime)
                .append("vkUid", vkUid)
                .append("fbUid", fbUid)
                .append("trainingCalendar", trainingCalendar)
                .toString();
    }

    @Override
    public String toJson() {
        FastJsonBuilder builder = new FastJsonBuilder();
        return builder
                .append("friendUid", friendUid)
                .next()
                .append("photoUrl", photoUrl)
                .next()
                .append("firstName", firstName)
                .next()
                .appendNumber("wordCount", wordCount)
                .next()
                .appendNumber("todaySpentTime", todaySpentTime)
                .next()
                .appendNumber("vkUid", vkUid)
                .next()
                .appendNumber("fbUid", fbUid)
                .next()
                .appendLongArray("trainingCalendar", trainingCalendar)
                .finish();
    }

    public void setVkUid(long vkUid) {
        this.vkUid = vkUid;
    }

    public long getVkUid() {
        return vkUid;
    }

    public void setFbUid(long fbUid) {
        this.fbUid = fbUid;
    }

    public long getFbUid() {
        return fbUid;
    }
}