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
import java.util.Random;

// TODO: add constraints to tables
// TODO: add modify w/ constaints to tables
// TODO: add delete w/ constaints to tables
public class DBMSSetup {

    static final boolean printDebug = false; // set to true to print debug messages

    // #region // * Main methods
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

    // #endregion Main methods
    // #region // * Table Creation
    // #region Tables creation consts
    static final String[] tableNames = new String[] {
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

    static final String[] tableCreateStatements = new String[] {
            // Member: memberID, name, phone#, email, dob, emergency contact
            "CREATE TABLE Member ("
                    + "memberID INTEGER, "
                    + "name VARCHAR(50), "
                    + "phone VARCHAR(20), " // 20 bc of international numbers
                    + "email VARCHAR(50), "
                    + "dob DATE, "
                    + "emergencyContact VARCHAR(50), "
                    + "PRIMARY KEY (memberID))",
            // Ski pass: skiPassID, price, timeOfPurchase, expDate, totalUses,
            // remainingUses, passType, status, memberID, rentalID
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
    private static int addMember(Connection dbconn, String name, String phone, String email, java.sql.Date dob,
            String emergencyContact) {
        int memberID = generateRandomID(dbconn, "Member", "memberID");

        try (PreparedStatement pstmt = dbconn.prepareStatement(
                "INSERT INTO Member VALUES (?, ?, ?, ?, ?, ?)")) {
            pstmt.setInt(1, memberID);
            pstmt.setString(2, name);
            pstmt.setString(3, phone);
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
            return -1; // error
        }
        return memberID; // Return the generated memberID
    }

    // * Ski pass: skiPassID, price, timeOfPurchase, expDate, totalUses,
    // remainingUses, passType, status, memberID, rentalID
    // adds a ski pass to the database
    private static int addSkiPass(Connection dbconn, int skiPassID, int price, java.sql.Date timeOfPurchase,
            java.sql.Date expDate,
            int totalUses, int remainingUses, String passType, String status, int memberID, int rentalID) {
        // int skiPassID = generateRandomID(dbconn, "SkiPass", "skiPassID");

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
            return -1;
        }
        return 0;
    }

    // * Lesson Order: lessonOrderID, memberID, lessonsPurchased, remainingSessions
    // adds a lesson order to the database
    private static int addLessonOrder(Connection dbconn, int lessonOrderID, int memberID, int lessonsPurchased,
            int remainingSessions) {
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
            return -1; // error
        }
        return 0;
    }

    // * Add equipment to the database
    // * Equipment: equipmentID, type, size, status
    public static int addEquipment(Connection conn, int equipmentID, String type, String size, String status) {
        try {
            // int equipmentID = generateRandomID(conn, "Equipment", "equipmentID"); //
            // generate ID for the new equipment

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
            return -1; // error
        }
        return 0;
    }

    // * Add gearRental to the database
    // * GearRental: rentalID, startDate, expDate, return status, status, skiPassID
    public static int addGearRental(Connection conn, int rentalID, java.sql.Date startDate,
            String returnStatus, String status, int skiPassID) {
        try {

            // int rentalID = generateRandomID(conn, "GearRental", "rentalID"); // generate
            // ID for the new gear rental
            String sql = "INSERT INTO GearRental (rentalID, startDate, returnStatus, status, skiPassID) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, rentalID);
                pstmt.setDate(2, startDate);
                pstmt.setString(3, returnStatus);
                pstmt.setString(4, status);
                pstmt.setInt(5, skiPassID);

                int rowsInserted = pstmt.executeUpdate();
                if (rowsInserted > 0) {
                    System.out.println("Successfully added gear rental with ID " + rentalID);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error adding gear rental: " + e.getMessage());
            return -1; // error
        }
        return 0;
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

    // * add lift to the database
    // * Lift: liftName, ability level, openTime, closeTime, status
    public static void addLift(Connection conn, String liftName, String abilityLevel, Time openTime, Time closeTime,
            String status) {
        try {
            String sql = "INSERT INTO Lift (liftName, abilityLevel, openTime, closeTime, status) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, liftName);
                pstmt.setString(2, abilityLevel);
                pstmt.setTime(3, openTime);
                pstmt.setTime(4, closeTime);
                pstmt.setString(5, status);

                int rowsInserted = pstmt.executeUpdate();
                if (rowsInserted > 0) {
                    System.out.println("Successfully added lift: " + liftName);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error adding lift: " + e.getMessage());
        }
    }

    // * add trail to the database
    // * Trail: trailName, location, difficulty, category, status
    public static void addTrail(Connection conn, String trailName, String location, String difficulty, String category,
            String status) {
        try {
            String sql = "INSERT INTO Trail (trailName, location, difficulty, category, status) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, trailName);
                pstmt.setString(2, location);
                pstmt.setString(3, difficulty);
                pstmt.setString(4, category);
                pstmt.setString(5, status);

                int rowsInserted = pstmt.executeUpdate();
                if (rowsInserted > 0) {
                    System.out.println("Successfully added trail: " + trailName);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error adding trail: " + e.getMessage());
        }
    }

    public static void addMyEntities(Connection dbconn) {
        // * John doe
        int mID1 = addMember(dbconn, "John Doe", "2344234234", "johnDoe@gmail.com", java.sql.Date.valueOf("1990-05-15"),
                "1234567890");

        // ski pass: skiPassID, price, timeOfPurchase, expDate, totalUses,
        // remainingUses, passType, status, memberID, rentalID
        // int skiPass1 = addSkiPass(dbconn, 300, java.sql.Date.valueOf("2025-3-01"),
        // java.sql.Date.valueOf("2026-3-01"), 10, 2,
        // "Season", "Active", mID1, -1);
        addMember(dbconn, "Gavin Borquez", "5202629618", "gavin.borquez@gmail.com", java.sql.Date.valueOf("2006-10-17"),
                "borquezgabriel@gmail.com");
        addMember(dbconn, "Ahen Dridman", "5120000100", "we.will.lose.wahwahwah@gmail.com",
                java.sql.Date.valueOf("2000-01-01"), "1234567890");
        addMember(dbconn, "Thegor Rilla", "5329999999", "a.whole.gorilla@gmail.com",
                java.sql.Date.valueOf("2000-01-01"), "1234567890");
        addMember(dbconn, "Andrew Johnson", "5206683030", "ajbecerra@arizona.edu", java.sql.Date.valueOf("2000-05-14"),
                "lmusngi@arizona.edu");
        addMember(dbconn, "Steve Jobs", "5551234567", "appleinc@gmail.com", java.sql.Date.valueOf("1957-01-01"),
                "Mike Wazowski");
        addMember(dbconn, "Dennis Rodman", "5552345678", "bulls96@gmail.com", java.sql.Date.valueOf("1970-01-01"),
                "Mike Wazowski");
        addMember(dbconn, "Stephen Curry", "5553456789", "chefCurry@gmail.com", java.sql.Date.valueOf("1987-01-01"),
                "Mike Wazowski");
        addMember(dbconn, "LeBron James", "5554567890", "kingJames@gmail.com", java.sql.Date.valueOf("1960-01-01"),
                "Mike Wazowski");
        addMember(dbconn, "Zebulon Powell", "5555678901", "steezLord@gmail.com", java.sql.Date.valueOf("2000-01-01"),
                "Mike Wazowski");

        // *
    }

    // * random entity generator
    // ------------------------------------------------------------------------------

    // rand param for seeding
    // * add random member to the database
    // * returns the memberID of the new member
    public static int addRandomMember(Connection dbconn, Random rand) {
        // member: memberID, name, phone#, email, dob, emergency contact
        // ski pass: skiPassID, price, timeOfPurchase, expDate, totalUses,
        // remainingUses, passType, status, memberID, rentalID
        // gear rental: rentalID, startDate, return status, status, skiPassID

        String name = randStr(rand, -1) + " " + randStr(rand, -1);

        String phone = String.format("%03d-%03d-%04d", rand.nextInt(1000), rand.nextInt(1000), rand.nextInt(10000));
        String email = randStr(rand, -1) + "@gmail.com";
        java.sql.Date dob = new java.sql.Date(
                System.currentTimeMillis() - rand.nextLong(100 * 365L * 24 * 60 * 60 * 1000)); // random date of birth
                                                                                               // 100 years ago
        String emergencyContact = String.format("%03d-%03d-%04d", rand.nextInt(1000), rand.nextInt(1000),
                rand.nextInt(10000));

        int memberID = addMember(dbconn, name, phone, email, dob, emergencyContact);
        if (memberID == -1) {
            System.err.println("Error adding member: " + name + ", " + phone + ", " + email + ", " + dob + ", "
                    + emergencyContact);
        }
        return memberID;
    }

    // * add random ski pass to the database
    // * returns the skiPassID of the new ski pass, or -1 on failure
    // ! isn't really a way to get the gear rentalID from the ski passID
    private static int addRandomSkiPass(Connection dbconn, Random rand, int memberID) {
        // ski pass: skiPassID, price, timeOfPurchase, expDate, totalUses,
        // remainingUses, passType, status, memberID, rentalID

        // * pass types
        // * in this system, punch cards don't expire and time limits can be used
        // unlimited times.
        // * therefore, we choose which one to use based on the pass type
        // type: punch card, time limit
        // punch card: 10 uses (80.00), 20 uses (150.00)
        // time limit: 1 day (80.00), 2 days (150.00), 3 days (200.00), 1 season
        // (1000.00)
        // status: active, inactive
        String passType = rand.nextBoolean() ? "Punch Card" : "Time Limit";
        String status = rand.nextBoolean() ? "Active" : "Inactive";

        int price = 0;
        java.sql.Date timeOfPurchase = new java.sql.Date(System.currentTimeMillis());
        java.sql.Date expDate = timeOfPurchase;
        int totalUses = 0;
        int remainingUses = 0;
        if (passType.equals("Punch Card")) {
            totalUses = rand.nextBoolean() ? 10 : 20;

            if (totalUses == 10) {
                price = 8000; // 80.00
            } else {
                price = 15000; // 150.00
            }

            if (status.equals("Active")) {
                remainingUses = rand.nextInt(totalUses) + 1; // random remaining uses between 1 and totalUses
            }
        } else {
            int limitType = rand.nextInt(4); // 0: 1 day, 1: 2 days, 2: 3 days, 3: 1 year // !(season?)
            long offset = 0; // offset for the expiration date in ms

            switch (limitType) {
                case 0 -> {
                    price = 8000; // 80.00
                    offset = 1L * 24 * 60 * 60 * 1000; // 1 day in milliseconds
                }
                case 1 -> {
                    price = 15000; // 150.00
                    offset = 2L * 24 * 60 * 60 * 1000; // 2 days in milliseconds
                }
                case 2 -> {
                    price = 20000; // 200.00
                    offset = 3L * 24 * 60 * 60 * 1000; // 3 days in milliseconds
                }
                case 3 -> {
                    price = 100000; // 1000.00
                    offset = 365L * 24 * 60 * 60 * 1000; // 1 year in milliseconds
                }
                default ->
                    throw new AssertionError();
            }

            if (status.equals("Active")) {
                timeOfPurchase = new java.sql.Date(System.currentTimeMillis() - rand.nextLong(offset)); // random date
                                                                                                        // up to offset
                                                                                                        // ago
            } else {
                // ! this can create rare cases in which a member holds multiple ski passes at
                // the same time some time in the past when inactive is called multiple times
                timeOfPurchase = new java.sql.Date(
                        System.currentTimeMillis() - offset - rand.nextLong(3L * 365 * 24 * 60 * 60 * 1000)); // random
                                                                                                              // inactive
                                                                                                              // date up
                                                                                                              // to 3
                                                                                                              // years
                                                                                                              // (+
                                                                                                              // offset)
                                                                                                              // ago
            }
            expDate = new java.sql.Date(timeOfPurchase.getTime() + offset); // expiration date is offset from current
                                                                            // time
        }

        int skiPassID = generateRandomID(dbconn, "SkiPass", "skiPassID"); // generate random ski pass ID
        int rentalID = -1; // rental ID is -1 by default
        if (rand.nextBoolean()) {
            rentalID = generateRandomID(dbconn, "GearRental", "rentalID"); // generate random rental ID

            int result = addRandomGearRental(dbconn, rentalID, rand, skiPassID, expDate, expDate);
            if (result == -1) {
                System.err.println("Error adding gear rental: " + rentalID + ", " + skiPassID);
                return -1;
            }
        }

        int result = addSkiPass(dbconn, skiPassID, price, timeOfPurchase, expDate, totalUses, remainingUses, passType,
                status,
                memberID, rentalID);

        if (result == -1) {
            System.err.println("Error adding ski pass: " + skiPassID + ", " + price + ", " + timeOfPurchase + ", "
                    + expDate + ", " + totalUses + ", " + remainingUses + ", " + passType + ", " + status);
            return -1;
        }
        return skiPassID;
    }

    // * add random gear rental to the database
    // * returns 0 on success, -1 on failure
    private static int addRandomGearRental(Connection dbconn, int rentalID, Random rand, int skiPassID,
            java.sql.Date skiStartDate, java.sql.Date skiExpDate) {
        // gear rental: rentalID, startDate, expDate, return status, status, skiPassID

        java.sql.Date startDate = new java.sql.Date(rand.nextLong(skiStartDate.getTime(),
                Math.min(System.currentTimeMillis(), skiExpDate.getTime()))); // random date between start date, and
                                                                              // min(now or skiExpDate)
        String returnStatus = rand.nextBoolean() ? "Returned" : "Not Returned"; // 50% chance of being returned

        String status = rand.nextInt(100) >= 5 ? "Active" : "Inactive"; // 5% chance of being inactive

        int result = addGearRental(dbconn, rentalID, startDate, returnStatus, status, skiPassID);
        if (result == -1) {
            System.err.println("Error adding gear rental: " + rentalID + ", " + startDate + ", " + returnStatus + ", "
                    + status + ", " + skiPassID);
            return -1;
        }
        return 0;
    }

    private static String randStr(Random rand, int length) {
        if (length == -1) {
            length = rand.nextInt(10) + 1; // generate random length between 4 and 10
        }

        char[] randomChars = new char[length];
        for (int i = 0; i < length; i++) {
            randomChars[i] = (char) ('a' + rand.nextInt(26)); // generate random lowercase letter
        }
        randomChars[0] = Character.toUpperCase(randomChars[0]); // capitalize the first letter
        return new String(randomChars);
    }
    // * random entity generator
    // ------------------------------------------------------------------------------

    // #endregion Populate tables

    // update region
    // * update member in the database
    public static boolean updateMember(Connection conn, int memberId, String newPhone, String newEmail,
            String newEmergencyContact) {
        String sql = "UPDATE Member SET phoneNumber = ?, email = ?, emergencyContact = ? WHERE memberID = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, Double.parseDouble(newPhone));
            pstmt.setString(2, newEmail);
            pstmt.setString(3, newEmergencyContact);
            pstmt.setInt(4, memberId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating member: " + e.getMessage());
            return false;
        }
    }

    public static boolean updateSkiPassUsage(Connection conn, int skiPassId, int newRemainingUses) {
        // Check if pass is valid
        String sqlCheck = "SELECT expirationDate, remainingUses FROM SkiPass WHERE skiPassID = ? AND expirationDate >= CURRENT_DATE";
        try (PreparedStatement pstmt = conn.prepareStatement(sqlCheck)) {
            pstmt.setInt(1, skiPassId); // get ski pass ID to update

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Date expirationDate = rs.getDate("expirationDate");
                    int remainingUses = rs.getInt("remainingUses");

                    // Check if the pass is valid
                    if (expirationDate.after(new java.util.Date()) && remainingUses >= 0) {
                        // update remaining uses
                        String updateSql = "UPDATE SkiPass SET remainingUses = ? WHERE skiPassID = ?";
                        try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                            updateStmt.setInt(1, newRemainingUses); // Set the new remaining uses value
                            updateStmt.setInt(2, skiPassId); // Set the ski pass ID
                            return updateStmt.executeUpdate() > 0; // Execute the update
                        }
                    } else {
                        System.out.println("Ski pass is either expired or has no remaining uses.");
                        return false;
                    }
                } else {
                    System.out.println("Ski pass not found.");
                    return false;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error updating ski pass usage: " + e.getMessage());
            return false;
        }
    }

    // Updates equipment type and size, and logs the change
    public static boolean updateEquipment(Connection conn, int equipmentId, String newType, String newSize) {
        String sql = "UPDATE Equipment SET type = ?, size = ? WHERE equipmentID = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newType);
            pstmt.setString(2, newSize);
            pstmt.setInt(3, equipmentId);

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                // Log the update in the equipmentUpdate table
                String note = String.format("Updated equipment type to '%s' and size to '%s'", newType, newSize);
                logEquipmentUpdate(conn, equipmentId, note);
                return true;
            } else {
                System.out.println("No equipment found with ID: " + equipmentId);
                return false;
            }
        } catch (SQLException e) {
            System.err.println("Error updating equipment: " + e.getMessage());
            return false;
        }
    }

    // * update lesson order in the database
    public static boolean updateLessonUsage(Connection conn, int lessonOrderId, int newRemainingSessions) {
        // Check if lesson order is valid
        String sql = "UPDATE LessonOrder SET remainingSessions = ? WHERE orderID = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, newRemainingSessions); // Set the new remaining sessions value
            pstmt.setInt(2, lessonOrderId); // Set the lesson order ID
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating lesson usage: " + e.getMessage());
            return false;
        }
    }

    // * update rental in the database
    public static boolean updateEquipmentRental(Connection conn, int rentalId, boolean isReturned) {
        String updateSql = "UPDATE EquipmentRental SET returnStatus = ? WHERE rentalID = ?";
        // Check if rental is valid
        try (PreparedStatement pstmt = conn.prepareStatement(updateSql)) {
            // get rental ID to update and set return status
            pstmt.setBoolean(1, isReturned);
            pstmt.setInt(2, rentalId);
            int rowsUpdated = pstmt.executeUpdate();
            // if updated successfully, log the change
            if (rowsUpdated > 0) {
                logGearRentalUpdate(conn, rentalId, "UPDATE", "Return status updated to " + isReturned);
                return true;
            } else {
                System.out.println("No rental record updated.");
                return false;
            }
        } catch (SQLException e) {
            System.err.println("Error updating equipment rental: " + e.getMessage());
            return false;
        }
    }

    // end region

    // delete region
    // * delete member in the database
    public static boolean deleteMember(Connection conn, int memberId) {
        try {
            // Check active ski passes
            String activeSkiPass = "SELECT COUNT(*) FROM SkiPass WHERE memberID = ? AND expirationDate >= CURRENT_DATE";
            String activeRental = "SELECT COUNT(*) FROM EquipmentRental WHERE memberID = ? AND returnStatus = 'not returned'";
            String activeLesson = "SELECT COUNT(*) FROM LessonOrder WHERE memberID = ? AND usedStatus = 'unused'";

            // dont close if ski pass, rental, or lesson is active
            if (hasOpenRecords(conn, activeSkiPass, memberId) || hasOpenRecords(conn, activeRental, memberId)
                    || hasOpenRecords(conn, activeLesson, memberId)) {
                System.out.println("Cannot delete member: active ski passes, open rentals, or unused lessons exist.");
                return false;
            }

            // Delete skiPass data, lift usage, rental history, and lesson history
            conn.setAutoCommit(false);
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("DELETE FROM LessonOrder WHERE memberID = " + memberId); // Delete lessons
                stmt.executeUpdate("DELETE FROM EquipmentRental WHERE memberID = " + memberId); // Delete rentals
                stmt.executeUpdate("DELETE FROM SkiPass WHERE memberID = " + memberId); // Delete ski passes
                stmt.executeUpdate("DELETE FROM Member WHERE memberID = " + memberId); // Delete member
                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                System.err.println("Error during deletion: " + e.getMessage());
                return false;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.err.println("Error checking/deleting member: " + e.getMessage());
            return false;
        }
    }

    public static boolean deleteSkiPass(Connection conn, int skiPassId) {
        // Check if pass is not expired and has remaining uses
        String checkPassSql = "SELECT expirationDate, remainingUses, status FROM SkiPass WHERE skiPassID = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(checkPassSql)) {
            pstmt.setInt(1, skiPassId); // Select the ski pass ID for review
            try (ResultSet res = pstmt.executeQuery()) {
                if (res.next()) {
                    Date expirationDate = res.getDate("expirationDate"); // Get the expiration date
                    int remainingUses = res.getInt("remainingUses"); // Get the remaining uses
                    String status = res.getString("status"); // Get the current status

                    // Check if the pass is already archived
                    if ("archived".equals(status)) {
                        System.out.println("Ski pass is already archived.");
                        return true; // No further action needed
                    }

                    // Check if the ski pass is still valid (has remaining uses or not expired)
                    if (remainingUses > 0 || expirationDate.after(new java.util.Date())) {
                        System.out.println(
                                "Ski pass cannot be archived. It is either still valid or has remaining uses.");
                        return false;
                    }

                    // Archive the ski pass by updating the status to 'archived'
                    String archiveSql = "UPDATE SkiPass SET status = 'archived' WHERE skiPassID = ?";
                    try (PreparedStatement archiveStmt = conn.prepareStatement(archiveSql)) {
                        archiveStmt.setInt(1, skiPassId);
                        int rowsUpdated = archiveStmt.executeUpdate();
                        return rowsUpdated > 0; // Return true if the pass was successfully archived
                    }
                } else {
                    System.out.println("Ski pass not found.");
                    return false;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error archiving ski pass: " + e.getMessage());
            return false;
        }
    }

    public static boolean deleteEquipment(Connection conn, int equipmentId) {
        // Check if equipment is rented or reserved
        String checkSql = "SELECT rentalStatus FROM EquipmentInventory WHERE equipmentID = ?";
        try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            checkStmt.setInt(1, equipmentId);
            // Check if the equipment is rented or reserved
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next()) {
                    String status = rs.getString("rentalStatus");
                    if ("rented".equalsIgnoreCase(status) || "reserved".equalsIgnoreCase(status)) {
                        System.out.println("Cannot delete equipment: it is currently rented or reserved.");
                        return false;
                    }

                    // Mark equipment as archived
                    String archiveSql = "UPDATE EquipmentInventory SET status = 'archived' WHERE equipmentID = ?";
                    try (PreparedStatement archiveStmt = conn.prepareStatement(archiveSql)) {
                        archiveStmt.setInt(1, equipmentId);
                        int updated = archiveStmt.executeUpdate();
                        if (updated > 0) {
                            logEquipmentUpdate(conn, equipmentId, "Equipment marked as archived.");
                            return true;
                        } else {
                            System.out.println("Equipment deletion failed.");
                            return false;
                        }
                    }
                } else {
                    System.out.println("Equipment not found.");
                    return false;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error deleting equipment: " + e.getMessage());
            return false;
        }
    }

    // * delete lesson order in the database
    public static boolean deleteLessonOrder(Connection conn, int OrderId) {
        String checkSql = "SELECT remainingSessions, totalSessions FROM LessonOrder WHERE orderID = ?";
        // compare remaining sessions to total sessions
        // if remaining sessions < total sessions, return false
        // else delete the lesson order
        try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            checkStmt.setInt(1, OrderId);
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next()) {
                    int remaining = rs.getInt("remainingSessions");
                    int total = rs.getInt("totalSessions");
                    // check if remaining sessions are less than total sessions
                    if (remaining < total) {
                        System.out.println("Cannot delete: some sessions have already been used.");
                        return false;
                    }
                    // lessons purchased are not used, so delete the lesson order
                    String deleteSql = "DELETE FROM LessonOrder WHERE orderID = ?";
                    try (PreparedStatement deleteStmt = conn.prepareStatement(deleteSql)) {
                        deleteStmt.setInt(1, OrderId);
                        return deleteStmt.executeUpdate() > 0;
                    }
                } else {
                    System.out.println("Lesson Order not found.");
                    return false;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error deleting lesson Order: " + e.getMessage());
            return false;
        }
    }

    // * delete rental in the database
    public static boolean deleteEquipmentRental(Connection conn, int rentalId) {
        String checkSql = "SELECT returnStatus FROM EquipmentRental WHERE rentalID = ?";
        try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            checkStmt.setInt(1, rentalId);
            ResultSet rs = checkStmt.executeQuery();
            // check if rental has been used
            if (rs.next()) {
                boolean isReturned = rs.getBoolean("returnStatus");
                if (isReturned) {
                    System.out.println("Cannot delete rental: equipment has already been returned/used.");
                    return false;
                }
            } else {
                System.out.println("Rental record not found.");
                return false;
            }
        } catch (SQLException e) {
            System.err.println("Error checking rental status: " + e.getMessage());
            return false;
        }

        // Proceed to delete
        String deleteSql = "DELETE FROM EquipmentRental WHERE rentalID = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(deleteSql)) {
            pstmt.setInt(1, rentalId);
            int rowsDeleted = pstmt.executeUpdate();

            if (rowsDeleted > 0) {
                logGearRentalUpdate(conn, rentalId, "DELETE", "Rental record deleted.");
                return true;
            } else {
                System.out.println("No rental record deleted.");
                return false;
            }
        } catch (SQLException e) {
            System.err.println("Error deleting equipment rental: " + e.getMessage());
            return false;
        }
    }

    // end region

    // * update/delete helpers

    // checks if the member has open records in the database
    // returns true if there are open records, false otherwise
    private static boolean hasOpenRecords(Connection conn, String sql, int memberId) throws SQLException {
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, memberId); // Set the member ID in the prepared statement
            try (ResultSet res = pstmt.executeQuery()) {
                res.next();
                return res.getInt(1) > 0; // Check if there are any open records
            }
        }
    }

    // Logs the equipment update in the equipmentUpdate table
    public static void logEquipmentUpdate(Connection conn, int equipmentId, String note) {
        String sql = "INSERT INTO equipmentUpdate (equipmentID, updateDate, notes) VALUES (?, CURRENT_DATE, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, equipmentId); // Set the equipment ID
            pstmt.setString(2, note); // Set the note for the update
            pstmt.executeUpdate(); // update changelog
        } catch (SQLException e) {
            System.err.println("Error logging equipment update: " + e.getMessage());
        }
    }

    // * Logs the gear rental update in the gearRentalUpdate table
    // * rentalID, type, notes, number
    public static void logGearRentalUpdate(Connection conn, int rentalId, String type, String notes) {
        String sql = "INSERT INTO gearRentalUpdate (rentalID, type, notes, number) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, rentalId);
            pstmt.setString(2, type);
            pstmt.setString(3, notes);
            pstmt.setInt(4, 1); // or some logic to determine version or change number
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error logging gear rental update: " + e.getMessage());
        }
    }

}
