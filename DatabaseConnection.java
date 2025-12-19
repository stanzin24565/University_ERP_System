

package edu.univ.erp.data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;

public class DatabaseConnection {

    // Update with your database names
    private static final String AUTH_DB_URL =
            "jdbc:mysql://localhost:3306/university_auth_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";

    private static final String ERP_DB_URL =
            "jdbc:mysql://localhost:3306/university_erp_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";

    private static final String USERNAME = "root";
    private static final String PASSWORD = "stanzin1809";

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("✅ MySQL JDBC Driver loaded successfully");
        } catch (ClassNotFoundException e) {
            System.err.println("❌ MySQL JDBC Driver not found!");
            e.printStackTrace();
        }
    }

    // -----------------------------
    // AUTH DB CONNECTION
    // -----------------------------
    public static Connection getAuthConnection() throws SQLException {
        try {
            return DriverManager.getConnection(AUTH_DB_URL, USERNAME, PASSWORD);
        } catch (SQLException e) {
            System.err.println("❌ Failed to connect to Auth DB: " + e.getMessage());
            throw e;
        }
    }

    // -----------------------------
    // ERP DB CONNECTION
    // -----------------------------
    public static Connection getERPConnection() throws SQLException {
        try {
            return DriverManager.getConnection(ERP_DB_URL, USERNAME, PASSWORD);
        } catch (SQLException e) {
            System.err.println("❌ Failed to connect to ERP DB: " + e.getMessage());
            throw e;
        }
    }

    // -----------------------------
    // TEST BOTH DATABASES
    // -----------------------------
    public static void testConnections() {

        // ---- AUTH DB ----
        try (Connection conn = getAuthConnection()) {
            System.out.println("✅ Connected to Auth DB");
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) AS count FROM users_auth");

            if (rs.next()) {
                System.out.println("   Users in auth database: " + rs.getInt("count"));
            }

        } catch (Exception e) {
            System.err.println("❌ Auth DB test failed: " + e.getMessage());
            System.err.println("   ⚠ Make sure 'users_auth' table exists in univ_auth_db");
        }

        // ---- ERP DB ----
        try (Connection conn = getERPConnection()) {
            System.out.println("✅ Connected to ERP DB");
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) AS count FROM students");

            if (rs.next()) {
                System.out.println("   Students in ERP database: " + rs.getInt("count"));
            }

        } catch (Exception e) {
            System.err.println("❌ ERP DB test failed: " + e.getMessage());
            System.err.println("   ⚠ Make sure 'students' table exists in univ_erp_db");
        }
    }

    public static void simpleTest() {
        System.out.println("----- Testing Database Connections -----");

        // Test Auth DB
        try (Connection conn = getAuthConnection()) {
            if (conn.isValid(2)) {
                System.out.println("✅ Auth DB connection OK");
            } else {
                System.out.println("❌ Auth DB connection FAILED");
            }
        } catch (Exception e) {
            System.err.println("❌ Auth DB error: " + e.getMessage());
        }

        // Test ERP DB
        try (Connection conn = getERPConnection()) {
            if (conn.isValid(2)) {
                System.out.println("✅ ERP DB connection OK");
            } else {
                System.out.println("❌ ERP DB connection FAILED");
            }
        } catch (Exception e) {
            System.err.println("❌ ERP DB error: " + e.getMessage());
        }

        System.out.println("----------------------------------------");
    }



    public static void main(String[] args) {
//        testConnections();
        simpleTest();
    }
}

//package edu.univ.erp.data;
//
//import java.sql.Connection;
//import java.sql.DriverManager;
//import java.sql.SQLException;
//
//public class DatabaseConnection {
//
//    // Database URLs
//    private static final String AUTH_DB_URL =
//            "jdbc:mysql://localhost:3306/university_auth_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
//
//    private static final String ERP_DB_URL =
//            "jdbc:mysql://localhost:3306/university_erp_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
//
//    private static final String USERNAME = "root";
//    private static final String PASSWORD = "stanzin1809";
//
//    static {
//        try {
//            Class.forName("com.mysql.cj.jdbc.Driver");
//            System.out.println("✅ MySQL JDBC Driver loaded successfully");
//        } catch (ClassNotFoundException e) {
//            System.err.println("❌ MySQL JDBC Driver not found!");
//            e.printStackTrace();
//        }
//    }
//
//    // -----------------------------
//    // AUTH DB CONNECTION
//    // -----------------------------
//    public static Connection getAuthConnection() throws SQLException {
//        try {
//            return DriverManager.getConnection(AUTH_DB_URL, USERNAME, PASSWORD);
//        } catch (SQLException e) {
//            System.err.println("❌ Failed to connect to Auth DB: " + e.getMessage());
//            throw e;
//        }
//    }
//
//    // -----------------------------
//    // ERP DB CONNECTION
//    // -----------------------------
//    public static Connection getERPConnection() throws SQLException {
//        try {
//            return DriverManager.getConnection(ERP_DB_URL, USERNAME, PASSWORD);
//        } catch (SQLException e) {
//            System.err.println("❌ Failed to connect to ERP DB: " + e.getMessage());
//            throw e;
//        }
//    }
//
//    // -----------------------------
//    // SIMPLE TEST — NO TABLE QUERIES
//    // -----------------------------
//    public static void simpleTest() {
//        System.out.println("----- Testing Database Connections -----");
//
//        // Test Auth DB
//        try (Connection conn = getAuthConnection()) {
//            if (conn.isValid(2)) {
//                System.out.println("✅ Auth DB connection OK");
//            } else {
//                System.out.println("❌ Auth DB connection FAILED");
//            }
//        } catch (Exception e) {
//            System.err.println("❌ Auth DB error: " + e.getMessage());
//        }
//
//        // Test ERP DB
//        try (Connection conn = getERPConnection()) {
//            if (conn.isValid(2)) {
//                System.out.println("✅ ERP DB connection OK");
//            } else {
//                System.out.println("❌ ERP DB connection FAILED");
//            }
//        } catch (Exception e) {
//            System.err.println("❌ ERP DB error: " + e.getMessage());
//        }
//
//        System.out.println("----------------------------------------");
//    }
//
//    public static void main(String[] args) {
//        simpleTest();
//    }
//}
//
