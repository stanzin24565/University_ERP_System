package edu.univ.erp.domain;

public class UserAuth {
    private int userId;
    private String username;
    private String role; // "ADMIN", "INSTRUCTOR", "STUDENT"
    private String passwordHash;
    private String status;
    // getters / setters / constructor(s)
    public UserAuth() {}
    public UserAuth(int userId, String username, String role, String passwordHash, String status) {
        this.userId = userId; this.username = username; this.role = role;
        this.passwordHash = passwordHash; this.status = status;
    }
    // getters/setters...
    // (Omitted for brevity in this preview; add standard getters and setters)
}
