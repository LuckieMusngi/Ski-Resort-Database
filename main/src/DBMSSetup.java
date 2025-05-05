// Authors: Luckie Musngi and Aj Becerra

// * To compile and execute this program on lectura:
//  *
//  *   Add the Oracle JDBC driver to your CLASSPATH environment variable:
//  *
//  *         export CLASSPATH=/usr/lib/oracle/19.8/client64/lib/ojdbc8.jar:${CLASSPATH}
//  *     (or whatever shell variable set-up you need to perform to add the
//  *     JAR file to your Java CLASSPATH)
//  *
//  *   Compile this file:
//  *
//  *         javac JDBC.java
//  *
//  *   Finally, run the program:
//  *
//  *         java JDBC <oracle username> <oracle password>
import java.io.IOException;
import java.sql.*;

public class DBMSSetup {

    static final boolean printDebug = false; // set to true to print debug messages

// #region // * Main methods
    public static void main(String[] args) throws Exception {
        Connection dbconn = getDbconn(); // connect to the database
    }

    // * gets and returns a connection to the database
    private static Connection getDbconn() {
        final String oracleURL
                = // Magic lectura -> aloe access spell
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

// #endregion Main methods
// #region // * Table Creation
    // #region Tables creation consts
    static final String[] tableNames = new String[]{
        // main entities
        "Member",
        "SkiPass",
        "GearRental",
        "Equipment",
        "EquipmentUpdate",
        "GearRentalUpdate",
        "Trail",
        "Lift",
        "LessonOrder",
        "Lesson",
        "LessonSession",
        "Instructor",
        "Employee",
        "Lodge",
        "IncomeSource",
        "Shuttle",
        // relation entities
        "LessonToOrder",
        "TrailLift",
        "LiftPassUsage",
        "ShuttleLodge",
        "RentalEquipment",
        "EmployeeIncomeSource"
    };

    static final String[] tableCreateStatements = new String[]{
        // Member: memberID, name, phone#, email, dob, emergency contact
        "CREATE TABLE Member ("
        + "memberID INTEGER, "
        + "name VARCHAR(50), "
        + "email VARCHAR(50), "
        + "dob DATE, "
        + "emergencyContact VARCHAR(50), "
        + "PRIMARY KEY (memberID))",
        // Ski pass: skiPassID, price, timeOfPurchase, expDate, totalUses,
        // remainingUses. passType, status, memberID, rentalID
        "CREATE TABLE SkiPass ("
        + "skiPassID INTEGER PRIMARY KEY, "
        + "price INTEGER NOT NULL, "
        + "timeOfPurchase TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
        + "expDate DATE NOT NULL, "
        + "totalUses INTEGER, "
        + "remainingUses INTEGER, "
        + "passType VARCHAR2(20), "
        + "status VARCHAR2(10), "
        + "memberID INTEGER, "
        + "rentalID INTEGER)"
        + "FOREIGN KEY (memberID) REFERENCES Member(memberID), "
        + "FOREIGN KEY (rentalID) REFERENCES GearRental(rentalID))",
        // Gear Rental: rentalID, startDate, expDate, return status, status, skiPassID
        "CREATE TABLE GearRental ("
        + "rentalID INTEGER, "
        + "startDate DATE, "
        + "expDate DATE, "
        + "returnStatus VARCHAR(50), "
        + "status VARCHAR(50), "
        + "skiPassID INTEGER, "
        + "PRIMARY KEY (rentalID), "
        + "FOREIGN KEY (skiPassID) REFERENCES SkiPass(skiPassID))",
        // Equipment: EquipmentID, type, size, status
        "CREATE TABLE Equipment ("
        + "equipmentID INTEGER PRIMARY KEY, "
        + "type VARCHAR2(20) NOT NULL, "
        + "size VARCHAR2(10), "
        + "status VARCHAR2(10))"
        + "FOREIGN KEY (rentalID) REFERENCES gearRental(rentalID)",
        // Equipment Update: equipmentUpdateID, equipmentID, type, notes
        "CREATE TABLE EquipmentUpdate ("
        + "equipmentUpdateID INTEGER, "
        + "equipmentID INTEGER, "
        + "type VARCHAR(20), "
        + "notes VARCHAR(50), "
        + "PRIMARY KEY (equipmentUpdateID), "
        + "FOREIGN KEY (equipmentID) REFERENCES Equipment(equipmentID))",
        // Gear Rental Update: rentalUpdateID, rentalID, type, notes
        "CREATE TABLE GearRentalUpdate ("
        + "rentalUpdateID INTEGER, "
        + "rentalID INTEGER, "
        + "type VARCHAR(20), "
        + "notes VARCHAR(200), "
        + "PRIMARY KEY (rentalUpdateID), "
        + "FOREIGN KEY (rentalID) REFERENCES GearRental(rentalID))",
        // Trail: trailName, location, difficulty, category, status
        "CREATE TABLE Trail ("
        + "trailName VARCHAR(50), "
        + "location VARCHAR(50), "
        + "difficulty VARCHAR(20), "
        + "category VARCHAR(20), "
        + "status VARCHAR(10), "
        + "PRIMARY KEY (trailName))",
        // Lift: liftName, ability level, openTime, closeTime, status
        "CREATE TABLE Lift ("
        + "liftName VARCHAR2(25) PRIMARY KEY, "
        + "abilityLevel VARCHAR2(15), "
        + "openTime TIME, "
        + "closeTime TIME, "
        + "status VARCHAR2(10))",
        // LessonOrder: lessonOrderID, memberID, lessonsPurchased, remainingSessions
        "CREATE TABLE LessonOrder ("
        + "lessonOrderID INTEGER, "
        + "memberID INTEGER, "
        + "lessonsPurchased INTEGER, "
        + "remainingSessions INTEGER, "
        + "PRIMARY KEY (lessonOrderID), "
        + "FOREIGN KEY (memberID) REFERENCES Member(memberID))",
        // Lesson: LessonID, lessonName, EmployeeID
        "CREATE TABLE Lesson ("
        + "lessonID INTEGER PRIMARY KEY, "
        + "lessonName VARCHAR2(25) NOT NULL, "
        + "employeeID INTEGER)"
        + "FOREIGN KEY (employeeID) REFERENCES Instructor(employeeID))",
        // LessonSession: sessionID, date, startTime, endTime, lessonID
        "CREATE TABLE LessonSession ("
        + "sessionID INTEGER, "
        + "date DATE, "
        + "startTime TIME, "
        + "endTime TIME, "
        + "lessonID INTEGER, "
        + "PRIMARY KEY (sessionID), "
        + "FOREIGN KEY (lessonID) REFERENCES Lesson(lessonID))",
        // Instructor: EmployeeID, Certification level
        "CREATE TABLE Instructor ("
        + "employeeID INTEGER PRIMARY KEY, "
        + "certificationLevel VARCHAR2(10))"
        + "FOREIGN KEY (employeeID) REFERENCES Employee(employeeID))",
        // Employee: employeeID, name, age, sex, race, monthly salary, job title
        "CREATE TABLE Employee ("
        + "employeeID INTEGER, "
        + "name VARCHAR(50), "
        + "age INTEGER, "
        + "sex VARCHAR(20), "
        + "race VARCHAR(20), "
        + "monthlySalary INTEGER, "
        + "jobTitle VARCHAR(50), "
        + "PRIMARY KEY (employeeID))",
        // Lodge: lodgeID, location
        "CREATE TABLE Lodge ("
        + "lodgeID INTEGER PRIMARY KEY, "
        + "location VARCHAR2(30))"
        + "FOREIGN KEY (lodgeID) REFERENCES IncomeSource(lodgeID))",
        // IncomeSource: sourceID, day, lodgeID, sourceName, dailyIncome
        "CREATE TABLE IncomeSource ("
        + "sourceID INTEGER, "
        + "day DATE, "
        + "lodgeID INTEGER, "
        + "sourceName VARCHAR(50), "
        + "dailyIncome INTEGER, "
        + "PRIMARY KEY (sourceID), "
        + "FOREIGN KEY (lodgeID) REFERENCES Lodge(lodgeID))",
        // Shuttle: shuttleID, location, capacity, status
        "CREATE TABLE Shuttle ("
        + "shuttleID INTEGER PRIMARY KEY, "
        + "shuttleName VARCHAR2(20), "
        + "status VARCHAR2(10))",
        // LessonToOrder: lessonID, lessonOrderID
        "CREATE TABLE LessonToOrder ("
        + "lessonID INTEGER, "
        + "lessonOrderID INTEGER, "
        + "PRIMARY KEY (lessonID, lessonOrderID), "
        + "FOREIGN KEY (lessonID) REFERENCES Lesson(lessonID), "
        + "FOREIGN KEY (lessonOrderID) REFERENCES LessonOrder(lessonOrderID))",
        // TrailLift: trailName, liftName
        "CREATE TABLE TrailLift ("
        + "trailName VARCHAR(25), "
        + "liftName VARCHAR(25), "
        + "PRIMARY KEY (trailName, liftName), "
        + "FOREIGN KEY (trailName) REFERENCES Trail(trailName), "
        + "FOREIGN KEY (liftName) REFERENCES Lift(liftName))",
        // LiftPassUsage: skiPassID, liftName, dateUsed, timeUsed
        "CREATE TABLE LiftPassUsage ("
        + "skiPassID INTEGER, "
        + "liftName VARCHAR(25), "
        + "dateUsed DATE, "
        + "timeUsed TIME, "
        + "PRIMARY KEY (skiPassID, liftName, dateUsed), "
        + "FOREIGN KEY (skiPassID) REFERENCES SkiPass(skiPassID), "
        + "FOREIGN KEY (liftName) REFERENCES Lift(liftName))",
        // ShuttleLodge: shuttleID, lodgeID
        "CREATE TABLE ShuttleLodge ("
        + "shuttleID INTEGER, "
        + "lodgeID INTEGER, "
        + "PRIMARY KEY (shuttleID, lodgeID), "
        + "FOREIGN KEY (shuttleID) REFERENCES Shuttle(shuttleID), "
        + "FOREIGN KEY (lodgeID) REFERENCES Lodge(lodgeID))",
        // RentalEquipment: rentalID, equipmentID
        "CREATE TABLE RentalEquipment ("
        + "rentalID INTEGER, "
        + "equipmentID INTEGER, "
        + "PRIMARY KEY (rentalID, equipmentID), "
        + "FOREIGN KEY (rentalID) REFERENCES GearRental(rentalID), "
        + "FOREIGN KEY (equipmentID) REFERENCES Equipment(equipmentID))",
        // EmployeeIncomeSource: employeeID, sourceID, day
        "CREATE TABLE EmployeeIncomeSource ("
        + "employeeID INTEGER, "
        + "sourceID INTEGER, "
        + "day DATE, "
        + "PRIMARY KEY (employeeID, sourceID, day), "
        + "FOREIGN KEY (employeeID) REFERENCES Employee(employeeID), "
        + "FOREIGN KEY (sourceID) REFERENCES IncomeSource(sourceID))"
    };
    // #endregion Tables creation consts

    private static void makeTables(Connection dbconn) {
        for (int i = 0; i < tableNames.length; i++) {
            String tableName = tableNames[i];
            String createTableSQL = tableCreateStatements[i];

            // makes the current table
            try {
                Statement stmt = dbconn.createStatement();
                stmt.executeUpdate(createTableSQL);
            } catch (SQLException e) {
                System.err.println("Error: couldn't INIT table " + tableName + ": " + e.getMessage());
                System.exit(-1);
            }

            // Grant SELECT permission on the table to PUBLIC
            String grantSelectSQL = String.format("GRANT SELECT ON %s TO PUBLIC", tableName);
            try {
                Statement stmt = dbconn.createStatement();
                stmt.executeUpdate(grantSelectSQL);
                // System.out.println("Granted SELECT on table: " + tableName);
            } catch (SQLException e) {
                System.err.println("Error granting SELECT on table " + tableName + ": " + e.getMessage());
            }
        }
    }

// #endregion Table Creation
// #region // * Add/Update/Delete
    // * Member: memberID, name, phone#, email, dob, emergency contact
    // adds a member to the database
    private static void addMember(Connection dbconn, String name, int phone, String email, java.sql.Date dob,
            String emergencyContact) {
        int memberID = generateRandomID(dbconn, "Member", "memberID");

        try (PreparedStatement pstmt = dbconn.prepareStatement(
                "INSERT INTO Member VALUES (?, ?, ?, ?, ?, ?)")) {
            pstmt.setInt(1, memberID);
            pstmt.setString(2, name);
            pstmt.setInt(3, phone);
            pstmt.setString(4, email);
            pstmt.setDate(5, dob);
            pstmt.setString(6, emergencyContact);

            pstmt.executeUpdate();

            if (printDebug) {
                System.out.println("Member added: " + memberID + ", " + name + ", " + email + ", " + dob + ", "
                        + emergencyContact);
            }
        } catch (SQLException e) {
            System.err.println("Error adding member: " + e.getMessage());
        }
    }

    // * Ski pass: skiPassID, price, timeOfPurchase, expDate, totalUses,
    // remainingUses, passType, status, memberID, rentalID
    // adds a ski pass to the database
    private static void addSkiPass(Connection dbconn, int price, java.sql.Date timeOfPurchase, java.sql.Date expDate,
            int totalUses, int remainingUses, String passType, String status, int memberID, int rentalID) {
        int skiPassID = generateRandomID(dbconn, "SkiPass", "skiPassID");

        try (PreparedStatement pstmt = dbconn.prepareStatement(
                "INSERT INTO SkiPass VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
            pstmt.setInt(1, skiPassID);
            pstmt.setInt(2, price);
            pstmt.setDate(3, timeOfPurchase);
            pstmt.setDate(4, expDate);
            pstmt.setInt(5, totalUses);
            pstmt.setInt(6, remainingUses);
            pstmt.setString(7, passType);
            pstmt.setString(8, status);
            pstmt.setInt(9, memberID);
            pstmt.setInt(10, rentalID);

            pstmt.executeUpdate();

            if (printDebug) {
                System.out.println("SkiPass added: " + skiPassID + ", " + price + ", " + expDate + ", " + totalUses
                        + ", " + remainingUses + ", " + passType + ", " + status + ", " + memberID + ", " + rentalID);
            }
        } catch (SQLException e) {
            System.err.println("Error adding SkiPass: " + e.getMessage());
        }
    }

    // * Lesson Order: lessonOrderID, memberID, lessonsPurchased, remainingSessions
    // adds a lesson order to the database
    private static void addLessonOrder(Connection dbconn, int memberID, int lessonsPurchased, int remainingSessions) {
        int lessonOrderID = generateRandomID(dbconn, "LessonOrder", "lessonOrderID");

        try (PreparedStatement pstmt = dbconn.prepareStatement(
                "INSERT INTO LessonOrder VALUES (?, ?, ?, ?)")) {
            pstmt.setInt(1, lessonOrderID);
            pstmt.setInt(2, memberID);
            pstmt.setInt(3, lessonsPurchased);
            pstmt.setInt(4, remainingSessions);

            pstmt.executeUpdate();

            if (printDebug) {
                System.out.println("LessonOrder added: " + lessonOrderID + ", " + memberID + ", " + lessonsPurchased
                        + ", " + remainingSessions);
            }
        } catch (SQLException e) {
            System.err.println("Error adding LessonOrder: " + e.getMessage());
        }
    }

    // * Add equipment to the database
    // * Equipment: equipmentID, type, size, status
    public static void addEquipment(Connection conn, String type, String size, String status) {
        try {
            int equipmentID = generateRandomID(conn, "Equipment", "equipmentID"); // generate ID for the new equipment

            String sql = "INSERT INTO Equipment (equipmentID, type, size, status) VALUES (?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, equipmentID);
                pstmt.setString(2, type);
                pstmt.setString(3, size);
                pstmt.setString(4, status);

                int rowsInserted = pstmt.executeUpdate();
                if (rowsInserted > 0) {
                    System.out.println("Successfully added equipment with ID " + equipmentID);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error adding equipment: " + e.getMessage());
        }
    }

    // * Add gearRental to the database
    // * GearRental: rentalID, startDate, expDate, return status, status, skiPassID
    public static void addGearRental(Connection conn, int rentalID, java.sql.Date startDate, java.sql.Date expDate,
            String returnStatus, String status, int skiPassID) {
        try {

            rentalID = generateRandomID(conn, "GearRental", "rentalID"); // generate ID for the new gear rental

            String sql = "INSERT INTO GearRental (rentalID, startDate, expDate, returnStatus, status, skiPassID) VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, rentalID);
                pstmt.setDate(2, startDate);
                pstmt.setDate(3, expDate);
                pstmt.setString(4, returnStatus);
                pstmt.setString(5, status);
                pstmt.setInt(6, skiPassID);

                int rowsInserted = pstmt.executeUpdate();
                if (rowsInserted > 0) {
                    System.out.println("Successfully added gear rental with ID " + rentalID);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error adding gear rental: " + e.getMessage());
        }
    }

    
    // * generate a random ID for a table (columnName should be the ID column)
    // this is a helper for the add methods
    private static int generateRandomID(Connection dbconn, String tableName, String columnName) {
        int i = 0;
        while (true) {
            int randomID = new java.util.Random().nextInt(100000); // Generate random ID

            // counts how many already have that ID
            // count everything from tableName where columnName = randomID
            try (PreparedStatement checkStmt = dbconn.prepareStatement("SELECT COUNT(*) FROM ? WHERE ? = ?")) {
                checkStmt.setString(1, tableName);
                checkStmt.setString(2, columnName);
                checkStmt.setInt(3, randomID);

                // execute and get result
                ResultSet rs = checkStmt.executeQuery();

                // if unique, return it
                if (rs.next() && rs.getInt(1) == 0) {
                    return randomID; // ID is unique, return it
                }

                // after 1000 tries, return -1
                i++;
                if (i > 1000) {
                    System.err.println("Error: Unable to generate a unique ID for table (1000 tries in)" + tableName);
                    return -1; // Return -1 if unable to generate a unique ID after 1000 attempts
                }
            } catch (SQLException e) {
                System.err.println("Error checking ID uniqueness for table " + tableName + ": " + e.getMessage());
                return -1; // Return -1 in case of an error
            }
        }
    }

// #endregion Add/Update/Delete
// #region // * Populate tables


// * the goat: adds a row to the table with the given name and values
// doesn't work on lift and trail tables (bc. no id primary key)
private static int addToTable(Connection dbconn, String tableName, String idName, String[] values) {
    int id = generateRandomID(dbconn, tableName, idName); // Generate a random ID for the new entry

    String sql = "INSERT INTO " + tableName + " VALUES (" + id + ", " + String.join(", ", values) + ")";
    try (Statement stmt = dbconn.createStatement()) {
        stmt.executeUpdate(sql);
        return id; // Return the generated ID
    } catch (SQLException e) {
        System.err.println("Error adding to table " + tableName + ": " + e.getMessage());
        return -1; // Return -1 in case of an error
    }
}


// #endregion Populate tables
}
