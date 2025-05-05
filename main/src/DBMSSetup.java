// Authors: Luckie Musngi and Aj Becerra

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.io.IOException;
import java.sql.*;

public class DBMSSetup {

    public static void main(String[] args) throws Exception {
        Connection dbconn = getDbconn(); // connect to the database
    }

    // * gets and returns a connection to the database
    private static Connection getDbconn() {
        final String oracleURL
                = // Magic lectura -> aloe access spell
                "jdbc:oracle:thin:@aloe.cs.arizona.edu:1521:oracle";

        String username = null, // Oracle DBMS username
                password = null;    // Oracle DBMS password

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

    // * Table Creation ---------------------------------------------------------------------------
    // Tables to be Made:
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

    // Table Creation Statements
    static final String[] tableCreateStatements = new String[]{
        // Member: memberID, name, phone#, email, dob, emergency contact
        "create table Member ("
        + "memberID integer, "
        + "name varchar(50), "
        + "email varchar(50), "
        + "dob date, "
        + "emergencyContact varchar(50), "
        + "primary key (memberID))",
        // Ski pass: skiPassID, price, timeOfPurchase, expDate, totalUses, remainingUses. passType, status, memberID, rentalID
        "",
        // Gear Rental: rentalID,  startDate, expDate, return status, status, skiPassID
        "create table GearRental ("
        + "rentalID integer, "
        + "startDate date, "
        + "expDate date, "
        + "returnStatus varchar(50), "
        + "status varchar(50), "
        + "skiPassID integer, "
        + "primary key (rentalID),"
        + "foreign key (skiPassID) references SkiPass(skiPassID))",
        // Equipment: EquipmentID, type, size, status

        // Equipment Update: equipmentUpdateID, equipmentID, type, notes
        "create table EquipmentUpdate ("
        + "equipmentUpdateID integer, "
        + "equipmentID integer, "
        + "type varchar(20), "
        + "notes varchar(50), "
        + "primary key (equipmentUpdateID),"
        + "foreign key (equipmentID) references Equipment(equipmentID))",
        // Gear Rental Update: rentalUpdateID, rentalID, type, notes
        "create table GearRentalUpdate ("
        + "rentalUpdateID integer, "
        + "rentalID integer, "
        + "type varchar(20), "
        + "notes varchar(200), "
        + "primary key (rentalUpdateID),"
        + "foreign key (rentalID) references GearRental(rentalID))",
        // Trail: trailName, location, difficulty, category, status
        "create table Trail ("
        + "trailName varchar(50), "
        + "location varchar(50), "
        + "difficulty varchar(20), "
        + "category varchar(20), "
        + "status varchar(10))"
        + "primary key (trailName)",
        // Lift: liftName, ability level, openTime, closeTime, status

        // LessonOrder: lessonOrderID, memberID, lessonsPurchased, remainingSessions
        "create table LessonOrder ("
        + "lessonOrderID integer, "
        + "memberID integer, "
        + "lessonsPurchased integer, "
        + "remainingSessions integer, "
        + "primary key (lessonOrderID),"
        + "foreign key (memberID) references Member(memberID))",
        // Lesson: LessonID, lessonName, EmployeeID

        // LessonSession: SessionID, Date, startTime, endTime, lessonID
        "create table LessonSession ("
        + "sessionID integer, "
        + "date date, "
        + "startTime time, "
        + "endTime time, "
        + "lessonID integer, "
        + "primary key (sessionID),"
        + "foreign key (lessonID) references Lesson(lessonID))",
        // Instructor: EmployeeID, Certification level

        // Employee: EmployeeID, Name, age, sex, race, monthly salary, job title
        "create table Employee ("
        + "employeeID integer, "
        + "name varchar(50), "
        + "age integer"
        + "sex varchar(20), "
        + "race varchar(20), "
        + "monthlySalary integer, "
        + "jobTitle varchar(50), "
        + "primary key (employeeID))",
        // Lodge: lodgeID, Location

        // IncomeSource: sourceID, day, lodgeID, sourceName, dailyIncome
        "create table IncomeSource ("
        + "sourceID integer, "
        + "day date, "
        + "lodgeID integer, "
        + "sourceName varchar(50), "
        + "dailyIncome integer, "
        + "primary key (sourceID),"
        + "foreign key (lodgeID) references Lodge(lodgeID))"
        // Shuttle: shuttleID, location, capacity, status
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

    // * Table Creation (End) ---------------------------------------------------------------------
}
