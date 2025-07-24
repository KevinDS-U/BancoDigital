import java.sql.*;

public class AuthService {

    public enum LoginResult {
        SUCCESS, WRONG_CREDENTIALS, BLOCKED
    }

    public static LoginResult login(String username, String password) {
        try (Connection conn = DBConnection.getConnection()) {
            CallableStatement stmt = conn.prepareCall("{call sp_LoginValidate(?, ?, ?, ?)}");

            stmt.setString(1, username);
            stmt.setString(2, password);

            stmt.registerOutParameter(3, Types.INTEGER);
            stmt.registerOutParameter(4, Types.VARCHAR);

            stmt.execute();

            int result = stmt.getInt(3);

            return switch (result) {
                case 0 -> LoginResult.SUCCESS;
                case 1 -> LoginResult.WRONG_CREDENTIALS;
                case 2 -> LoginResult.BLOCKED;
                default -> LoginResult.WRONG_CREDENTIALS;
            };

        } catch (Exception e) {
            e.printStackTrace();
            return LoginResult.WRONG_CREDENTIALS;
        }
    }
}