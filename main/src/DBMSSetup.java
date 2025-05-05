// Authors: Luckie Musngi and Aj Becerra

import java.io.IOException;
import java.sql.*;

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

    // Table Creation Statements
    static final String[] tableCreateStatements = new String[] {
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

    private static void makeTables(Connection dbconn) {
        for (int i = 0; i < tableNames.length; i++) {
            String tableName = tableNames[i];
            String createTableSQL = tableCreateStatements[i];

            // makes the current table
            try {
                Statement stmt = dbconn.createStatement();
                // System.out.println("Table made " + tableName + "");
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

    // * Table Creation (End)
    // ---------------------------------------------------------------------
}
