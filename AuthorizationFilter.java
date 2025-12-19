package edu.univ.erp.auth;

import edu.univ.erp.domain.User;
import edu.univ.erp.util.Logger;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

public class AuthorizationFilter implements Filter {
    private AuthenticationService authService;
    private Logger logger;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        this.authService = new AuthenticationService();
        this.logger = Logger.getInstance();
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        User currentUser = (User) httpRequest.getAttribute("currentUser");
        String path = httpRequest.getRequestURI();
        String method = httpRequest.getMethod();

        if (currentUser == null) {
            chain.doFilter(request, response);
            return;
        }

        if (!isAuthorized(currentUser, path, method)) {
            logger.warn("Access denied for user: " + currentUser.getUsername() +
                    " → " + method + " " + path);
            httpResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
            httpResponse.getWriter().write("{\"error\": \"Access denied\"}");
            return;
        }

        logger.debug("User authorized: " + currentUser.getUsername() + " → " + method + " " + path);
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // Nothing to clean up
    }

    private boolean isAuthorized(User user, String path, String method) {
        String role = user.getRole();

        // Admins have full access
        if ("ADMIN".equals(role)) return true;

        // Instructors
        if (path.startsWith("/api/instructor")) {
            return "INSTRUCTOR".equals(role) || "ADMIN".equals(role);
        }

        // Students
        if (path.startsWith("/api/student")) {
            return "STUDENT".equals(role) || "INSTRUCTOR".equals(role) || "ADMIN".equals(role);
        }

        // Restrict admin endpoints
        if (path.startsWith("/api/admin")) return false;

        // Course management restrictions
        if (path.startsWith("/api/courses") && "POST".equals(method)) {
            return "ADMIN".equals(role) || "INSTRUCTOR".equals(role);
        }

        // Grades restrictions
        if (path.startsWith("/api/grades") &&
                ("POST".equals(method) || "PUT".equals(method))) {
            return "ADMIN".equals(role) || "INSTRUCTOR".equals(role);
        }

        // Default allow
        return true;
    }
}
