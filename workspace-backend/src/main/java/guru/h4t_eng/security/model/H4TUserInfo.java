package guru.h4t_eng.security.model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

/**
 * H4TUserInfo.
 *
 * Created by Alexey Alexeenka on 11.07.2015.
 */
public class H4TUserInfo implements Serializable {

    private static final long serialVersionUID = -7885972361833152652L;

    public static final String SESSION_ID = "H4TUserSession";

    /**
     * The userId, KEY NUMBER IN THE SYSTEM
     */
    private UUID userId;

    private final UserAuthType userAuthType;
    private final long socialNetworkId;
    private final String photoURL;
    private final String lastName;
    private final String firstName;
    private final String email;
    private final String accessToken;
    private final Date lastLoginDate;

    public H4TUserInfo(UserAuthType userAuthType, long socialNetworkId, String firstName, String lastName, String photoURL, String email, String accessToken, Date lastLoginDate) {
        this.userAuthType = userAuthType;
        this.socialNetworkId = socialNetworkId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.photoURL = photoURL;
        this.email = email;
        this.accessToken = accessToken;
        this.lastLoginDate = lastLoginDate;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public long getSocialNetworkId() {
        return socialNetworkId;
    }

    public String getPhotoURL() {
        return photoURL;
    }

    public String getLastName() {
        return lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public Date getLastLoginDate() {
        return lastLoginDate;
    }

    public UserAuthType getUserAuthType() {
        return userAuthType;
    }

    public String getEmail() { return email; }



    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("userId", userId)
                .append("userAuthType", userAuthType)
                .append("socialNetworkId", socialNetworkId)
                .append("photoURL", photoURL)
                .append("lastName", lastName)
                .append("firstName", firstName)
                .append("accessToken", accessToken)
                .append("lastLoginDate", lastLoginDate)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof H4TUserInfo)) return false;

        H4TUserInfo that = (H4TUserInfo) o;

        return new EqualsBuilder()
                .append(getSocialNetworkId(), that.getSocialNetworkId())
                .append(getUserId(), that.getUserId())
                .append(getUserAuthType(), that.getUserAuthType())
                .append(getPhotoURL(), that.getPhotoURL())
                .append(getLastName(), that.getLastName())
                .append(getFirstName(), that.getFirstName())
                .append(getAccessToken(), that.getAccessToken())
                .append(getLastLoginDate(), that.getLastLoginDate())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(getUserId())
                .append(getUserAuthType())
                .append(getSocialNetworkId())
                .append(getPhotoURL())
                .append(getLastName())
                .append(getFirstName())
                .append(getAccessToken())
                .append(getLastLoginDate())
                .toHashCode();
    }
}
