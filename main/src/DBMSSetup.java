// Authors: Luckie Musngi and Aj Becerra
// This sets up the database tables and populates them with data
// this is expected to be run before interface.java

import java.sql.*;
import java.util.Random;

public class DBMSSetup {
    static final boolean printDebug = false; // set to true to print debug messages

    // #region // * Main methods
    public static void main(String[] args) throws Exception {
        Connection dbconn = getDbconn(); // connect to the database

        forceDropTables(dbconn); // drop all tables in the database
        makeTables(dbconn); // create tables in the database
        addForeignKeys(dbconn); // add foreign key constraints

        populateTables(dbconn);

        // printTableContents(dbconn, "Equipment"); // print the equipment table
        // printTableContents(dbconn, "Member"); // print member table
        // printTableContents(dbconn, "SkiPass"); // print skipass table

        printTupleCounts(dbconn); // print the number of tuples in each table
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
    
    // * prints out the number of tuples in each table
    private static void printTupleCounts(Connection dbconn) {
        for (String tableName : tableNames) {
            String query = "SELECT COUNT(*) AS tupleCount FROM " + tableName;
            try (Statement stmt = dbconn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
                if (rs.next()) {
                    int count = rs.getInt("tupleCount");

                    if (count == 0) {
                        System.out.printf("%-20s %10d tuples !%n", tableName, count);
                    } else {
                        System.out.printf("%-20s %10d tuples%n", tableName, count);
                    }
                }
            } catch (SQLException e) {
                System.err.println("Error counting tuples for table " + tableName + ": " + e.getMessage());
            }
        }
    }

    // * Prints table tuples
    private static void printTable(Connection dbconn, String tableName) {
        String query = "SELECT * FROM " + tableName;
        try (Statement stmt = dbconn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {

            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            // Print column headers
            for (int i = 1; i <= columnCount; i++) {
                System.out.print(metaData.getColumnName(i) + "\t");
            }
            System.out.println();

            // Print rows
            while (rs.next()) {
                for (int i = 1; i <= columnCount; i++) {
                    System.out.print(rs.getString(i) + "\t");
                }
                System.out.println();
            }
        } catch (SQLException e) {
            System.err.println("Error printing table contents for " + tableName + ": " + e.getMessage());
        }
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
                    + "passType VARCHAR(20), "
                    + "status VARCHAR(10), "
                    + "memberID INTEGER, "
                    + "rentalID INTEGER)",
            // Gear Rental: rentalID, startDate, expDate, return status, status, skiPassID
            "CREATE TABLE GearRental ("
                    + "rentalID INTEGER, "
                    + "startDate DATE, "
                    + "returnStatus VARCHAR(50), "
                    + "status VARCHAR(50), "
                    + "skiPassID INTEGER, "
                    + "PRIMARY KEY (rentalID))",
            // Equipment: EquipmentID, type, size, status
            "CREATE TABLE Equipment ("
                    + "equipmentID INTEGER, "
                    + "type VARCHAR(20), "
                    + "eSize VARCHAR(10), "
                    + "status VARCHAR(10),"
                    + "PRIMARY KEY (equipmentID))",
            // Equipment Update: equipmentUpdateID, equipmentID, type, notes
            "CREATE TABLE EquipmentUpdate ("
                    + "equipmentUpdateID INTEGER, "
                    + "equipmentID INTEGER, "
                    + "type VARCHAR(20), "
                    + "notes VARCHAR(200), "
                    + "updateDate DATE, " // ! just added
                    + "PRIMARY KEY (equipmentUpdateID))",
            // Gear Rental Update: rentalUpdateID, rentalID, type, notes
            "CREATE TABLE GearRentalUpdate ("
                    + "rentalUpdateID INTEGER, "
                    + "rentalID INTEGER, "
                    + "type VARCHAR(20), "
                    + "notes VARCHAR(200), "
                    + "updateDate DATE, " // ! just added
                    + "PRIMARY KEY (rentalUpdateID))",
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
                    + "abilityLevel VARCHAR2(25), "
                    + "openTime TIMESTAMP, " // * time doesn't exist so we use date's time component. ignore the
                                             // calender components
                    + "closeTime TIMESTAMP, " // * time doesn't exist so we use date's time component. ignore the
                                              // calender components
                    + "status VARCHAR2(10))",
            // LessonOrder: lessonOrderID, memberID, lessonsPurchased, remainingSessions
            "CREATE TABLE LessonOrder ("
                    + "lessonOrderID INTEGER, "
                    + "memberID INTEGER, "
                    + "lessonsPurchased INTEGER, "
                    + "remainingSessions INTEGER, "
                    + "PRIMARY KEY (lessonOrderID))",
            // Lesson: LessonID, lessonName, EmployeeID
            "CREATE TABLE Lesson ("
                    + "lessonID INTEGER PRIMARY KEY, "
                    + "lessonName VARCHAR2(25) NOT NULL, "
                    + "employeeID INTEGER)",
            // LessonSession: sessionID, date, startTime, endTime, lessonID
            "CREATE TABLE LessonSession ("
                    + "sessionID INTEGER, " // ! removed Date
                    + "startTime TIMESTAMP, " // * time doesn't exist so we use date's time component. ignore other
                                              // components
                    + "endTime TIMESTAMP, " // * time doesn't exist so we use date's time component. ignore other
                                            // components
                    + "lessonID INTEGER, "
                    + "PRIMARY KEY (sessionID))",
            // Instructor: EmployeeID, Certification level
            "CREATE TABLE Instructor ("
                    + "employeeID INTEGER PRIMARY KEY, "
                    + "certificationLevel VARCHAR2(20))",
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
                    + "location VARCHAR2(30))",
            // IncomeSource: sourceID, day, lodgeID, sourceName, dailyIncome
            "CREATE TABLE IncomeSource ("
                    + "sourceID INTEGER, "
                    + "day DATE, "
                    + "lodgeID INTEGER, "
                    + "sourceName VARCHAR(50), "
                    + "dailyIncome INTEGER, "
                    + "PRIMARY KEY (sourceID))",
            // Shuttle: shuttleID, location, capacity, status
            "CREATE TABLE Shuttle ("
                    + "shuttleID INTEGER PRIMARY KEY, "
                    + "shuttleName VARCHAR2(20), "
                    + "status VARCHAR2(10))",
            // LessonToOrder: lessonID, lessonOrderID
            "CREATE TABLE LessonToOrder ("
                    + "sessionID INTEGER, "
                    + "lessonOrderID INTEGER, "
                    + "PRIMARY KEY (sessionID, lessonOrderID))", // ! changed to sessionID
            // TrailLift: trailName, liftName
            "CREATE TABLE TrailLift ("
                    + "trailName VARCHAR(25), "
                    + "liftName VARCHAR(25), "
                    + "PRIMARY KEY (trailName, liftName))",
            // LiftPassUsage: skiPassID, liftName, dateUsed, timeUsed
            "CREATE TABLE LiftPassUsage ("
                    + "skiPassID INTEGER, "
                    + "liftName VARCHAR(25), "
                    + "dateUsed DATE, " // * date includes time
                    + "PRIMARY KEY (skiPassID, liftName, dateUsed))",
            // ShuttleLodge: shuttleID, lodgeID
            "CREATE TABLE ShuttleLodge ("
                    + "shuttleID INTEGER, "
                    + "lodgeID INTEGER, "
                    + "PRIMARY KEY (shuttleID, lodgeID))",
            // RentalEquipment: rentalID, equipmentID
            "CREATE TABLE RentalEquipment ("
                    + "rentalID INTEGER, "
                    + "equipmentID INTEGER, "
                    + "PRIMARY KEY (rentalID, equipmentID))",
            // EmployeeIncomeSource: employeeID, sourceID, day
            "CREATE TABLE EmployeeIncomeSource ("
                    + "employeeID INTEGER, "
                    + "sourceID INTEGER, "
                    + "PRIMARY KEY (employeeID, sourceID))"
    };
    // #endregion Tables creation consts


    private static void addForeignKeys(Connection dbconn) {
        try (Statement stmt = dbconn.createStatement()) {
            // Ski pass references Member and GearRental
            stmt.executeUpdate(
                    "ALTER TABLE SkiPass ADD CONSTRAINT fk_member FOREIGN KEY (memberID) REFERENCES Member(memberID)");
            stmt.executeUpdate(
                    "ALTER TABLE SkiPass ADD CONSTRAINT fk_rental FOREIGN KEY (rentalID) REFERENCES GearRental(rentalID)");
            // GearRental references SkiPass
            stmt.executeUpdate(
                    "ALTER TABLE GearRental ADD CONSTRAINT fk_skiPass FOREIGN KEY (skiPassID) REFERENCES SkiPass(skiPassID)");
            // EquipmentUpdate references Equipment
            stmt.executeUpdate(
                    "ALTER TABLE EquipmentUpdate ADD CONSTRAINT fk_equipment FOREIGN KEY (equipmentID) REFERENCES Equipment(equipmentID)");
            // GearRentalUpdate references GearRental
            stmt.executeUpdate(
                    "ALTER TABLE GearRentalUpdate ADD CONSTRAINT fk_rentalUpdate FOREIGN KEY (rentalID) REFERENCES GearRental(rentalID)");
            // LessonOrder references Member
            stmt.executeUpdate(
                    "ALTER TABLE LessonOrder ADD CONSTRAINT fk_lessonOrder_member FOREIGN KEY (memberID) REFERENCES Member(memberID)");
            // LessonSession references Lesson
            stmt.executeUpdate(
                    "ALTER TABLE LessonSession ADD CONSTRAINT fk_lessonSession FOREIGN KEY (lessonID) REFERENCES Lesson(lessonID)");
            // Instructor references Employee
            stmt.executeUpdate(
                    "ALTER TABLE Lesson ADD CONSTRAINT fk_lesson_employee FOREIGN KEY (employeeID) REFERENCES Instructor(employeeID)");
            // Employee references EmployeeIncomeSource
            stmt.executeUpdate(
                    "ALTER TABLE IncomeSource ADD CONSTRAINT fk_incomeSource FOREIGN KEY (lodgeID) REFERENCES Lodge(lodgeID)");
            // Shuttle references ShuttleLodge
            stmt.executeUpdate(
                    "ALTER TABLE LessonToOrder ADD CONSTRAINT fk_lessonToOrder_session FOREIGN KEY (sessionID) REFERENCES LessonSession(sessionID)");
            // ShuttleLodge references Shuttle and Lodge
            stmt.executeUpdate(
                    "ALTER TABLE LessonToOrder ADD CONSTRAINT fk_lessonToOrder_order FOREIGN KEY (lessonOrderID) REFERENCES LessonOrder(lessonOrderID)");
            // TrailLift references Trail and Lift
            stmt.executeUpdate(
                    "ALTER TABLE TrailLift ADD CONSTRAINT fk_trailLift_trail FOREIGN KEY (trailName) REFERENCES Trail(trailName)");
            stmt.executeUpdate(
                    "ALTER TABLE TrailLift ADD CONSTRAINT fk_trailLift_lift FOREIGN KEY (liftName) REFERENCES Lift(liftName)");
            // LiftPassUsage references SkiPass and Lift
            stmt.executeUpdate(
                    "ALTER TABLE LiftPassUsage ADD CONSTRAINT fk_liftPassUsage_skiPass FOREIGN KEY (skiPassID) REFERENCES SkiPass(skiPassID)");
            stmt.executeUpdate(
                    "ALTER TABLE LiftPassUsage ADD CONSTRAINT fk_liftPassUsage_lift FOREIGN KEY (liftName) REFERENCES Lift(liftName)");
            // ShuttleLodge references Shuttle and Lodge
            stmt.executeUpdate(
                    "ALTER TABLE ShuttleLodge ADD CONSTRAINT fk_shuttleLodge_shuttle FOREIGN KEY (shuttleID) REFERENCES Shuttle(shuttleID)");
            stmt.executeUpdate(
                    "ALTER TABLE ShuttleLodge ADD CONSTRAINT fk_shuttleLodge_lodge FOREIGN KEY (lodgeID) REFERENCES Lodge(lodgeID)");
            // RentalEquipment references GearRental and Equipment
            stmt.executeUpdate(
                    "ALTER TABLE RentalEquipment ADD CONSTRAINT fk_rentalEquipment_rental FOREIGN KEY (rentalID) REFERENCES GearRental(rentalID)");
            stmt.executeUpdate(
                    "ALTER TABLE RentalEquipment ADD CONSTRAINT fk_rentalEquipment_equipment FOREIGN KEY (equipmentID) REFERENCES Equipment(equipmentID)");
            // EmployeeIncomeSource references Employee and IncomeSource
            stmt.executeUpdate(
                    "ALTER TABLE EmployeeIncomeSource ADD CONSTRAINT fk_EISource_employee FOREIGN KEY (employeeID) REFERENCES Employee(employeeID)");
            stmt.executeUpdate(
                    "ALTER TABLE EmployeeIncomeSource ADD CONSTRAINT fk_EISource_source FOREIGN KEY (sourceID) REFERENCES IncomeSource(sourceID)");
        } catch (SQLException e) {
            System.err.println("Error adding foreign keys: " + e.getMessage());
        }
    }

    private static void makeTables(Connection dbconn) {
        for (int i = 0; i < tableNames.length; i++) {
            String tableName = tableNames[i];
            String createTableSQL = tableCreateStatements[i];

            // makes the current table
            try {
                Statement stmt = dbconn.createStatement();
                if (printDebug) {
                    System.out.println("Creating table: " + tableName);
                    System.out.println("SQL Statement: " + createTableSQL);
                }

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
        if (printDebug) {
            System.out.println("made tables!");
        }

    }

    private static void forceDropTables(Connection dbconn) {
        for (int i = 0; i < tableNames.length; i++) {
            String tableName = tableNames[i];

            // drops the current table
            try {
                Statement stmt = dbconn.createStatement();
                stmt.executeUpdate("DROP TABLE " + tableName + " CASCADE CONSTRAINTS");
                if (printDebug) {
                    System.out.println("Dropping table: " + tableName);
                }
            } catch (SQLException e) {
                System.err.println("Error: couldn't DROP table " + tableName + ": " + e.getMessage());
            }
        }
    }

    // #endregion // * Table Creation

    // #region // * Populate tables
    
    // List of names (courtesy of https://1000randomnames.com)
    static int currentName = 0;
    // 300 names
    static final String[] names = {
            "Lilianna Branch", "Keenan Giles", "Bailee Ortega", "Kobe Faulkner", "Ansley Caldwell",
            "Rylan Hutchinson", "Jamie Gonzalez", "Ethan Huff", "Karsyn Barajas", "Brennan Lyons",
            "Kenzie Chandler", "Royal Waters", "Bristol Richardson", "Robert Montoya", "Kamryn Snyder",
            "Thiago Gray", "Sarah Nixon", "Cory Davidson", "Jayla Walton", "Dominick Edwards",
            "Ivy Pierce", "Nicolas Koch", "Milana Rice", "Graham Yang", "Angelina Beasley",
            "Stanley Dejesus", "Julissa Blake", "Zyaire Bates", "Madilyn Lucero", "Felipe Boyer",
            "Chaya Wolf", "Jase Camacho", "Armani Durham", "Kellen Acosta", "Kaia Tran",
            "Braxton Eaton", "Miley Flowers", "Saul Hines", "Poppy Barron", "Dustin Vang",
            "Madisyn Reyna", "Reginald Fernandez", "Amara Barber", "Solomon Small", "Zaria Lozano",
            "Boone Simon", "Kalani Flynn", "Kannon Calderon", "Serena Pennington", "Bobby Waller",
            "Whitley Peck", "Yousef Rollins", "Araceli Nolan", "Maximo Wood", "Natalia Shah",
            "Zain Ellis", "Ayla Woodard", "Westley Gould", "Violeta Brock", "Julio Bowman",
            "Fiona Monroe", "Colby Larson", "Alayna Dickson", "Maxton Huffman", "Hayley Delgado",
            "Colt Wolfe", "Hallie Schultz", "Cody Cox", "Sadie Finley", "Calum Maldonado",
            "Elaina Delgado", "Colt Atkinson", "Jazmin French", "Corey McIntyre", "Rebekah Rios",
            "Israel Le", "Myla Craig", "Odin Wagner", "Maeve Gallagher", "Marcos Salgado",
            "Avalynn Portillo", "Wallace Barker", "Remington Crosby", "Tristen Patterson", "Kaylee Zhang",
            "Isaias Hensley", "Malaya Ingram", "Tripp Hudson", "Kamila Boyer", "Zeke O’Connor",
            "Charli Christensen", "Gregory Parks", "Ainsley Hurley", "Van Stevens", "Katherine Tapia",
            "Samir Shaw", "Emersyn Chase", "Otis Marks", "Monica McKay", "Joey Galindo",
            "Corinne Roy", "Marcelo Jimenez", "Adeline Mahoney", "Kamryn Clarke", "Kaitlyn Mata",
            "Ray Webb", "Ariella Graves", "Cesar Crawford", "Aubree Webb", "Lorenzo Oliver",
            "Camille Ramsey", "Luciano Rosales", "Kinley McDaniel", "Major Sierra", "Marceline Galvan",
            "Kingsley McPherson", "Emmaline Tapia", "Samir Sampson", "Meilani Dyer", "Atreus Cochran",
            "Alma Humphrey", "Krew Arroyo", "Kyra Preston", "Vincenzo Gallagher", "Elliott Faulkner",
            "Jabari Tran", "Kylie Villarreal", "Nikolai Meadows", "Pearl Perry", "Waylon Zavala",
            "Liv Hubbard", "Forrest Wilson", "Luna Lu", "Duncan Romero", "Eliza Shepard",
            "Damari Travis", "Mazikee Gross", "Quinn Daniels", "Ember McLaughlin", "Ibrahim James",
            "Quinn Moore", "Levi Jones", "Sophia Buckley", "Aryan Oliver", "Camille Sanford",
            "Truett Terry", "Wren Lowery", "Jaxxon Moody", "Elaine Anthony", "Shiloh Morse",
            "Kairi Hess", "Lawrence Medrano", "Halle Rose", "Hayden Ball", "Abby Bates",
            "Ellis McConnell", "Denise Orozco", "Keanu Lindsey", "Colette Melendez", "Nikolas Guevara",
            "Teresa Taylor", "Jackson Glenn", "Blaire Bradshaw", "Emory McCoy", "Mckenzie Xiong",
            "Azrael Horn", "Avah Novak", "Bishop Leon", "Amora Conway", "Orlando Rich",
            "Sunny Campbell", "Christopher Luna", "Journey Michael", "Bronson Guzman", "Ashley Tran",
            "Braxton Rose", "Magnolia Sosa", "Emir Nolan", "Itzayana Reyna", "Reginald Houston",
            "Lylah Patrick", "Derrick James", "Quinn Michael", "Bronson Schmitt", "Queen Nielsen",
            "Tru Heath", "Amani Avery", "Jakari Cohen", "Destiny Torres", "Jayden McCullough",
            "Hana Rhodes", "Titus Novak", "Kaiya Donaldson", "Canaan Pena", "Rachel Medrano",
            "Arian Mann", "Paislee Mata", "Ray Salas", "Amber Haley", "Leif Walker",
            "Hazel Cooper", "Jonathan Horne", "Marlowe Underwood", "Reece Riley", "Kayla Horton",
            "Garrett Rose", "Magnolia McFarland", "Dane David", "Haylee Harrington", "Omari Tanner",
            "Harmoni Pittman", "Valentino Dougherty", "Alisson Harvey", "Cayden Parrish", "Tiana Nash",
            "Chandler Lim", "Giavanna Tucker", "Ivan Ramirez", "Grace Padilla", "Jaden Alexander",
            "Lyla Copeland", "Axton Ellison", "Raina Jefferson", "Raylan Rollins", "Araceli Cuevas",
            "Brecken Ali", "Zelda Knox", "Valentin Pope", "Aurelia Pittman", "Valentino Dalton",
            "Lilian Harrington", "Omari Barton", "Danna Keith", "Jagger Leonard", "Demi Baker",
            "Ezra Rios", "Brooke Jimenez", "Silas Meadows", "Pearl Galvan", "Kingsley Harmon",
            "Maren Avalos", "Coen Cabrera", "Daleyza Barry", "Emery Harper", "Ana Golden",
            "Amias Dean", "Julianna Murillo", "Lance Pennington", "Yareli Chung", "Ira Jacobs",
            "Camilla Moses", "Niklaus Chapman", "Zuri Vang", "Jimmy Hull", "Andi Moran",
            "Tate Wiley", "Lauryn Love", "Jeffrey Burton", "Miriam Barron", "Dustin Vincent",
            "Allyson Perkins", "Kyrie Murray", "Faith McCormick", "Jasiah Spencer", "Alyssa Martin",
            "Mateo Stephenson", "Khaleesi Leon", "Marshall Meyer", "Sara Guzman", "Jude Spears",
            "Isabela Peterson", "Santiago Allen", "Riley Wiley", "Mathew Zhang", "Sarai Lane",
            "Matias Sanford", "Emerald Alvarez", "Xavier Avery", "Meghan Lester", "Lee Banks",
            "Cali Nielsen", "Tru Nichols", "Aliyah Burke", "Jax Woodard", "Aubrie Vang",
            "Jimmy Woodard", "Aubrie Glover", "Mack McKee", "Kori Paul", "Noel Young",
            "Zoey Padilla", "Jaden Eaton", "Miley Gilbert", "Tobias Lam", "Karina Paul",
            "Noel Graves", "Elle Skinner", "Ridge Barron", "Anya Madden", "Everest Lyons"
    };

    
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

            if (rentalID == -1) {
                pstmt.setNull(10, Types.INTEGER); // Set rentalID to NULL if not provided
            } else {
                pstmt.setInt(10, rentalID);
            }

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
                if (printDebug) {
                    if (rowsInserted > 0) {
                        System.out.println("Successfully added equipment: " + type + ", " + size + ", " + status);
                    }
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
            String returnStatus, String status, int skiPassID, int[] rentedEquipmentTypes) {
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
            if (printDebug) {
                if (rowsInserted > 0) {
                    System.out.println("Successfully added gear rental: " + rentalID + ", " + startDate + ", "
                            + returnStatus + ", " + status + ", " + skiPassID);
                }
            }

            // [0]ski boots [1]ski poles [2]skis [3]snowboards [4]helmet
            String[] types = { "Ski boots", "Ski poles", "Skis", "Snowboard", "Helmet" };

            // Add rented equipment to RentalEquipment table
            for (int equipmentType : rentedEquipmentTypes) {
                int equipmentID = -1;

                // find available equipment of the specified type (active and not rented)
                String findUnusedEquipmentSQL = "SELECT equipmentID FROM Equipment WHERE type = ? AND status = 'Active' AND equipmentID NOT IN (SELECT equipmentID FROM RentalEquipment)";

                PreparedStatement findStmt = conn.prepareStatement(findUnusedEquipmentSQL);
                findStmt.setString(1, types[equipmentType]);

                try (ResultSet rs = findStmt.executeQuery()) {
                    if (rs.next()) {
                        equipmentID = rs.getInt("equipmentID");
                    } else {
                        System.err.println("No unused equipment found for type: " + types[equipmentType]);
                        return -1;
                    }
                } catch (SQLException e) {
                    System.err.println("Error finding unused equipment: " + e.getMessage());
                    return -1; // error
                }

                // Insert into RentalEquipment table
                String insertRentalEquipmentSQL = "INSERT INTO RentalEquipment (rentalID, equipmentID) VALUES (?, ?)";
                PreparedStatement rentalPstmt = conn.prepareStatement(insertRentalEquipmentSQL);
                rentalPstmt.setInt(1, rentalID);
                rentalPstmt.setInt(2, equipmentID);
                rentalPstmt.executeUpdate();
            }
        } catch (SQLException e) {
            System.err.println("Error adding gear rental: " + e.getMessage());
            return -1; // error
        }
        return 0;
    }
    // * generate a random ID for a table (columnName should be the ID column)
    // this is a helper for the add methods

    private static int populateTables(Connection dbconn) {
        // * add random entities to the database
        // * this is extremely long, but it is necessary due to the ID overlaps and how some of the created IDs are used in other tables

        Random rand = new Random(); // random number generator
        rand.setSeed(0); // set seed for reproducibility
        int status = 0;


        // * Visitor Center Lodge: 1, N visitory centery 123 
        //  pass management, restaurant
        int visitorCenterID = addToTable(dbconn, "Lodge", "lodgeID", new String[]{"'North Visitry Centery 123'"});
        int employee1 = addRandomEmployee(dbconn, rand, "Ski Clerk");
        int employee2 = addRandomEmployee(dbconn, rand, "Ski Clerk");
        int employee3 = addRandomEmployee(dbconn, rand, "General Manager");
        makeIncomeSourceRecords(dbconn, rand, new int[]{employee1, employee2, employee3}, visitorCenterID, "'Ski pass and rental'");

        employee1 = addRandomEmployee(dbconn, rand, "Restaurant worker");
        employee2 = addRandomEmployee(dbconn, rand, "Restaurant worker");
        employee3 = addRandomEmployee(dbconn, rand, "Restaurant worker");
        int employee4 = addRandomEmployee(dbconn, rand, "Restaurant worker");
        int employee5 = addRandomEmployee(dbconn, rand, "Restaurant worker");
        int employee7 = addRandomEmployee(dbconn, rand, "Restaurant worker");
        int employee6 = addRandomEmployee(dbconn, rand, "General Manager");
        makeIncomeSourceRecords(dbconn, rand, new int[]{employee1, employee2, employee3, employee4, employee5, employee6, employee7}, visitorCenterID, "'Restaurant'");

        // * Restaurant Lodge: 2, N therestaurant 456
        //  restaurant, gift shop, parking lot
        int restaurantID = addToTable(dbconn, "Lodge", "lodgeID", new String[]{"'el restaurante 456'"});
        employee1 = addRandomEmployee(dbconn, rand, "Restaurant worker");
        employee2 = addRandomEmployee(dbconn, rand, "Restaurant worker");
        employee3 = addRandomEmployee(dbconn, rand, "Gift shop worker");
        employee4 = addRandomEmployee(dbconn, rand, "Gift shop worker");
        employee5 = addRandomEmployee(dbconn, rand, "Parking lot manager");
        makeIncomeSourceRecords(dbconn, rand, new int[]{employee1, employee2}, restaurantID, "'Restaurant'");
        makeIncomeSourceRecords(dbconn, rand, new int[]{employee3, employee4}, restaurantID, "'Gift shop'");
        makeIncomeSourceRecords(dbconn, rand, new int[]{employee5}, restaurantID, "'Parking lot'");

        // * School Lodge: 3, N theschool 789
        //  school, gift shop
        int schoolID = addToTable(dbconn, "Lodge", "lodgeID", new String[]{"'N schoolStreet 789'"});
        employee1 = addRandomEmployee(dbconn, rand, "Instructor");
        employee2 = addRandomEmployee(dbconn, rand, "Instructor");
        employee3 = addRandomEmployee(dbconn, rand, "Instructor");
        employee4 = addRandomEmployee(dbconn, rand, "Gift shop worker");
        employee5 = addRandomEmployee(dbconn, rand, "Gift shop worker");
        makeIncomeSourceRecords(dbconn, rand, new int[]{employee1, employee2, employee3}, schoolID, "'Ski lessons'");
        makeIncomeSourceRecords(dbconn, rand, new int[]{employee4, employee5}, schoolID, "'Gift shop'");

        // * Add snowboard lessons (beginner, intermediate, advanced)
        int[] boardBeginnerIDs = makeRandomLesson(dbconn, rand, "Snowboard Beginner", "PSIA-AASI I");
        int[] boardIntermediateIDs = makeRandomLesson(dbconn, rand, "Snowboard Intermediate", "PSIA-AASI II");
        int[] boardAdvancedIDs = makeRandomLesson(dbconn, rand, "Snowboard Advanced", "PSIA-AASI III");

        // * add ski lessons (beginner, intermediate, advanced)
        int[] skiBeginnerIDs = makeRandomLesson(dbconn, rand, "Ski Beginner", "PSIA-AASI I");
        int[] skiIntermediateIDs = makeRandomLesson(dbconn, rand, "Ski Intermediate", "PSIA-AASI II");
        int[] skiAdvancedIDs = makeRandomLesson(dbconn, rand, "Ski Advanced", "PSIA-AASI III");

        // * add shuttles
        int shuttle1 = addToTable(dbconn, "Shuttle", "shuttleID", new String[]{"'Shuttle 1'", "'Active'"});
        int shuttle2 = addToTable(dbconn, "Shuttle", "shuttleID", new String[]{"'Shuttle 2'", "'Active'"});
        int shuttle3 = addToTable(dbconn, "Shuttle", "shuttleID", new String[]{"'Shuttle 2'", "'Inactive'"});

        // Shuttle 1 goes to lodge 1 and lodge 2
        rawAddToTable(dbconn, "ShuttleLodge", new String[]{String.valueOf(shuttle1), String.valueOf(visitorCenterID)});
        rawAddToTable(dbconn, "ShuttleLodge", new String[]{String.valueOf(shuttle1), String.valueOf(restaurantID)});

        // Shuttle 2 goes to lodge 2 and lodge 3
        rawAddToTable(dbconn, "ShuttleLodge", new String[]{String.valueOf(shuttle2), String.valueOf(restaurantID)});
        rawAddToTable(dbconn, "ShuttleLodge", new String[]{String.valueOf(shuttle2), String.valueOf(schoolID)});

        // Shuttle 3 goes to lodge 3 and lodge 1
        rawAddToTable(dbconn, "ShuttleLodge", new String[]{String.valueOf(shuttle3), String.valueOf(visitorCenterID)});
        rawAddToTable(dbconn, "ShuttleLodge", new String[]{String.valueOf(shuttle3), String.valueOf(schoolID)});

        // * add equipment
        // make 50 of each equipment type
        for (int i = 0; i < 50; i++) {
            for (int j = 0; j < 5; j++) {
                int equipmentID = addRandomEquipment(dbconn, rand, j);
                if (equipmentID == -1) {
                    System.err.println("Error adding random equipment to the database");
                    status = -1; // error
                }
            }
        }

        // * add lifts
        addLift(dbconn, "Lift A", "Beginner-Intermediate", Time.valueOf("09:00:00"), Time.valueOf("16:00:00"), "Active");
        addLift(dbconn, "Lift B", "Beginner-Intermediate", Time.valueOf("08:30:00"), Time.valueOf("16:30:00"), "Active");
        addLift(dbconn, "Lift C", "Intermediate-Expert", Time.valueOf("10:00:00"), Time.valueOf("15:00:00"), "Inactive");
        addLift(dbconn, "Lift D", "Intermediate-Expert", Time.valueOf("09:00:00"), Time.valueOf("17:00:00"), "Active");
        String[] liftNames = new String[]{"Lift A", "Lift B", "Lift C", "Lift D"};
        
        // * add trails
        for (int i = 0; i < 48; i++) {
            String trailID = addRandomTrail(dbconn, rand, liftNames);
            if (trailID == "-1") {
                System.err.println("Error adding random trail to the database");
                status = -1; // error
            }
        }

        // * member loop
        // makes 20 members
        //   makes ski passes (80% active, 20% inactive) <- seperate chances
        //   makes lesson orders (40% chance)
        for (int i = 0; i < 100; i++) {
            int memberID = addRandomMember(dbconn, rand); // add a random member to the database

            // 20% chance for inactive ski pass
            if (rand.nextInt(100) < 20) {
                int inactiveSkiPassID = addRandomSkiPass(dbconn, rand, memberID, false, liftNames);
                if (inactiveSkiPassID == -1) {
                    System.err.println("Error adding inactive ski pass for member " + memberID);
                    status = -1; // error
                }
            }
            // seperate 80% chance for active ski pass
            if (rand.nextInt(100) < 80) {
                int activeSkiPassID = addRandomSkiPass(dbconn, rand, memberID, true, liftNames);
                if (activeSkiPassID == -1) {
                    System.err.println("Error adding active ski pass for member " + memberID);
                    status = -1; // error
                }
            }

            int allLessonSessionLength = boardBeginnerIDs.length + boardIntermediateIDs.length + boardAdvancedIDs.length + skiBeginnerIDs.length + skiIntermediateIDs.length + skiAdvancedIDs.length;


            // if i % 10 == 0 or 1, add it to unfinished lessons; otherwise, add it to finished lessons
            int[] finishedSessions = new int[allLessonSessionLength * 2 / 5];
            int[] unfinishedSessions = new int[allLessonSessionLength * 3 / 5];
            int finishedCount = 0, unfinishedCount = 0;

            // f, f, f, f, u, u, u, u, u, u
            for (int[] lessonIDs : new int[][]{boardBeginnerIDs, boardIntermediateIDs, boardAdvancedIDs, skiBeginnerIDs, skiIntermediateIDs, skiAdvancedIDs}) {
                for (int j = 0; j < lessonIDs.length; j++) {
                    if (j % 10 < 4) {
                        finishedSessions[unfinishedCount++] = lessonIDs[j];
                    } else {
                        unfinishedSessions[finishedCount++] = lessonIDs[j];
                    }
                }
            }

            // // Trim the arrays to their actual sizes
            // finishedSessions = java.util.Arrays.copyOf(finishedSessions, finishedCount);
            // unfinishedSessions = java.util.Arrays.copyOf(unfinishedSessions, unfinishedCount);

            int lessonOrderID = 0;
            if (rand.nextInt(100) < 40) { // 40% chance to make an order
                lessonOrderID = makeRandomLessonOrder(dbconn, rand, memberID, finishedSessions, unfinishedSessions);
                if (lessonOrderID == -1) {
                    System.err.println("Error adding lesson order for member " + memberID);
                    status = -1; // error
                }
            }

            if (status == -1 || lessonOrderID == -1 || memberID == -1) {
                System.err.println("Error adding random entities to the database");

                status = -1; // error
            }
        }

        // * adds an extra 10 general worker employees
        for (int i = 0; i < 10; i++) {
            int employeeID = addRandomEmployee(dbconn, rand, "General Workers");
            if (employeeID == -1) {
                System.err.println("Error adding random employee to the database");
                status = -1; // error
            }
        }

        return status;
    }

    // * adds an income source with random daily income for a given lodge and with given employees
    // also creates EmployeeIncomeSource records for each employee for 30 days
    private static int makeIncomeSourceRecords(Connection dbconn, Random rand, int[] employees, int lodgeID, String sourceName) {
        int sourceID = -1;
        for (int i = 0; i < 30; i++) {
            long dayOffset = i * 24L * 60 * 60 * 1000; // Offset in milliseconds for each day
            java.sql.Date day = new java.sql.Date(System.currentTimeMillis() - dayOffset);
            sourceID = addRandomIncomeSource(dbconn, rand, lodgeID, sourceName, day);

            if (sourceID == -1) {
                System.err.println("Error adding income source for lodge " + lodgeID + " on day " + day + " for source " + sourceName);
                return -1;
            }

            int[] pickedEmployees = new int[employees.length];
            for (int j = 0; j < rand.nextInt(employees.length) + 1; j++) { // Pick a random number of employees
                int employee;
                while (true) {
                    employee = rand.nextInt(employees.length);
                    if (pickedEmployees[employee] == 0) { // Check if the employee has already been picked
                        pickedEmployees[employee] = 1; // Mark as picked
                        break;
                    }
                }
                employee = employees[employee]; // Get the actual employee ID

                int employeeIncomeSource = rawAddToTable(dbconn, "EmployeeIncomeSource", new String[]{
                    String.valueOf(employee), String.valueOf(sourceID)});

                if (employeeIncomeSource == -1) {
                    System.err.println("Error adding employee income source for employee " + employee + " on day " + day);
                    return -1;
                }
            }
        }

        return sourceID;
    }

    // * adds weekly sessions with random days and times (but same days/times per week); makes 5 weeks of sessions (2 back, current, and 2 forward)
    private static int[] addRandomSessions(Connection dbconn, Random rand, int lessonID) {
        // LessonSession: sessionID, startTime, endTime, lessonID

        // Generate two random days of the week
        int day1 = rand.nextInt(7) + 1; // Random day between 1 and 8
        int day2 = rand.nextInt(7) + 1; // Random day between 1 and 8
        while (day1 == day2) {
            day2 = rand.nextInt(7); // Ensure the two days are different
        }

        int[] sessionIDs = new int[10]; // Array to store session IDs for 5 weeks (2 sessions per week)

        int startHour = 10 + rand.nextInt(5); // Random hour between 10 and 14 (2 PM)
        String startTime = String.format("%02d:00:00", startHour);
        String endTime = String.format("%02d:00:00", startHour + 3); // 3 hours later

        if (printDebug) {
            System.out.println("Session Details: Day1: " + day1 + ", Day2: " + day2 + ", StartTime: " + startTime + ", EndTime: " + endTime);
        }

        int sessions = 0;
        // * add lesson sessions to the database for the past 2 weeks to 2 weeks forward
        // * returns the number of sessions added
        
        for (int i = -2; i < 3; i++) { // Loop from 2 weeks ago (-14) to 2 weeks forward (+14)
            // current day + i weeks + day days
            java.sql.Date sessionDate1 = new java.sql.Date(System.currentTimeMillis() + i * 24L * 60 * 60 * 1000 * 7 + day1 * 24L * 60 * 60 * 1000); // Calculate the session date
            java.sql.Date sessionDate2 = new java.sql.Date(System.currentTimeMillis() + i * 24L * 60 * 60 * 1000 * 7 + day2 * 24L * 60 * 60 * 1000); // Calculate the session date

            // Add session for day 1
            sessionIDs[sessions] = addToTable(dbconn, "LessonSession", "sessionID", new String[]{
                "TO_DATE('" + sessionDate1 + " " + startTime + "', 'YYYY-MM-DD HH24:MI:SS')",
                "TO_DATE('" + sessionDate1 + " " + endTime + "', 'YYYY-MM-DD HH24:MI:SS')",
                String.valueOf(lessonID)
            });
            
            if (sessionIDs[sessions] == -1) {
                System.err.println("Error adding session for lesson " + lessonID + " on date " + sessionDate1 + " or " + sessionDate2);
                return new int[] {-1}; // error
            }
            sessions++;
            
            // Add session for day 2
            sessionIDs[sessions] = addToTable(dbconn, "LessonSession", "sessionID", new String[]{
                "TO_DATE('" + sessionDate2 + " " + startTime + "', 'YYYY-MM-DD HH24:MI:SS')",
                "TO_DATE('" + sessionDate2 + " " + endTime + "', 'YYYY-MM-DD HH24:MI:SS')",
                String.valueOf(lessonID)
            });

            if (sessionIDs[sessions] == -1) {
                System.err.println("Error adding session for lesson " + lessonID + " on date " + sessionDate1 + " or " + sessionDate2);
                return new int[] {-1}; // error
            }
            sessions++;
        }

        return sessionIDs; // returns 10: [f, f, f, f, u, u, u, u, u, u]
    }

    // * makes lesson orders for a member (and their lessonToOrders)
    private static int makeRandomLessonOrder(Connection dbconn, Random rand, int memberID, int[] finishedSessions, int[] unfinishedSessions) {
        // LessonOrder: lessonOrderID, memberID, lessonsPurchased, remainingSessions
        int lessonOrderID = -1;
        int lessonAmount = rand.nextInt(10) + 1; // Random number of lessons between 1 and 10

        int lessonsPurchased = lessonAmount; // Lessons purchased start as the total lessons purchased
        int remainingSessions = rand.nextInt(lessonAmount); // random amount remain

        lessonOrderID = addToTable(dbconn, "LessonOrder", "lessonOrderID",
                new String[]{
                    String.valueOf(memberID),
                    String.valueOf(lessonsPurchased),
                    String.valueOf(remainingSessions)});


        int i = 0;
        boolean[] pickedSessions = new boolean[unfinishedSessions.length]; // Track picked lessons
        while (i < remainingSessions) {
            int lessonIndex = rand.nextInt(pickedSessions.length); // Pick a random lesson index

            if (!pickedSessions[lessonIndex]) { // Check if the lesson has already been picked
                pickedSessions[lessonIndex] = true; // Mark the lesson as picked

                // add lesson to the order
                int sessionID = unfinishedSessions[lessonIndex];
                int lessonToOrderID = rawAddToTable(dbconn, "LessonToOrder", new String[]{String.valueOf(sessionID), String.valueOf(lessonOrderID)});

                if (lessonToOrderID == -1) {
                    System.err.println("Error adding lesson to order for member " + memberID);
                    return -1; // error
                }
                i++; // increment when one is picked (to ensure lessonAmount picked)
            }
        }

        pickedSessions = new boolean[finishedSessions.length]; // Track picked lessons
        // add the remaining sessions to the order
        while (i < lessonsPurchased) {
            int lessonIndex = rand.nextInt(finishedSessions.length); // Pick a random lesson index

            if (!pickedSessions[lessonIndex]) { // Check if the lesson has already been picked
                pickedSessions[lessonIndex] = true; // Mark the lesson as picked

                // add lesson to the order
                int sessionID = finishedSessions[lessonIndex];
                int lessonToOrderID = rawAddToTable(dbconn, "LessonToOrder", new String[]{String.valueOf(sessionID), String.valueOf(lessonOrderID)});

                if (lessonToOrderID == -1) {
                    System.err.println("Error adding lesson to order for member " + memberID);
                    return -1; // error
                }
                i++; // increment when one is picked (to ensure lessonAmount picked)
            }
        }


        return lessonOrderID;
    }

    // * makes a lesson with a new random instructor, and returns the lessonID
    private static int[] makeRandomLesson(Connection dbconn, Random rand, String lessonName, String certification) {
        int instructorID = addRandomEmployee(dbconn, rand, "Instructor");
        rawAddToTable(dbconn, "Instructor", new String[]{String.valueOf(instructorID), "'" + certification + "'"});
        
        int lessonID = addToTable(dbconn, "Lesson", "lessonID", new String[]{"'" + lessonName + "'", String.valueOf(instructorID)});

        if (lessonID == -1) {
            System.err.println("Error adding lesson: " + lessonName);
            return new int[] {-1}; // error
        }

        int[] sessionIDs = addRandomSessions(dbconn, rand, lessonID);
        if (sessionIDs[0] == -1) {
            System.err.println("Error adding sessions for lesson: " + lessonName);
            return new int[] {-1}; // error
        }

        return sessionIDs; // used in lessonOrder creation
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
                if (printDebug) {
                    if (rowsInserted > 0) {
                        System.out.println("Successfully added lift: " + liftName + ", " + abilityLevel + ", "
                                + openTime + ", " + closeTime + ", " + status);
                    }
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
                if (printDebug) {
                    if (rowsInserted > 0) {
                        System.out.println("Successfully added trail: " + trailName + ", " + location + ", "
                                + difficulty + ", " + category + ", " + status);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error adding trail: " + e.getMessage());
        }
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

        String name = nextName();

        String phone = String.format("%03d-%03d-%04d", rand.nextInt(1000), rand.nextInt(1000), rand.nextInt(10000));
        String email = randStr(rand, -1) + "@gmail.com";
        java.sql.Date dob = new java.sql.Date(
                System.currentTimeMillis() - randLong(rand, 0, 100 * 365L * 24 * 60 * 60 * 1000)); // random date of
        // birth 100 years
        // ago
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
    private static int addRandomSkiPass(Connection dbconn, Random rand, int memberID, boolean active,
            String[] liftNames) {
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
        String status = active ? "Active" : "Inactive";

        int price = 0;
        java.sql.Date timeOfPurchase = new java.sql.Date(0); // will get overwritten either way
        java.sql.Date expDate = new java.sql.Date(0); // also gets overwritten
        int totalUses = 0;
        int remainingUses = 0;
        if (passType.equals("Punch Card")) {
            totalUses = rand.nextBoolean() ? 10 : 20;

            if (totalUses == 10) {
                price = 8000; // 80.00
            } else {
                price = 15000; // 150.00
            }

            timeOfPurchase = new java.sql.Date(
                    System.currentTimeMillis() - randLong(rand, 0, 3L * 365 * 24 * 60 * 60 * 1000)); // random date up
                                                                                                     // to 3 years ago
            expDate = new java.sql.Date(timeOfPurchase.getTime()); // * exp date is the same as time of purchase for
                                                                   // punch cards

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
                timeOfPurchase = new java.sql.Date(System.currentTimeMillis() - randLong(rand, 0, offset)); // random
                // date up
                // to offset
                // ago
            } else {
                // ! this can create rare cases in which a member holds multiple ski passes at
                // the same time some time in the past when inactive is called multiple times
                timeOfPurchase = new java.sql.Date(
                        System.currentTimeMillis() - offset - randLong(rand, 0, 3L * 365 * 24 * 60 * 60 * 1000)); // random
                // inactive
                // date
                // up
                // to
                // 3
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

        // * ski pass added
        int result = addSkiPass(dbconn, skiPassID, price, timeOfPurchase, expDate, totalUses, remainingUses, passType,
                status,
                memberID, rentalID);

        if (rand.nextBoolean()) {
            rentalID = generateRandomID(dbconn, "GearRental", "rentalID"); // generate random rental ID

            // * rental gear added
            result = addRandomGearRental(dbconn, rentalID, rand, skiPassID, expDate, expDate);
            if (result == -1) {
                System.err.println("Error adding gear rental: " + rentalID + ", " + skiPassID);
                return -1;
            }

            // update the ski pass with the rental ID
            try (PreparedStatement pstmt = dbconn.prepareStatement(
                    "UPDATE SkiPass SET rentalID = ? WHERE skiPassID = ?")) {
                pstmt.setInt(1, rentalID);
                pstmt.setInt(2, skiPassID);
                pstmt.executeUpdate();
            } catch (SQLException e) {
                System.err.println("Error updating SkiPass with rentalID: " + e.getMessage());
                return -1;
            }
        }

        // Add lift pass usages for random lifts
        int liftPassUsageCount = rand.nextInt(40);
        if (passType.equals("Punch Card")) {
            liftPassUsageCount = totalUses - remainingUses;
        }

        for (int i = 0; i < liftPassUsageCount; i++) {
            String liftName = liftNames[rand.nextInt(liftNames.length)]; // Random lift name
            // Random date between min(current time or purchase) and expiration
            java.sql.Date usageDate = new java.sql.Date(
                    randLong(rand, Math.min(System.currentTimeMillis(), timeOfPurchase.getTime()), expDate.getTime()));
            String usageTime = String.format("%02d:%02d:%02d", rand.nextInt(24), rand.nextInt(60), rand.nextInt(60)); // Random
                                                                                                                      // time

            // * Add lift pass usages added
            int liftPassUsageResult = rawAddToTable(dbconn, "LiftPassUsage", new String[] {
                    String.valueOf(skiPassID),
                    "'" + liftName + "'",
                    "TO_DATE('" + usageDate + " " + usageTime + "', 'YYYY-MM-DD HH24:MI:SS')"
            });

            if (liftPassUsageResult == -1) {
                System.err.println("Error adding lift pass usage for skiPassID " + skiPassID + " on lift " + liftName);
                return -1;
            }
        }

        if (result == -1) {
            System.err.println("Error adding ski pass: " + skiPassID + ", " + price + ", " + timeOfPurchase + ", "
                    + expDate + ", " + totalUses + ", " + remainingUses + ", " + passType + ", " + status);
            return -1;
        }
        return skiPassID;
    }

    // * Checks if a value exists in an array
    private static boolean intInArr(int[] array, int value) {
        for (int element : array) {
            if (element == value) {
                return true;
            }
        }
        return false;
    }

    // * add random gear rental to the database
    // * returns 0 on success, -1 on failure
    private static int addRandomGearRental(Connection dbconn, int rentalID, Random rand, int skiPassID,
            java.sql.Date skiStartDate, java.sql.Date skiExpDate) {
        // gear rental: rentalID, startDate, expDate, return status, status, skiPassID

        java.sql.Date startDate = new java.sql.Date(randLong(rand, skiStartDate.getTime(),
                Math.min(System.currentTimeMillis(), skiExpDate.getTime()))); // random date between start date, and
        // min(now or skiExpDate)
        String returnStatus = rand.nextBoolean() ? "Returned" : "Not Returned"; // 50% chance of being returned

        String status = rand.nextInt(100) >= 5 ? "Active" : "Inactive"; // 5% chance of being inactive

        int[] draftEquipment = new int[5];
        int count = 0;
        if (rand.nextBoolean()) {
            // [0]ski boots [1]ski poles [2]skis [3]snowboards [4]helmet
            draftEquipment[0] = rand.nextInt(5);
            count++;
            for (int i = 1; i < draftEquipment.length; i++) {
                int next = rand.nextInt(10); // random number between 0 and 9. equipment types are 0-4, so 5-9 are
                                             // purposefully invalid

                // 50% chance of being the same equipment type as before and therefore breaking
                // the loop
                if (intInArr(draftEquipment, next) || next > 4) {
                    break;
                } else {
                    draftEquipment[i] = next; // same equipment type as before
                }
                count++;
            }
        }

        int[] eqTypes = java.util.Arrays.copyOf(draftEquipment, count);

        int result = addGearRental(dbconn, rentalID, startDate, returnStatus, status, skiPassID, eqTypes);
        if (result == -1) {
            System.err.println("Error adding gear rental: " + rentalID + ", " + startDate + ", " + returnStatus + ", "
                    + status + ", " + skiPassID);
            return -1;
        }

        if (status.equals("Inactive")) {
            java.sql.Date date = new java.sql.Date(
                    System.currentTimeMillis() - randLong(rand, 0, 3L * 365 * 24 * 60 * 60 * 1000)); // random date up
                                                                                                     // to 3 years ago

            // * log the gear rental update
            int updateResult = addToTable(dbconn, "GearRentalUpdate", "rentalUpdateID", new String[] {
                    String.valueOf(rentalID),
                    "'Delete'",
                    "'Rental deleted (error occured).'",
                    "TO_DATE('" + date + "', 'YYYY-MM-DD')"
            });
            if (updateResult == -1) {
                System.err.println("Error logging gear rental update.");
                return -1;
            }
        }

        return 0;
    }

    // * add random trail to the database
    // * returns the trailID of the new trail, or -1 on failure
    // * lifts are [easy-int, easy-int, int-expert, int-expert]
    private static String addRandomTrail(Connection dbconn, Random rand, String[] liftNames) {
        // trail: trailName, location, difficulty, category, status
        String trailName = randStr(rand, -1);
        String location = randStr(rand, -1);

        String[] difficulties = new String[] { "Beginner", "Intermediate", "Expert" };
        int difficultyIndex = rand.nextInt(difficulties.length);
        String difficulty = difficulties[difficultyIndex];

        String[] categories = new String[] { "groomed", "park", "moguls", "glade skiing" };
        String category = categories[rand.nextInt(categories.length)];

        String status = rand.nextBoolean() ? "Active" : "Inactive"; // * open, closed

        try {
            addTrail(dbconn, trailName + " trail", "mt. " + location, difficulty, category, status);
        } catch (Exception e) {
            System.err.println("Error adding trail: " + e.getMessage());
            return "-1";
        }

        int[] possibleLifts;
        switch (difficultyIndex) {
            case 0 ->
                possibleLifts = new int[] { 0, 1 }; // easy lifts
            case 1 ->
                possibleLifts = new int[] { 0, 1, 2, 3 }; // easy and intermediate lifts
            case 2 ->
                possibleLifts = new int[] { 2, 3 }; // intermediate and expert lifts
            default ->
                throw new AssertionError();
        }

        // * add the lift to the trail
        String liftName = liftNames[possibleLifts[rand.nextInt(possibleLifts.length)]]; // random lift name from the
                                                                                        // possible lifts
        String sql = "INSERT INTO TrailLift (trailName, liftName) VALUES (?, ?)";
        try (PreparedStatement pstmt = dbconn.prepareStatement(sql)) {
            pstmt.setString(1, trailName + " trail");
            pstmt.setString(2, liftName);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error adding traillift: " + e.getMessage());
            return "-1";
        }
        if (printDebug) {
            System.out.println("Successfully added lift to trail: " + trailName + ", " + liftName);
        }

        // 30% chance to add a second lift
        if (rand.nextInt(100) < 30) {
            String secondLiftName = liftNames[possibleLifts[rand.nextInt(possibleLifts.length)]]; // random second lift
                                                                                                  // name

            // if same as first, just ignore it
            // this decreases the chance more, and makes it more likely for trails w/
            // multiple possible lifts to have multiple
            if (!secondLiftName.equals(liftName)) {
                try (PreparedStatement pstmt = dbconn.prepareStatement(sql)) {
                    pstmt.setString(1, trailName + " trail");
                    pstmt.setString(2, secondLiftName);
                    pstmt.executeUpdate();
                } catch (SQLException e) {
                    System.err.println("Error adding second traillift: " + e.getMessage());
                    return "-1";
                }
                if (printDebug) {
                    System.out.println("second lift to trail: " + trailName + ", " + secondLiftName);
                }
            }
        }

        return trailName; // primary key
    }

    // * types: [0]ski boots [1]ski poles [2]skis [3]snowboards [4]helmet
    // * adds an equipment of a given type, with a random size and status
    private static int addRandomEquipment(Connection dbconn, Random rand, int eqType) {
        // equipment: equipmentID, type, size, status
        // int eqType = rand.nextInt(types.length);
        String[] types = new String[] { "Ski boots", "Ski poles", "Skis", "Snowboard", "Helmet" };
        String type = types[eqType];

        int[] lowerBound = new int[] { 4, 80, 70, 90, 48 };
        int[] upperBound = new int[] { 15, 145, 210, 180, 65 };

        int size = rand.nextInt(upperBound[eqType] - lowerBound[eqType]) + lowerBound[eqType]; // random size between
                                                                                               // lower and upper bound
        String sizeStr = String.valueOf(size); // convert to string

        String status = rand.nextInt(100) < 95 ? "Active" : "Inactive"; // 95% chance of being available

        int equipmentID = addToTable(dbconn, "Equipment", "equipmentID",
                new String[] { "'" + type + "'", "'" + sizeStr + "'",
                        "'" + status + "'" });
        if (equipmentID == -1) {
            System.err.println("Error adding equipment: " + type + ", " + sizeStr + ", " + status);
            return -1;
        }

        if (status.equals("Inactive")) {
            // * log the equipment update
            java.sql.Date date = new java.sql.Date(
                    System.currentTimeMillis() - randLong(rand, 0, 3L * 365 * 24 * 60 * 60 * 1000)); // random date up
                                                                                                     // to 3 years ago

            int updateResult = addToTable(dbconn, "EquipmentUpdate", "equipmentUpdateID", new String[] {
                    String.valueOf(equipmentID),
                    "'Delete'",
                    "'Equipment broken or missing.'",
                    "TO_DATE('" + date + "', 'YYYY-MM-DD')"
            });
            if (updateResult == -1) {
                System.err.println("Error logging equipment update.");
                return -1;
            }
        }

        return equipmentID;
    }

    // * adds a random employee with the given job title to the database
    // * returns the employeeID of the new employee, or -1 on failure
    private static int addRandomEmployee(Connection dbconn, Random rand, String jobTitle) {
        // Employee: employeeID, name, age, sex, race, monthly salary, job title
        String name = nextName(); // get a random name from the list
        int age = rand.nextInt(50) + 20; // random age between 20 and 70
        String[] sexes = { "Male", "Female", "Other" };
        String sex = sexes[rand.nextInt(sexes.length)];
        String[] races = { "White", "Black", "Asian", "Hispanic", "Other" };
        String race = races[rand.nextInt(races.length)];
        int monthlySalary = rand.nextInt(5000) + 3000; // random salary between $3000 and $8000

        // String[] jobTitles = { "Instructor", "Manager", "Technician", "Clerk",
        // "Shuttle driver", "Janitor", "Security" }; // ! used for if we want random
        // titles (felt like it was kinda bad)
        // String jobTitle = jobTitles[rand.nextInt(jobTitles.length)];
        int employeeID = addToTable(dbconn, "Employee", "employeeID", new String[] {
                "'" + name + "'",
                String.valueOf(age),
                "'" + sex + "'",
                "'" + race + "'",
                String.valueOf(monthlySalary),
                "'" + jobTitle + "'"
        });

        if (employeeID == -1) {
            System.err.println("Error adding employee: " + name + ", " + age + ", " + sex + ", " + race + ", "
                    + monthlySalary + ", " + jobTitle);
            return -1;
        }
        return employeeID;
    }
    // "CREATE TABLE IncomeSource ("
    // + "sourceID INTEGER, "
    // + "day DATE, "
    // + "lodgeID INTEGER, "
    // + "sourceName VARCHAR(50), "
    // + "dailyIncome INTEGER, "
    // + "PRIMARY KEY (sourceID))",

    // * adds an income source with the given name to the database
    // ! note: income sources are per day and per lodge. It is maybe better to have
    // an income source entity but this goes
    private static int addRandomIncomeSource(Connection dbconn, Random rand, int lodgeID, String sourceName,
            java.sql.Date day) {
        // IncomeSource: sourceID, day, lodgeID, sourceName, dailyIncome

        // ! we want to ensure out source names with the way ours works right now
        // String[] sourceNames = { "Lift ticket sales", "Ski pass sales", "Equipment
        // rentals", "Food and beverage sales",
        // "Merchandise sales", "Ski lessons", "Ski patrol services", "Parking sales" };
        // String sourceName = sourceNames[rand.nextInt(sourceNames.length)]; //
        // randomly select a source name
        int dailyIncome = rand.nextInt(1000) + 100; // random daily income between $100 and $1100

        int result = addToTable(dbconn, "IncomeSource", "sourceID", new String[] {
                "TO_DATE('" + day + "', 'YYYY-MM-DD')", String.valueOf(lodgeID), sourceName, String.valueOf(dailyIncome)
        });

        if (result == -1) {
            System.err.println("Error adding income source: " + sourceName + ", " + dailyIncome + ", " + day + ", "
                    + lodgeID);
            return -1;
        }
        return result;
    }

    
    // * Random populate helpers!
    // * generates a random string of lowercase letters of the given length
    // if length is -1, generates a random length between 4 and 10, 1st letter capitalized
    private static String randStr(Random rand, int length) {
        if (length == -1) {
            length = rand.nextInt(7) + 4; // generate random length between 4 and 10
        }
        
        char[] randomChars = new char[length];
        for (int i = 0; i < length; i++) {
            randomChars[i] = (char) ('a' + rand.nextInt(26)); // generate random lowercase letter
        }
        randomChars[0] = Character.toUpperCase(randomChars[0]); // capitalize the first letter
        return new String(randomChars);
    }
    
    // * gets the next name from the list of names
    private static String nextName() {
        if (currentName >= names.length) {
            return "outOfNames" + currentName; // if we run out of names, just return bob + number
        }
        return names[currentName++];
    }
    
    // * generates a random ID for the given table and ID name
    private static long randLong(Random rand, long min, long max) {
        return min + (long) (rand.nextDouble() * (max - min)); // generate random long between min and max
    }
    
    // * Adds a row to the table, creates a random primary key for it and returns it
    // doesn't work on lift and trail tables (bc. no id primary key)
    private static int addToTable(Connection dbconn, String tableName, String idName, String[] values) {
        int id = generateRandomID(dbconn, tableName, idName); // Generate a random ID for the new entry

        String sql = "INSERT INTO " + tableName + " VALUES (" + id + ", " + String.join(", ", values) + ")";
        try (Statement stmt = dbconn.createStatement()) {
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            System.err.println("Error adding to table " + tableName + ": " + e.getMessage());
            System.out.println("SQL Statement: " + sql);
            return -1; // Return -1 in case of an error
        }
        return id; // Return the generated ID
    }

    // * adds a row to the table (doesn't generate a random ID for it)
    private static int rawAddToTable(Connection dbconn, String tableName, String[] values) {
        String sql = "INSERT INTO " + tableName + " VALUES (" + String.join(", ", values) + ")";
        try (Statement stmt = dbconn.createStatement()) {
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            System.err.println("Error adding to table " + tableName + ": " + e.getMessage());
            System.out.println("SQL Statement: " + sql);
            return -1; // Return -1 in case of an error
        }
        return 0;
    }
    // * random entity generator (end)
    
    // * makes a random ID for the given table and column name
    private static int generateRandomID(Connection dbconn, String tableName, String columnName) {
        int i = 0;
        while (true) {
            int randomID = new java.util.Random().nextInt(100000); // Generate random ID

            // counts how many already have that ID
            // count everything from tableName where columnName = randomID
            try (PreparedStatement checkStmt = dbconn
                    .prepareStatement("SELECT COUNT(*) FROM " + tableName + " WHERE " + columnName + " = ? ")) {
                checkStmt.setInt(1, randomID);

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
    // #endregion // * Populate tables

}