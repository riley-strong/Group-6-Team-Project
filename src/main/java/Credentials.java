import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/*
    Credentials class is used to establish database login
 */
public class Credentials {

    //Variable initialization - used in database login
    static String userName = "TEAM_6";
    static String password = "HHwp5r2)|j";
    static String ipAddress = "45.79.55.190";
    static String portNumber = "3306";
    static String databaseName = "TEAM_6";

    /**
     * returns new QueryMaker with the credentials needed for the database login
     *
     * @throws SQLException     provides information on a database access error.
     * @throws ClassNotFoundException thrown when it tries to load the class at runtime.
     */

    public static QueryMaker databaseLogin() throws SQLException, ClassNotFoundException {
        return new QueryMaker(userName, password, ipAddress, portNumber, databaseName);

    }




}

