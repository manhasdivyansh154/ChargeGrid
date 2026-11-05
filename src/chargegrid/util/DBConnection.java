package chargegrid.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/** Central place to obtain JDBC connections. Settings come from db.properties (optional). */
public final class DBConnection {
    private static String url = "jdbc:mysql://localhost:3306/chargegrid?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Kolkata";
    private static String user = "root";
    private static String password = "";
    private static boolean loaded = false;

    private DBConnection() { }

    private static synchronized void loadConfig() {
        if (loaded) return;
        loaded = true;
        File f = new File("db.properties");
        if (f.exists()) {
            try (FileInputStream in = new FileInputStream(f)) {
                Properties p = new Properties();
                p.load(in);
                url = p.getProperty("db.url", url);
                user = p.getProperty("db.user", user);
                password = p.getProperty("db.password", password);
            } catch (IOException e) {
                System.out.println("Could not read db.properties, using defaults.");
            }
        }
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("MySQL JDBC driver not found. Put mysql-connector-j-*.jar in the lib/ folder.");
        }
    }

    public static Connection getConnection() throws SQLException {
        loadConfig();
        return DriverManager.getConnection(url, user, password);
    }
}
