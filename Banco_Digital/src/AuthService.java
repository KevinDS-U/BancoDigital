import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AuthService {

    public static String login(String username, String password) {
        String resultado = "Error al conectar con la base de datos.";

        try {
            // Paso 1: Obtener conexión
            Connection conn = DBConnection.getConnection();

            if (conn != null) {
                // Paso 2: Preparar llamada al procedimiento almacenado
                CallableStatement stmt = conn.prepareCall("{CALL sp_LoginValidate(?, ?)}");
                stmt.setString(1, username);
                stmt.setString(2, password);  // Ya no se hashea aquí

                // Paso 3: Ejecutar y leer resultado
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    resultado = rs.getString("Mensaje"); // ¡Este es el alias correcto!
                }

                rs.close();
                stmt.close();
                conn.close();
            }

        } catch (SQLException e) {
            resultado = "Error SQL: " + e.getMessage();
            e.printStackTrace();
        } catch (Exception e) {
            resultado = "Error: " + e.getMessage();
            e.printStackTrace();
        }

        return resultado;
    }
}