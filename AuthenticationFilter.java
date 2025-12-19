package edu.univ.erp.auth;

import edu.univ.erp.domain.User;
import edu.univ.erp.util.Logger;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

public class AuthenticationFilter implements Filter {
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

        String path = httpRequest.getRequestURI();

        // Skip authentication for public endpoints
        if (isPublicEndpoint(path)) {
            chain.doFilter(request, response);
            return;
        }

        // Extract Bearer token
        String token = extractToken(httpRequest);
        if (token == null || !JWTTokenUtil.validateToken(token)) {
            logger.warn("Authentication failed: missing or invalid token for path " + path);
            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            httpResponse.getWriter().write("{\"error\": \"Authentication required\"}");
            return;
        }

        // Verify token + user
        Integer userId = JWTTokenUtil.getUserIdFromToken(token);
        if (userId == null || !authService.isValidSession(userId)) {
            logger.warn("Invalid session for token in path " + path);
            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            httpResponse.getWriter().write("{\"error\": \"Invalid or expired session\"}");
            return;
        }

        // Load user into request
        User user = authService.getUserById(userId);
        if (user != null) {
            httpRequest.setAttribute("currentUser", user);
        }

        logger.debug("Authenticated request by user: " + (user != null ? user.getUsername() : "unknown"));
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // No cleanup needed
    }

    // --- Helpers ---
    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return request.getParameter("token");
    }

    private boolean isPublicEndpoint(String path) {
        return path.equals("/api/auth/login") ||
                path.equals("/api/auth/register") ||
                path.startsWith("/public/") ||
                path.equals("/health") ||
                path.startsWith("/swagger") ||
                path.startsWith("/v3/api-docs");
    }
}
