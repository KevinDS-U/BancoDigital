import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
//conecta la Bd a java
public class DBConnection {
    private static final String URL = "jdbc:sqlserver://127.0.0.1:1433;databaseName=BANCO_DIGITAL ;encrypt=true;trustServerCertificate=true";//cambiar prueba a BANCO_DIGITAL
    private static final String USER = "P01bd";
    private static final String PASSWORD = "utpya";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
    //Prueba jajjajajja

}
