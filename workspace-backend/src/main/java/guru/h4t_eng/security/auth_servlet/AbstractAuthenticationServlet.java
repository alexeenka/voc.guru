package guru.h4t_eng.security.auth_servlet;

import com.google.common.collect.ImmutableSet;
import guru.h4t_eng.datasource.UserDataSource;
import guru.h4t_eng.exception.H4TApplicationException;
import guru.h4t_eng.logs.AppLoggerFactory;
import guru.h4t_eng.security.model.H4TUserInfo;
import guru.h4t_eng.security.model.UserSessionCookie;
import guru.h4t_eng.util.SecurityEncodeUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Enumeration;
import java.util.Set;
import java.util.UUID;

/**
 * AuthenticationServlet.
 *
 * Created by aalexeenka on 2/29/2016.
 */
public abstract class AbstractAuthenticationServlet extends HttpServlet {

    private static final Logger LOG = AppLoggerFactory.getH4TLog(AbstractAuthenticationServlet.class);

    public static final String NO_EMAIL = "no-email";

    private static final Set<String> HEADERS_TO_GET_IP = ImmutableSet.of(
            "x-forwarded-for", // example: Header [x-forwarded-for], value [178.154.20.77, 66.102.9.79]
            "x-real-ip", // example: Header [x-real-ip], value [66.102.9.79]
            "proxy-client-ip",
            "wl-proxy-client-ip",
            "http_x_forwarded_for",
            "http_x_forwarded",
            "http_x_cluster_client_ip",
            "http_client_ip",
            "http_forwarded_for",
            "http_forwarded",
            "http_via",
            "remote_addr"
    );

    /**
     * 28 days expired period
     */
    public static final int EXPIRED_PERIOD = 60 * 60 * 24 * 28;

    public static void printHeaders(HttpServletRequest request, String name) {
        LOG.debug("PrintHeaders. Begin [{}]", name);
        final Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String header = headerNames.nextElement();

            String value = request.getHeader(header);
            LOG.debug("Header [{}], value [{}]", header, value);
        }
        LOG.debug("GetRemoteAdder value  [{}]", request.getRemoteAddr());
        LOG.debug("PrintHeaders. End [{}]", name);
    }

    public static String getClientIpAddress(HttpServletRequest request) {
        //printHeaders(request, "getClientIpAddress");
        String ip = null;

        for (Enumeration<String> headerNames = request.getHeaderNames(); headerNames.hasMoreElements();) {
            String header = headerNames.nextElement();
            if (HEADERS_TO_GET_IP.contains(header.toLowerCase())) {
                String ipValue = request.getHeader(header);
                if (StringUtils.isNoneBlank(ipValue) && !"unknown".equalsIgnoreCase(ipValue)) {
                    if (ip == null) {
                        LOG.trace("Found and will use for IP: Header [{}], value [{}]", header, ipValue);
                        ip = ipValue;
                        continue;
                    }
                    LOG.trace("Also found: Header [{}], value [{}]", header, ipValue);
                }
            }
        }

        if (ip == null) {
            LOG.trace("Not found header, use IP is eval of GetRemoteAdder value  [{}]", request.getRemoteAddr());
            return request.getRemoteAddr();
        }

        return ip;
    }

    public void loginUser(HttpServletRequest request, H4TUserInfo userInfo) throws H4TApplicationException {
        final String userAgent = request.getHeader("user-agent");
        final String clientIpAddress = getClientIpAddress(request);
        UserDataSource.getInstance().loginUser(userInfo, userAgent, clientIpAddress);
    }

    public void createCookie(HttpServletRequest req, HttpServletResponse resp, UUID userId) {
        //userId = UUID.fromString("37d55540-2734-11e7-a102-c31caea4b96a");
        UserSessionCookie userSessionCookie = new UserSessionCookie(
                req.getHeader("User-Agent"),
                req.getHeader("Host"),
                userId.toString()
        );
        String encode = SecurityEncodeUtil.cipherEncode(UserSessionCookie.toJson(userSessionCookie));

        Cookie cookie = new Cookie(UserSessionCookie.COOKIE_NAME, encode);
        // Set expiry date after one week.
        cookie.setMaxAge(EXPIRED_PERIOD);

        // Add the cookie to the response header.
        resp.addCookie(cookie);
    }

}
