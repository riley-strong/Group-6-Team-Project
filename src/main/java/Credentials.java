import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Credentials {
    static String userName = "TEAM_6";
    static String password = "HHwp5r2)|j";
    static String ipAddress = "45.79.55.190";
    static String portNumber = "3306";
    static String databaseName = "TEAM_6";

    public static QueryMaker databaseLogin() throws SQLException, ClassNotFoundException {
        return new QueryMaker(userName, password, ipAddress, portNumber, databaseName);

    }




}