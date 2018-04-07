package guru.h4t_eng.security;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;

/**
 * CacheFilter.
 *
 * Created by Alexey Alexeenka on 25.10.2015.
 */
@WebFilter(filterName = "cacheFilter")
public class CacheFilter implements javax.servlet.Filter {

    public static final String[] CACHEABLE_CONTENT_TYPES = new String[] {
            "text/css",
            "text/javascript",
            "application/javascript",
            "text/html",
            "image/png",
            "image/jpeg",
            "image/gif",
            "image/jpg"
    };

    public static String[] DONT_CACHE_HTML_PAGES = new String [] {
            "/index.html",
            "/login.html"
    };

    public static final String[] CACHEABLE_RESOURCES_NAMES = new String[] {
            "glyphicons-halflings-regular.woff2"
    };

    static
    {
        Arrays.sort(CACHEABLE_CONTENT_TYPES);
        Arrays.sort(DONT_CACHE_HTML_PAGES);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        final HttpServletRequest httpReq = (HttpServletRequest) servletRequest;
        final HttpServletResponse httpResp = (HttpServletResponse) servletResponse;

        String contentType = servletRequest.getServletContext().getMimeType(httpReq.getRequestURI());

        // check main application pages
        if (isDontCacheHtmlPages(httpReq)) {
            httpResp.setHeader("Expires", "-1");
            httpResp.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
        } else if (isCachableContentType(contentType) || isCachableResource(httpReq)) {
            Calendar inTwoMonths = Calendar.getInstance();
            inTwoMonths.add(Calendar.MONTH, 2);
            httpResp.setDateHeader("Expires", inTwoMonths.getTimeInMillis());
        } else {
            httpResp.setHeader("Expires", "-1");
            httpResp.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
        }

        filterChain.doFilter(servletRequest, servletResponse);
    }

    private boolean isDontCacheHtmlPages(HttpServletRequest httpReq) {
        String requestURI = httpReq.getRequestURI();
        return Arrays.binarySearch(DONT_CACHE_HTML_PAGES, requestURI.toLowerCase()) > -1;
    }

    private boolean isCachableResource(HttpServletRequest httpReq) {
        String requestURI = httpReq.getRequestURI();

        for (String iResource : CACHEABLE_RESOURCES_NAMES) {
            if (requestURI.endsWith(iResource)) {
                return true;
            }
        }
        return false;
    }

    private boolean isCachableContentType(String contentType) {
        return contentType != null && Arrays.binarySearch(CACHEABLE_CONTENT_TYPES, contentType.toLowerCase()) > -1;
    }

    @Override
    public void destroy() {

    }
}
