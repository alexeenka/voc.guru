package guru.h4t_eng.logs;

import guru.h4t_eng.security.auth_servlet.AbstractAuthenticationServlet;
import org.slf4j.Logger;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * PerformanceLogFilter.
 *
 * Created by Alexey Alexeenka on 05.07.2015.
 */
@WebFilter(filterName = "performanceLogFilter")
public class PerformanceLogFilter implements Filter {

    private static final Logger LOG = AppLoggerFactory.getPerformanceLog(PerformanceLogFilter.class);

    public void init(FilterConfig filterConfig) throws ServletException {
    }

    public void doFilter(
            ServletRequest request,
            ServletResponse response,
            FilterChain chain
    )
            throws ServletException, IOException {

        long startTime = System.currentTimeMillis();
        chain.doFilter(request, response);
        long elapsed = System.currentTimeMillis() - startTime;

        String path = "unknown_path";
        String ip = "unknown_ip";
        if (request instanceof HttpServletRequest) {
            path = ((HttpServletRequest) request).getRequestURI();
            ip = AbstractAuthenticationServlet.getClientIpAddress((HttpServletRequest) request);
        }

        if (elapsed > 200) {
            LOG.info("IP [**]{}[**], path [**]{}[**] took [**]{}[**] ms ", ip, path, elapsed);
        }
    }

    public void destroy() {
    }
}
