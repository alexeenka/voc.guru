package guru.h4t_eng.global_voc;

import java.util.UUID;

/**
 * GlobalVocAuthor.
 * <p>
 * Created by aalexeenka on 12.01.2017.
 */
public class GlobalVocAuthor {
    public GlobalVocAuthor(UUID uid, String name, String profileImg, String fbuid, String vkuid) {
        this.uid = uid;
        this.name = name;
        this.profileImg = profileImg;
        this.fbuid = fbuid;
        this.vkuid = vkuid;
    }

    private UUID uid;
    private String name;
    private String profileImg;
    private String fbuid;
    private String vkuid;

    public UUID getUid() {
        return uid;
    }

    public void setUid(UUID uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProfileImg() {
        return profileImg;
    }

    public void setProfileImg(String profileImg) {
        this.profileImg = profileImg;
    }

    public String getFbuid() {
        return fbuid;
    }

    public void setFbuid(String fbuid) {
        this.fbuid = fbuid;
    }

    public String getVkuid() {
        return vkuid;
    }

    public void setVkuid(String vkuid) {
        this.vkuid = vkuid;
    }
}
