package edu.univ.erp.access;

import edu.univ.erp.domain.User;

public class SessionManager {
    private static SessionManager instance;
    private User currentUser;
    private long loginTime;

    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public void login(User user) {
        this.currentUser = user;
        this.loginTime = System.currentTimeMillis();

        // Log login activity
        System.out.println("User logged in: " + user.getUsername() + " (" + user.getRole() + ")");
    }

    public void logout() {
        if (currentUser != null) {
            System.out.println("User logged out: " + currentUser.getUsername());
        }
        this.currentUser = null;
        this.loginTime = 0;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public boolean hasRole(String role) {
        return currentUser != null && role.equals(currentUser.getRole());
    }

    public long getSessionDuration() {
        return loginTime > 0 ? System.currentTimeMillis() - loginTime : 0;
    }
}
