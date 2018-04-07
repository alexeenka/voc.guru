package guru.h4t_eng.top_users;

import java.util.UUID;

/**
 * Contains information about TopUser.
 *
 * Created by aalexeenka on 12.05.2017.
 */
public class TopUser {

    private UUID userId;
    private long work;
    private String firstName;
    private String lastName;
    private long vkuid;
    private long fbuid;
    private String photoUrl;

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public long getWork() {
        return work;
    }

    public void setWork(long work) {
        this.work = work;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public long getVkuid() {
        return vkuid;
    }

    public void setVkuid(long vkuid) {
        this.vkuid = vkuid;
    }

    public long getFbuid() {
        return fbuid;
    }

    public void setFbuid(long fbuid) {
        this.fbuid = fbuid;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }
}
