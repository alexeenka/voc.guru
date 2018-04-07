package guru.h4t_eng.security;

import guru.h4t_eng.admin.ActiveUsersLogger;
import guru.h4t_eng.config.ApplicationProperties;
import guru.h4t_eng.security.model.Constants;
import guru.h4t_eng.security.model.UserSessionCookie;
import guru.h4t_eng.util.SecurityEncodeUtil;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

import static guru.h4t_eng.security.auth_servlet.FbAuthenticationServlet.FACEBOOK_AUTHORIZE_PATH;
import static guru.h4t_eng.security.auth_servlet.VkAuthenticationServlet.VK_AUTHORIZE_PATH;

/**
 * AuthenticationFilter.
 *
 * Created by Alexey Alexeenka on 04.07.2015.
 */
@WebFilter(filterName = "authenticationFilter")
public class AuthenticationFilter implements Filter {

    public void init(FilterConfig filterConfig) throws ServletException {
    }

    private static final ActiveUsersLogger ACTIVE_USERS = ActiveUsersLogger.getInstance();

    public void doFilter(
            ServletRequest request,
            ServletResponse response,
            FilterChain chain
    ) throws ServletException, IOException {
        HttpServletResponse httpResp = (HttpServletResponse) response;
        HttpServletRequest httpReq = (HttpServletRequest) request;

        // System.out.println("Request_uri: " + ((HttpServletRequest) request).getRequestURI());

        if (!authFilter(httpReq, httpResp)) {
            return;
        }

        chain.doFilter(request, response);
    }

    private static final String BUILD_VERSION = ApplicationProperties.getInstance().getBuildVersion();

    /**
     * Return true if user authenticate.
     */
    public static boolean authFilter(HttpServletRequest httpReq, HttpServletResponse httpResp) throws IOException {
        final String requestURI = httpReq.getRequestURI();
        final Optional<String> optionalRequestURI = Optional.ofNullable(requestURI);

        final boolean isSocialNetworkAuthorizationProcess = optionalRequestURI.isPresent() &&
                requestURI.contains(FACEBOOK_AUTHORIZE_PATH) || requestURI.contains(VK_AUTHORIZE_PATH);

        final boolean isLoginProcess = ApplicationProperties.getInstance().getLoginPage().equals(requestURI);

        if (isSocialNetworkAuthorizationProcess || isLoginProcess) {
            return true;
        }

        // check version header, to avoid situation when old tab of app is used to work.
        final String header = httpReq.getHeader("voc-version");
        if (header != null && !BUILD_VERSION.equals(header.trim())) {
            loginRequired(httpReq, httpResp);
            return false;
        }

        Cookie userCookie = getUserCookie(httpReq);

        if (userCookie == null) {
            loginRequired(httpReq, httpResp);
            return false;
        }


        String value = userCookie.getValue();
        UserSessionCookie cookie;
        try {
            String decodedValue = SecurityEncodeUtil.cipherDecode(value);
            cookie = UserSessionCookie.fromJson(decodedValue);
        } catch (Throwable th) {
            CookieUtils.removeCookie(httpResp);
            loginRequired(httpReq, httpResp);
            return false;
        }

        // check headers
        if (
                !httpReq.getHeader("User-Agent").equals(cookie.getHeaderUserAgent()) ||
                        !httpReq.getHeader("Host").equals(cookie.getHeaderHost())
                ) {
            CookieUtils.removeCookie(httpResp);
            loginRequired(httpReq, httpResp);
            return false;
        }

        final UUID userId = UUID.fromString(cookie.getAppUserId());
        httpReq.setAttribute(Constants.USER_ID, userId);
        ACTIVE_USERS.activate(userId);
        return true;
    }

    private static void loginRequired(HttpServletRequest httpReq, HttpServletResponse httpResp) throws IOException {
        // for REST, send error with code 406 "Not Acceptable", processed in main.js
        String requestURI = httpReq.getRequestURI();
        if (requestURI != null && requestURI.startsWith("/rest")) {
            httpResp.sendError(HttpServletResponse.SC_NOT_ACCEPTABLE);
            return;
        }

        // not for REST
        redirect2LoginPage(httpResp);
    }

    public static void redirect2LoginPage(HttpServletResponse httpResp) throws IOException {
        httpResp.sendRedirect(ApplicationProperties.getInstance().getLoginPage());
    }

    public static void redirect2IndexPage(HttpServletResponse response) throws ServletException, IOException {
        response.sendRedirect(ApplicationProperties.getInstance().getIndexPage());
    }

    private static Cookie getUserCookie(HttpServletRequest httpReq) {
        if (httpReq.getCookies() == null) {
            return null;
        }

        for (Cookie cookie : httpReq.getCookies()) {
            if (UserSessionCookie.COOKIE_NAME.equals(cookie.getName())) {
                return cookie;
            }
        }

        return null;
    }

    public void destroy() {
    }


}
