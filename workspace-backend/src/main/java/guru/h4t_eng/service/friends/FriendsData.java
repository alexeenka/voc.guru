package guru.h4t_eng.service.friends;

/**
 * FriendsData.
 * "data": [
 *  {
 *    "name": "Nancy VocGuru Qinstein",
 *    "id": "131782960633139"
 *  },
 *  ..
 * ]
 * Created by aalexeenka on 21.11.2016.
 */
public class FriendsData {


    private String name;

    /**
     * For facebook, it is facebook_id
     * For vk, it is secure_uuid
     */
    private String id;

    /**
     * Filled only for VK now
     */
    private String profileImg;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProfileImg() {
        return profileImg;
    }

    public void setProfileImg(String profileImg) {
        this.profileImg = profileImg;
    }

}