package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class UpgradeConditionGenerator {

    // Define your database connection parameters
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/clicker_service"; // Replace with your DB URL
    private static final String USER = "your_username"; // Replace with your DB username
    private static final String PASS = "your_password"; // Replace with your DB password

    public static class Upgrade {
        private int id;
        private String name;
        private int level;
        private double cost;

        public Upgrade(int id, String name, int level, double cost) {
            this.id = id;
            this.name = name;
            this.level = level;
            this.cost = cost;
        }

        @Override
        public String toString() {
            return "Upgrade{" +
                    "id=" + id +
                    ", name='" + name + '\'' +
                    ", level=" + level +
                    ", cost=" + cost +
                    '}';
        }
    }

    // Function to retrieve all upgrades
    public static List<Upgrade> getAllUpgrades() {
        List<Upgrade> upgrades = new ArrayList<>();

        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            // Register JDBC driver for PostgreSQL
            Class.forName("org.postgresql.Driver");

            // Open a connection
            conn = DriverManager.getConnection(DB_URL, USER, PASS);

            // Execute a query
            stmt = conn.createStatement();
            String sql = "SELECT * FROM upgrades";
            rs = stmt.executeQuery(sql);

            // Extract data from result set
            while (rs.next()) {
                // Retrieve by column name
                int id = rs.getInt("id");
                String name = rs.getString("name");
                int level = rs.getInt("level");
                double cost = rs.getDouble("cost");

                // Create a new Upgrade object and add it to the list
                Upgrade upgrade = new Upgrade(id, name, level, cost);
                upgrades.add(upgrade);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Clean up the environment
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return upgrades;
    }

    public static void main(String[] args) {
        List<Upgrade> upgrades = getAllUpgrades();
        for (Upgrade upgrade : upgrades) {
            System.out.println(upgrade);
        }
    }

}
