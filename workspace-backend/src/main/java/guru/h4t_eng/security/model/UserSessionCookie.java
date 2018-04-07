package guru.h4t_eng.security.model;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * UserSessionCookie.
 * избавиться от сессии:
 *
 * 1.проверка на headers:
 * ========================================================
 * User-Agent:Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/50.0.2661.75 Safari/537.36
 * Host:10.99.55.90:9080
 * -----------
 * User-Agent:Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/50.0.2661.75 Safari/537.36
 * Host:voc.guru
 *
 * 2. user_id
 * ========================================================
 * айди пользователя в БД
 * Created by aalexeenka on 27.04.2016.
 */
public class UserSessionCookie {

    public static final String COOKIE_NAME = "_voc";

    public UserSessionCookie(String headerUserAgent, String headerHost, String appUserId) {
        this.headerUserAgent = headerUserAgent;
        this.headerHost = headerHost;
        this.appUserId = appUserId;
    }

    private String headerUserAgent;

    private String headerHost;

    private String appUserId;


    public String getHeaderUserAgent() {
        return headerUserAgent;
    }

    public void setHeaderUserAgent(String headerUserAgent) {
        this.headerUserAgent = headerUserAgent;
    }

    public String getHeaderHost() {
        return headerHost;
    }

    public void setHeaderHost(String headerHost) {
        this.headerHost = headerHost;
    }

    public String getAppUserId() {
        return appUserId;
    }

    public void setAppUserId(String appUserId) {
        this.appUserId = appUserId;
    }


    public static String toJson(UserSessionCookie cookie) {
        return Json.object()
                .add("hua", cookie.headerUserAgent)
                .add("hh", cookie.headerHost)
                .add("aui", cookie.appUserId)
                .toString();
    }

    public static UserSessionCookie fromJson(String value) {
        JsonObject json = Json.parse(value).asObject();
        return new UserSessionCookie(
                json.get("hua").asString(),
                json.get("hh").asString(),
                json.get("aui").asString()
        );


    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof UserSessionCookie)) return false;

        UserSessionCookie that = (UserSessionCookie) o;

        return new EqualsBuilder()
                .append(headerUserAgent, that.headerUserAgent)
                .append(headerHost, that.headerHost)
                .append(appUserId, that.appUserId)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(headerUserAgent)
                .append(headerHost)
                .append(appUserId)
                .toHashCode();
    }
}
