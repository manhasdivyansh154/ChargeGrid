package chargegrid;

import chargegrid.service.AppContext;
import chargegrid.ui.MainMenu;
import chargegrid.util.DBConnection;

import java.sql.Connection;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {
        System.out.println("Connecting to database...");
        try (Connection con = DBConnection.getConnection()) {
            System.out.println("Connected to " + con.getMetaData().getURL().split("\\?")[0]);
        } catch (SQLException e) {
            System.out.println("\nCould not connect to MySQL: " + e.getMessage());
            System.out.println("Checklist:");
            System.out.println("  1. MySQL server is running");
            System.out.println("  2. You ran sql/schema.sql to create the 'chargegrid' database");
            System.out.println("  3. db.properties has the right user/password");
            System.out.println("  4. lib/mysql-connector-j-*.jar is on the classpath");
            return;
        }
        new MainMenu(new AppContext()).show();
    }
}
