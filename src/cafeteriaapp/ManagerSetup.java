package cafeteriaapp;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.mindrot.jbcrypt.BCrypt;

/**
 * Utility: call ManagerSetup.ensureManagerExists() on app startup
 * to seed the manager account if it doesn't exist yet.
 *
 * Manager credentials:
 *   Email:    manager@paucafe.com
 *   Password: Manager@123
 */
public class ManagerSetup {

    private static final String MANAGER_EMAIL = "manager@paucafe.com";
    private static final String MANAGER_PASSWORD = "Manager@123";

    public static void ensureManagerExists() {
        try (Connection conn = DatabaseConnection.getConnect()) {
            PreparedStatement check = conn.prepareStatement(
                    "SELECT id FROM users WHERE email = ?");
            check.setString(1, MANAGER_EMAIL);
            ResultSet rs = check.executeQuery();

            if (!rs.next()) {
                String hash = BCrypt.hashpw(MANAGER_PASSWORD, BCrypt.gensalt(10));
                PreparedStatement insert = conn.prepareStatement(
                        "INSERT INTO users (first_name, last_name, email, password, role, account_balance)"
                        + " VALUES (?,?,?,?,?,?)");
                insert.setString(1, "Cafeteria");
                insert.setString(2, "Manager");
                insert.setString(3, MANAGER_EMAIL);
                insert.setString(4, hash);
                insert.setString(5, "manager");
                insert.setDouble(6, 0.00);
                insert.executeUpdate();
                System.out.println("[Setup] Manager account created: " + MANAGER_EMAIL);
            }
        } catch (SQLException ex) {
            System.err.println("[Setup] Could not ensure manager exists: " + ex.getMessage());
        }
    }
}
