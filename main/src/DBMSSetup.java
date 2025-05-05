// Authors: Luckie Musngi and Aj Becerra

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBMSSetup {

    public static void main(String[] args) throws Exception {
        Connection dbconn = getDbconn(); // connect to the database
    }

    // * gets and returns a connection to the database
    private static Connection getDbconn() {
        final String oracleURL = // Magic lectura -> aloe access spell
                "jdbc:oracle:thin:@aloe.cs.arizona.edu:1521:oracle";

        String username = null, // Oracle DBMS username
                password = null; // Oracle DBMS password

        try (java.util.Scanner scanner = new java.util.Scanner(System.in)) {
            System.out.print("Enter your Oracle DBMS username: ");
            username = scanner.nextLine();
            System.out.print("Enter your Oracle DBMS password: ");
            password = scanner.nextLine();
        } catch (Exception e) {
            System.err.println("*** Exception:  " + e.getMessage());
        }

        try {
            Class.forName("oracle.jdbc.OracleDriver");

        } catch (ClassNotFoundException e) {

            System.err.println("*** ClassNotFoundException:  "
                    + "Error loading Oracle JDBC driver.  \n"
                    + "\tPerhaps the driver is not on the Classpath?");
            System.exit(-1);
        }

        // make and return a database connection to the user's
        // Oracle database
        Connection dbconn = null;

        try {
            dbconn = DriverManager.getConnection(oracleURL, username, password);
        } catch (SQLException e) {

            System.err.println("*** SQLException:  "
                    + "Could not open JDBC connection.");
            System.err.println("\tMessage:   " + e.getMessage());
            System.err.println("\tSQLState:  " + e.getSQLState());
            System.err.println("\tErrorCode: " + e.getErrorCode());
            System.exit(-1);
        }

        return dbconn;
    }

    // * Table Creation
    // ---------------------------------------------------------------------------

    // Tables to be Made:
    String[] tableNames = new String[] {
            "skiPass",
            "equipment",
            "gearRentalUpdate",
            "lift",
            "lesson",
            "instructor",
            "lodge",
            "shuttle"
    };

    String[] tableCreationStatements = new String[] {
            // Ski Pass:
            "CREATE TABLE skiPass (" +
                    "  skiPassID INTEGER PRIMARY KEY," +
                    "  price INTEGER NOT NULL," +
                    "  timeOfPurchase TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "  expDate DATE NOT NULL," +
                    "  totalUses INTEGER," +
                    "  remainingUses INTEGER," +
                    "  passType VARCHAR2(20)," +
                    "  status VARCHAR2(10)," +
                    "  memberID INTEGER," +
                    "  rentalID INTEGER," +

                    ")",
            // equipment:
            "CREATE TABLE equipment (" +
                    "  equipmentID INTEGER PRIMARY KEY," +
                    "  type VARCHAR2(20) NOT NULL," +
                    "  size VARCHAR2(10)," +
                    "  status VARCHAR2(10)" +
                    ")",
            // gearRentalUpdate:
            "CREATE TABLE gearRentalUpdate (" +
                    "  rentalUpdateID INTEGER PRIMARY KEY," +
                    "  rentalID INTEGER NOT NULL," +
                    "  type VARCHAR2(20)," +
                    "  notes VARCHAR2(200)," +

                    ")",
            // lift
            "CREATE TABLE lift (" +
                    "  liftName VARCHAR2(25) PRIMARY KEY," +
                    "  abilityLevel VARCHAR2(15)," +
                    "  openTime VARCHAR2(5)," +
                    "  closeTime VARCHAR2(5)," +
                    "  status VARCHAR2(10)" +
                    ")",
            // lesson
            "CREATE TABLE lesson (" +
                    "  LessonID INTEGER PRIMARY KEY," +
                    "  lessonName VARCHAR2(25) NOT NULL," +
                    "  employeeID INTEGER," +

                    ")",
            // instructor
            "CREATE TABLE instructor (" +
                    "  EmployeeID INTEGER PRIMARY KEY," +
                    "  certificationLevel VARCHAR2(10)," +

                    ")",
            // lodge
            "CREATE TABLE lodge (" +
                    "  lodgeID INTEGER PRIMARY KEY," +
                    "  location VARCHAR2(30)," +
                    "  incomeSourceID INTEGER," +

                    ")",
            // Shuttle
            "CREATE TABLE shuttle (" +
                    "  ShuttleID INTEGER PRIMARY KEY," +
                    "  shuttleName VARCHAR2(20)," +
                    "  status VARCHAR2(10)" +
                    ")"
    };

    private static void makeTables(Connection dconn) {
    }

    // * Table Creation (End)
    // ---------------------------------------------------------------------
}
