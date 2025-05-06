
import java.sql.*;
import java.util.Scanner;

public class Interface {

    private static boolean printDebug = false; // true = print

    public static void main(String[] args) throws Exception {
        Connection dbconn = getDbconn();

        System.out.println("This is the [] Ski Resort Database User Interface.");
        System.out.println("Here, you may add/update/delete records or query records.");
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("Would you like to modify records (m), query records (q),  or exit (e):");
            String input = scanner.nextLine();

            switch (input.toLowerCase().charAt(0)) {
                case 'm' -> {
                    // Modify records
                    prepareModifyRecords(dbconn, scanner);
                }

                case 'q' -> {
                    // Query records
                    queryRecords(dbconn, scanner);
                }

                case 'p' -> {
                    String tableName = (String) getArgument(scanner, "table name", 1);
                    printTable(dbconn, tableName);
                }

                case 'e' -> {
                    // Exit
                    System.out.println("Exiting.");
                    dbconn.close();
                    scanner.close(); // close the scanner
                    return;
                }

                default ->
                    // ask again
                    System.out.println("Invalid input. Please try again.");
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

    private static int prepareModifyRecords(Connection dbconn, Scanner scanner) {
        System.out.println("Which record would you like to add/update/delete?");
        char entity = ' '; // arbitrary, gets set in following loop
        while (true) {
            System.out.println(
                    "Member (m), Ski Pass (s), Equipment Inventory (l), Equipment Rental (r), Purchase Order (p), or Exit to go back (e)");

            String input = scanner.nextLine();
            entity = input.toLowerCase().charAt(0); // get the first character of the input

            if (entity == 'm' || entity == 's' || entity == 'l' || entity == 'r' || entity == 'p') {
                break; // valid input, exit
            } else if (entity == 'e') {
                System.out.println("Exiting.");
                return 0; // exit current method
            } else {
                System.out.println("Invalid input. Please try again.");
            }
        }

        char action = ' '; // arbitrary, gets set in following loop
        while (true) {
            System.out.println("Would you like to Add (a), Update (u), Delete (d), or Exit (e):");
            String input = scanner.nextLine();
            action = input.toLowerCase().charAt(0); // get the first character of the input

            if (action == 'a' || action == 'u' || action == 'd') {
                break; // valid input, exit
            } else if (action == 'e') {
                System.out.println("Exiting.");
                return 0; // exit current method
            } else {
                System.out.println("Invalid input. Please try again.");
            }
        }

        return modifyRecords(dbconn, scanner, action, entity); // call the modifyRecords method with the selected action
        // and entity
    }

    private static int modifyRecords(Connection dbconn, Scanner scanner, char action, char entity) {
        switch (action) {
            case 'a' -> {
                switch (entity) {
                    case 'm' -> {
                        System.out.println("Adding a new member...");
                        System.out.println(
                                "This needs the following parameters: name, phone#, email, dob, emergency contact");
                        String name = (String) getArgument(scanner, "name", 1);
                        String phone = (String) getArgument(scanner, "phone#", 1);
                        String email = (String) getArgument(scanner, "email", 1);
                        java.sql.Date dob = (java.sql.Date) getArgument(scanner, "dob", 2);
                        String emergencyContact = (String) getArgument(scanner, "emergency contact", 1);
                        int memberID = addMember(dbconn, name, phone, email, dob, emergencyContact);
                        if (memberID != -1) {
                            System.out.println("Member added with ID: " + memberID);
                        } else {
                            System.out.println("Error adding member.");
                            return -1;
                        }
                    }
                    case 's' -> {
                        System.out.println("Adding a new ski pass...");
                        System.out.println(
                                "This needs the following parameters: price, timeOfPurchase, expDate, totalUses, passType, memberID, rentalID");
                        int skiPassID = generateRandomID(dbconn, "SkiPass", "skiPassID");
                        int price = (int) getArgument(scanner, "price (numeric amount in cents)", 0);
                        java.sql.Date timeOfPurchase = java.sql.Date.valueOf(java.time.LocalDate.now()); // default
                        // current date
                        java.sql.Date expDate = (java.sql.Date) getArgument(scanner, "expiration date", 2);
                        int totalUses = (int) getArgument(scanner, "total uses", 0);
                        int remainingUses = totalUses; // default to total uses
                        String passType = (String) getArgument(scanner, "pass type", 1);
                        String status = "Active"; // default to active
                        int memberID = (int) getArgument(scanner, "member ID", 0);
                        int rentalID = (int) getArgument(scanner, "rental ID", 0);
                        // ! ski pass doesn't make its own ID
                        int result = addSkiPass(dbconn, skiPassID, price, timeOfPurchase, expDate, totalUses,
                                remainingUses, passType, status, memberID, rentalID);
                        if (result != -1) {
                            System.out.println("Ski pass added with ID: " + skiPassID);
                        } else {
                            System.out.println("Error adding ski pass.");
                            return -1;
                        }
                    }
                    case 'l' -> {
                        System.out.println("Adding a new lesson order...");
                        System.out.println("This needs the following parameters: memberID, lessonsPurchased");
                        int memberID = (int) getArgument(scanner, "member ID", 0);
                        int lessonsPurchased = (int) getArgument(scanner, "lessons purchased", 0);
                        int remainingSessions = lessonsPurchased; // default to lessons purchased
                        int lessonOrderID = generateRandomID(dbconn, "LessonOrder", "lessonOrderID");
                        // ! lesson order doesn't make its own ID
                        int result = addLessonOrder(dbconn, lessonOrderID, memberID, lessonsPurchased,
                                remainingSessions);
                        if (result != -1) {
                            System.out.println("Lesson order added with ID: " + lessonOrderID);
                        } else {
                            System.out.println("Error adding lesson order.");
                            return -1;
                        }
                    }
                    case 'r' -> {
                        System.out.println("Adding a new gear rental...");
                        System.out.println(
                                "This needs the following parameters: skiPassID, rentedEquipmentTypes (comma separated)");
                        java.sql.Date startDate = java.sql.Date.valueOf(java.time.LocalDate.now()); // default current
                        // date
                        String returnStatus = "Not Returned"; // default to Not Returned
                        String status = "Active"; // default to active
                        int skiPassID = (int) getArgument(scanner, "ski pass ID", 0);
                        int rentalID = generateRandomID(dbconn, "GearRental", "rentalID");
                        // ! rental doesn't make its own ID
                        String[] rentedEquipmentTypeStrs = (String[]) getArgument(scanner,
                                "rented equipment types (comma separated)", 3);
                        int[] rentedEquipmentTypes = new int[rentedEquipmentTypeStrs.length];

                        // convert back into int array
                        // [0]ski boots [1]ski poles [2]skis [3]snowboards [4]helmet
                        for (int i = 0; i < rentedEquipmentTypeStrs.length; i++) {
                            switch (rentedEquipmentTypeStrs[i].toLowerCase()) {
                                case "ski boots" ->
                                    rentedEquipmentTypes[i] = 0;
                                case "ski poles" ->
                                    rentedEquipmentTypes[i] = 1;
                                case "skis" ->
                                    rentedEquipmentTypes[i] = 2;
                                case "snowboard" ->
                                    rentedEquipmentTypes[i] = 3;
                                case "helmet" ->
                                    rentedEquipmentTypes[i] = 4;
                                default ->
                                    throw new IllegalArgumentException(
                                            "Invalid equipment type: " + rentedEquipmentTypeStrs[i]);
                            }
                        }

                        int result = addGearRental(dbconn, rentalID, startDate, returnStatus, status, skiPassID,
                                rentedEquipmentTypes);
                        if (result != -1) {
                            System.out.println("Gear rental added with ID: " + rentalID);
                        } else {
                            System.out.println("Error adding gear rental.");
                            return -1;
                        }
                    }
                    case 'p' -> {
                        System.out.println("Adding new equipment...");
                        System.out.println("This needs the following parameters: type, size");
                        int equipmentID = generateRandomID(dbconn, "Equipment", "equipmentID");
                        String type = (String) getArgument(scanner, "type", 1);
                        String size = (String) getArgument(scanner, "size", 1);
                        String status = "Active"; // default to active
                        int result = addEquipment(dbconn, equipmentID, type, size, status);
                        if (result != -1) {
                            System.out.println("Equipment added with ID: " + equipmentID);
                        } else {
                            System.out.println("Error adding equipment.");
                            return -1;
                        }
                    }
                }
            }
            case 'u' -> {
                switch (entity) {
                    case 'm' -> {
                        // ! rather faulty one might say TODO
                        System.out.println("Updating a member...");
                        System.out.println(
                                "This needs the following parameters: memberID, new phone#, new email, new emergency contact");
                        int memberID = (int) getArgument(scanner, "member ID", 0);
                        String newPhone = (String) getArgument(scanner, "new phone#", 1);
                        String newEmail = (String) getArgument(scanner, "new email", 1);
                        String newEmergencyContact = (String) getArgument(scanner, "new emergency contact", 1);
                        boolean result = updateMember(dbconn, memberID, newPhone, newEmail, newEmergencyContact);
                        if (result) {
                            System.out.println("Member updated successfully.");
                        } else {
                            System.out.println("Error updating member.");
                            return -1;
                        }
                    }
                    case 's' -> {
                        System.out.println("Updating a ski pass...");
                        System.out.println("This needs the following parameters: skiPassID, new remaining uses");
                        int skiPassID = (int) getArgument(scanner, "ski pass ID", 0);
                        int newRemainingUses = (int) getArgument(scanner, "new remaining uses", 0);
                        boolean result = updateSkiPassUsage(dbconn, skiPassID, newRemainingUses);
                        if (result) {
                            System.out.println("Ski pass updated successfully.");
                        } else {
                            System.out.println("Error updating ski pass.");
                            return -1;
                        }
                    }
                    case 'l' -> {
                        System.out.println("Updating a lesson order...");
                        System.out
                                .println("This needs the following parameters: lessonOrderID, new remaining sessions");
                        int lessonOrderID = (int) getArgument(scanner, "lesson order ID", 0);
                        int newRemainingSessions = (int) getArgument(scanner, "new remaining sessions", 0);
                        boolean result = updateLessonUsage(dbconn, lessonOrderID, newRemainingSessions);
                        if (result) {
                            System.out.println("Lesson order updated successfully.");
                        } else {
                            System.out.println("Error updating lesson order.");
                            return -1;
                        }
                    }
                    case 'r' -> {
                        System.out.println("Updating a gear rental...");
                        System.out.println("This needs the following parameters: rentalID, return status (true/false)");
                        int rentalID = (int) getArgument(scanner, "rental ID", 0);
                        boolean isReturned = (boolean) getArgument(scanner, "return status (true/false)", 0);
                        boolean result = updateEquipmentRental(dbconn, rentalID, isReturned);
                        if (result) {
                            System.out.println("Gear rental updated successfully.");
                        } else {
                            System.out.println("Error updating gear rental.");
                            return -1;
                        }
                    }
                    case 'p' -> {
                        System.out.println("Updating equipment...");
                        System.out.println("This needs the following parameters: equipmentID, new type, new size");
                        int equipmentID = (int) getArgument(scanner, "equipment ID", 0);
                        String newType = (String) getArgument(scanner, "new type", 1);
                        String newSize = (String) getArgument(scanner, "new size", 1);
                        boolean result = updateEquipment(dbconn, equipmentID, newType, newSize);
                        if (result) {
                            System.out.println("Equipment updated successfully.");
                        } else {
                            System.out.println("Error updating equipment.");
                            return -1;
                        }
                    }
                }
            }
            case 'd' -> {
                switch (entity) {
                    case 'm' -> {
                        System.out.println("Deleting a member...");
                        System.out.println("This needs the following parameters: memberID");
                        int memberID = (int) getArgument(scanner, "member ID", 0);
                        boolean result = deleteMember(dbconn, memberID);
                        if (result) {
                            System.out.println("Member deleted successfully.");
                        } else {
                            System.out.println("Error deleting member.");
                            return -1;
                        }
                    }
                    case 's' -> {
                        System.out.println("Deleting a ski pass...");
                        System.out.println("This needs the following parameters: skiPassID");
                        int skiPassID = (int) getArgument(scanner, "ski pass ID", 0);
                        boolean result = deleteSkiPass(dbconn, skiPassID);
                        if (result) {
                            System.out.println("Ski pass deleted successfully.");
                        } else {
                            System.out.println("Error deleting ski pass.");
                            return -1;
                        }
                    }
                    case 'l' -> {
                        System.out.println("Deleting a lesson order...");
                        System.out.println("This needs the following parameters: lessonOrderID");
                        int lessonOrderID = (int) getArgument(scanner, "lesson order ID", 0);
                        boolean result = updateLessonUsage(dbconn, lessonOrderID, 0); // set remaining sessions to 0
                        if (result) {
                            System.out.println("Lesson order deleted successfully.");
                        } else {
                            System.out.println("Error deleting lesson order.");
                            return -1;
                        }
                    }
                    case 'r' -> {
                        System.out.println("Deleting a gear rental...");
                        System.out.println("This needs the following parameters: rentalID");
                        int rentalID = (int) getArgument(scanner, "rental ID", 0);
                        boolean result = updateEquipmentRental(dbconn, rentalID, true); // set return status to true
                        if (result) {
                            System.out.println("Gear rental deleted successfully.");
                        } else {
                            System.out.println("Error deleting gear rental.");
                            return -1;
                        }
                    }
                    case 'p' -> {
                        System.out.println("Deleting equipment...");
                        System.out.println("This needs the following parameters: equipmentID");
                        int equipmentID = (int) getArgument(scanner, "equipment ID", 0);
                        boolean result = updateEquipment(dbconn, equipmentID, "deleted", "deleted"); // mark as deleted
                        if (result) {
                            System.out.println("Equipment deleted successfully.");
                        } else {
                            System.out.println("Error deleting equipment.");
                            return -1;
                        }
                    }
                }
            }
        }

        return 0;
    }

    // * getArgument helper
    private static boolean isEquipment(String equipment) {
        // Check if the equipment is valid (e.g., "Ski boots", "Ski poles", etc.)
        String[] validEquipments = {"Ski boots", "Ski poles", "Skis", "Snowboard", "Helmet"};
        for (String validEquipment : validEquipments) {
            if (equipment.equalsIgnoreCase(validEquipment)) {
                return true; // valid equipment
            }
        }
        return false; // invalid equipment
    }

    // * getArgument: get an argument from the user
    static final String[] typeStr = {"int", "String", "Date", "equipment array (comma separated)"}; // types of
    // arguments

    private static Object getArgument(Scanner scanner, String argName, int type) {
        while (true) {
            System.out.print("Enter " + argName + ":");

            String input = scanner.nextLine();
            if (input.isEmpty()) {
                System.out.println("Input is empty. Enter a valid " + typeStr[type]);
                continue; // ask again
            }

            try {
                switch (type) {
                    case 0 -> {
                        return Integer.valueOf(input); // int
                    }
                    case 1 -> {
                        return input; // String
                    }
                    case 2 -> {
                        return java.sql.Date.valueOf(input); // Date
                    }
                    case 3 -> { // Handle equipment array (comma-separated values) ["Ski boots", "Ski poles",
                        // "Skis", "Snowboard", "Helmet"]
                        String[] equipments = input.split(",");
                        for (int i = 0; i < equipments.length; i++) {
                            equipments[i] = equipments[i].trim(); // trim whitespace
                            if (equipments[i].isEmpty()) {
                                continue; // skip empties
                            }
                            if (!isEquipment(equipments[i])) {
                                System.out.println(
                                        "The valid equipment types are: Ski boots, Ski poles, Skis, Snowboard, Helmet.");
                                throw new IllegalArgumentException("Invalid equipment type: " + equipments[i]);
                            }
                        }
                        return equipments;
                    }
                    default ->
                        throw new IllegalArgumentException("Invalid type: " + type);
                }
            } catch (IllegalArgumentException e) {
                System.out.println("Invalid input. Enter a valid " + typeStr[type]);
                if (type == 2) {
                    System.out.println("Dates are in YYYY-MM-DD.");
                }
                // it goes again
            }
        }
    }

    private static int queryRecords(Connection dbconn, Scanner scanner) {
        while (true) {
            System.out.println("Which query would you like to access (1, 2, 3, 4) or exit to go back (e):");
            System.out.println("1. For a given member, list all the ski lessons they have purchased, including the number of remaining\r\n"
                    + //
                    "sessions, instructor name, and scheduled time.");
            System.out.println("2. For a given ski pass, list all lift rides and equipment rentals associated with it, along with timestamps\r\n"
                    + //
                    "and return status.");
            System.out.println("3. List all open trails suitable for intermediate-level skiers, along with their category and connected lifts\r\n"
                    + //
                    "that are currently operational.");
            System.out.println("4. Lists all the ski passees, rentals, and equipment rented from a given memberID");
            String input = scanner.nextLine();

            switch (input.toLowerCase().charAt(0)) {
                case '1' -> {
                    // query1: getMemberLessons
                    System.out.println("Querying member lessons: ");
                    int memberID = (int) getArgument(scanner, "member ID", 0);
                    getMemberLessons(dbconn, memberID);
                }

                case '2' -> {
                    // query2: getSkiPassInfo
                    System.out.println("Querying ski pass info: ");
                    int skiPassID = (int) getArgument(scanner, "ski pass ID", 0);
                    getSkiPassInfo(dbconn, skiPassID);
                }

                case '3' -> {
                    // query3: getTrailInfo
                    System.out.println("Querying open intermediate trails: ");
                    getTrailInfo(dbconn);
                }

                case '4' -> {
                    // query4: getMemberInfo
                    System.out.println("Querying member info: ");
                    int memberID = (int) getArgument(scanner, "member ID", 0);
                    getMemberInfo(dbconn, memberID);
                }

                case 'e' -> {
                    // Exit
                    System.out.println("Exiting.");
                    return 0;
                }

                default ->
                    // ask again
                    System.out.println("Invalid input. Please try again.");
            }
        }
    }

    private static Connection getDbconn() {
        final String oracleURL
                = // Magic lectura -> aloe access spell
                "jdbc:oracle:thin:@aloe.cs.arizona.edu:1521:oracle";

        String username = null, // Oracle DBMS username
                password = null; // Oracle DBMS password

        java.util.Scanner scanner = new java.util.Scanner(System.in);
        System.out.print("Enter your Oracle DBMS username: ");
        username = scanner.nextLine();
        System.out.print("Enter your Oracle DBMS password: ");
        password = scanner.nextLine();

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

    // #region // * add
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
    private static int addEquipment(Connection conn, int equipmentID, String type, String size, String status) {
        try {
            // int equipmentID = generateRandomID(conn, "Equipment", "equipmentID"); //
            // generate ID for the new equipment

            String sql = "INSERT INTO Equipment (equipmentID, type, eSize, status) VALUES (?, ?, ?, ?)";
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
    private static int addGearRental(Connection conn, int rentalID, java.sql.Date startDate,
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
            String[] types = {"Ski boots", "Ski poles", "Skis", "Snowboard", "Helmet"};

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

    // #endregion // * add
    // region // * update
    // * update member in the database
    public static boolean updateMember(Connection conn, int memberId, String newPhone, String newEmail,
            String newEmergencyContact) {
        String sql = "UPDATE Member SET phone = ?, email = ?, emergencyContact = ? WHERE memberID = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newPhone);
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
                    if (expirationDate.after(new java.util.Date()) && remainingUses >= 0 && newRemainingUses >= 0) {
                        // update remaining uses, make sure then new value is non negative
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
        String sql = "UPDATE Equipment SET type = ?, eSize = ? WHERE equipmentID = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newType);
            pstmt.setString(2, newSize);
            pstmt.setInt(3, equipmentId);

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                // Log the update in the equipmentUpdate table
                String note = String.format("Updated equipment type to '%s' and size to '%s'", newType, newSize);
                logEquipmentUpdate(conn, equipmentId, "UPDATE", note);
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
        String sql = "UPDATE LessonOrder SET remainingSessions = ? WHERE lessonOrderID = ?";
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
        String updateSql = "UPDATE GearRental SET returnStatus = ? WHERE rentalID = ?";
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

    // #endregion update
    // #region // * delete
    // * delete member in the database
    // * delete member in the database
    public static boolean deleteMember(Connection conn, int memberId) {
        try {
            // Check active ski passes, rentals, lessons
            String activeSkiPass = """
                    SELECT
                        COUNT(*)
                    FROM
                        SkiPass
                    WHERE
                        SkiPass.memberID = ?
                        AND SkiPass.expDate >= CURRENT_DATE
                    """;

            String activeRental = """
                    SELECT
                        COUNT(*)
                    FROM
                        GearRental
                    JOIN
                        SkiPass ON GearRental.skiPassID = SkiPass.skiPassID
                    WHERE
                        SkiPass.memberID = ?
                        AND GearRental.returnStatus = 'not returned'
                    """;

            String activeLesson = """
                    SELECT
                        COUNT(*)
                    FROM
                        LessonOrder
                    WHERE
                        LessonOrder.memberID = ?
                        AND LessonOrder.remainingSessions < LessonOrder.lessonsPurchased
                    """;

            if (hasOpenRecords(conn, activeSkiPass, memberId)
                    || hasOpenRecords(conn, activeRental, memberId)
                    || hasOpenRecords(conn, activeLesson, memberId)) {
                System.out.println("Cannot delete member: active ski passes, open rentals, or unused lessons exist.");
                return false;
            }

            conn.setAutoCommit(false);
            try {
                // Delete lesson orders
                try (PreparedStatement stmt = conn.prepareStatement(
                        "DELETE FROM LessonOrder WHERE memberID = ?")) {
                    stmt.setInt(1, memberId);
                    stmt.executeUpdate();
                }

                // Delete gear rentals via skiPass lookup
                try (PreparedStatement stmt = conn.prepareStatement(
                        "DELETE FROM GearRental WHERE skiPassID IN (\n" +
                                "    SELECT skiPassID FROM SkiPass WHERE memberID = ?\n" +
                                ")")) {
                    stmt.setInt(1, memberId);
                    stmt.executeUpdate();
                }

                // Delete ski passes
                try (PreparedStatement stmt = conn.prepareStatement(
                        "DELETE FROM SkiPass WHERE memberID = ?")) {
                    stmt.setInt(1, memberId);
                    stmt.executeUpdate();
                }

                // Finally delete the member
                try (PreparedStatement stmt = conn.prepareStatement(
                        "DELETE FROM Member WHERE memberID = ?")) {
                    stmt.setInt(1, memberId);
                    stmt.executeUpdate();
                }

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
        String checkSql = "SELECT status FROM Equipment WHERE equipmentID = ?";
        try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            checkStmt.setInt(1, equipmentId);
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next()) {
                    String status = rs.getString("status");
                    if ("rented".equalsIgnoreCase(status) || "reserved".equalsIgnoreCase(status)) {
                        System.out.println("Cannot delete equipment: it is currently rented or reserved.");
                        return false;
                    }

                    // Archive equipment
                    String archiveSql = "UPDATE Equipment SET status = 'archived' WHERE equipmentID = ?";
                    try (PreparedStatement archiveStmt = conn.prepareStatement(archiveSql)) {
                        archiveStmt.setInt(1, equipmentId);
                        int updated = archiveStmt.executeUpdate();
                        if (updated > 0) {
                            logEquipmentUpdate(conn, equipmentId, "DELETE", "Equipment marked as archived.");
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
    public static boolean deleteLessonOrder(Connection conn, int orderId) {
        String checkSql = "SELECT remainingSessions, totalSessions FROM LessonOrder WHERE lessonOrderID = ?";
        try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            checkStmt.setInt(1, orderId);
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next()) {
                    int remaining = rs.getInt("remainingSessions");
                    int total = rs.getInt("totalSessions");
                    if (remaining != total) {
                        System.out.println("Cannot delete: some sessions have already been used.");
                        return false;
                    }

                    String deleteSql = "DELETE FROM LessonOrder WHERE lessonOrderID = ?";
                    try (PreparedStatement deleteStmt = conn.prepareStatement(deleteSql)) {
                        deleteStmt.setInt(1, orderId);
                        return deleteStmt.executeUpdate() > 0;
                    }
                } else {
                    System.out.println("Lesson Order not found.");
                    return false;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error deleting lesson order: " + e.getMessage());
            return false;
        }
    }

    // * delete rental in the database
    public static boolean deleteEquipmentRental(Connection conn, int rentalId) {
        String checkSql = "SELECT returnStatus FROM GearRental WHERE rentalID = ?";
        try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            checkStmt.setInt(1, rentalId);
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next()) {
                    String status = rs.getString("returnStatus");
                    if (!"not returned".equalsIgnoreCase(status)) {
                        System.out.println("Cannot delete rental: equipment has already been returned/used.");
                        return false;
                    }
                } else {
                    System.out.println("Rental record not found.");
                    return false;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error checking rental status: " + e.getMessage());
            return false;
        }

        // Proceed to delete
        String deleteSql = "DELETE FROM GearRental WHERE rentalID = ?";
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

    // #endregion delete
    // #region // * update/delete/helpers + logging
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

    // Logs the gear rental update in the gearRentalUpdate table
    public static void logGearRentalUpdate(Connection conn, int rentalId, String type, String notes) {
        // Generate a new record ID for the gear rental update
        int nextId = generateRandomID(conn, "GearRentalUpdate", "rentalUpdateID");
        if (nextId == -1) {
            System.err.println("Failed to generate a unique gear rental update ID.");
            return;
        }

        String sql = "INSERT INTO GearRentalUpdate (rentalUpdateID, rentalID, type, notes, updateDate) VALUES (?, ?, ?, ?, CURRENT_DATE)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, nextId); // Unique update ID
            pstmt.setInt(2, rentalId); // Rental ID
            pstmt.setString(3, type); // Type of update ("ADD", "UPDATE", "DELETE")
            pstmt.setString(4, notes); // Description of change
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error logging gear rental update: " + e.getMessage());
        }
    }

    // logs changes in the equipment in the equipmentUpdate table
    public static void logEquipmentUpdate(Connection conn, int equipmentId, String type, String note) {
        int nextId = generateRandomID(conn, "EquipmentUpdate", "equipmentUpdateID");
        if (nextId == -1) {
            System.err.println("Failed to generate a unique equipment update ID.");
            return;
        }

        String sql = "INSERT INTO EquipmentUpdate (equipmentUpdateID, equipmentID, type, notes, updateDate) VALUES (?, ?, ?, ?, CURRENT_DATE)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, nextId); // Unique update ID
            pstmt.setInt(2, equipmentId); // Equipment ID
            pstmt.setString(3, type); // Type of update ("ADD", "UPDATE", "DELETE")
            pstmt.setString(4, note); // Description of what changed
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error logging equipment update: " + e.getMessage());
        }
    }

    // #endregion // * update/delete/helpers + logging
    // #region // * queries
    // for a member, get all purchased ski lessons, the number of remaining
    // sessions, the lesson time, and lesson instructor
    public static void getMemberLessons(Connection conn, int memberId) {
        String sql = """
                    SELECT
                        LessonOrder.remainingSessions,
                        Employee.name AS instructorName,
                        LessonSession.startTime as scheduledTime
                    FROM
                        LessonOrder
                    JOIN LessonToOrder ON LessonOrder.lessonOrderID = LessonToOrder.lessonOrderID
                    JOIN LessonSession ON LessonToOrder.SessionID = LessonSession.SessionID
                    JOIN Lesson ON LessonSession.lessonID = Lesson.lessonID
                    JOIN Instructor ON lesson.employeeID = Instructor.employeeID
                    JOIN Employee ON Instructor.employeeID = Employee.employeeID
                    WHERE
                        LessonOrder.memberID = ?
                        AND LessonSession.startTime > CURRENT_DATE
                """;

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, memberId);

            try (ResultSet res = pstmt.executeQuery()) {
                System.out.println("Lessons for member ID " + memberId + ":");
                while (res.next()) {
                    int remaining = res.getInt("remainingSessions");
                    String instructor = res.getString("instructorName");
                    Timestamp time = res.getTimestamp("scheduledTime");

                    System.out.println("Sessions remaining: " + remaining + ", Instructor: " + instructor
                            + ", Scheduled at: " + time);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching member lessons: " + e.getMessage());
        }
    }

    // for a ski pass, list all lift usage, and gear rentals with timestamps and
    // return status
    public static void getSkiPassInfo(Connection conn, int skiPassID) {
        // get
        String sql = """
                    SELECT
                        LiftPassUsage.liftName,
                        LiftPassUsage.timeUsed
                    FROM
                        LiftPassUsage
                    WHERE
                        LiftPassUsage.skiPassID = ?
                """;
        String sql2 = """
                    SELECT
                        GearRental.startDate,
                        GearRental.returnStatus
                    FROM
                        GearRental
                    WHERE
                        GearRental.skiPassID = ?
                """;

        try (
                PreparedStatement pstmt = conn.prepareStatement(sql); PreparedStatement pstmt2 = conn.prepareStatement(sql2);) {
            pstmt.setInt(1, skiPassID);
            pstmt2.setInt(1, skiPassID);

            try (ResultSet res = pstmt.executeQuery()) {
                System.out.println("Lift usage for ski pass ID " + skiPassID + ":");
                while (res.next()) {
                    String liftName = res.getString("liftName");
                    Timestamp timeUsed = res.getTimestamp("timeUsed");

                    System.out.println("Lift: " + liftName + ", Time used: " + timeUsed);
                }
            }

            try (ResultSet res2 = pstmt2.executeQuery()) {
                System.out.println("Gear rentals for ski pass ID " + skiPassID + ":");
                while (res2.next()) {
                    Date startDate = res2.getDate("startDate");
                    String returnStatus = res2.getString("returnStatus");

                    System.out.println("Start date: " + startDate + ", Return status: " + returnStatus);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error fetching ski pass info: " + e.getMessage());
        }
    }

    // for intermediates, list all open trails, their category, and the lifts that
    // service them
    public static void getTrailInfo(Connection conn) {
        String sql = """
                    SELECT DISTINCT
                        Trail.trailName,
                        Trail.category,
                        Lift.liftName
                    FROM
                        Trail
                    JOIN TrailLift ON Trail.trailName = TrailLift.trailName
                    JOIN Lift ON TrailLift.liftName = Lift.liftName
                    WHERE
                        (Trail.difficulty = 'Intermediate')
                        AND Trail.status = 'Active'
                        AND Lift.status = 'Active'
                """;
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            ResultSet res = pstmt.executeQuery();
            while (res.next()) {
                String trailName = res.getString("trailName");
                String category = res.getString("category");
                String liftName = res.getString("liftName");

                System.out.println("Trail: " + trailName + ", Category: " + category + ", Lift: " + liftName);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching trail info: " + e.getMessage());
        }
    }

    // given a memberID, do they have a ski pass?, do they have a rental?, if so
    // what equipment do they own?
    public static void getMemberInfo(Connection conn, int memberID) {
        String sql = """
                    SELECT
                        skiPassID
                    FROM
                        SkiPass
                    WHERE
                        memberID = ?
                """;

        String sql2 = """
                    SELECT
                        rentalID
                    FROM
                        GearRental
                    WHERE
                        memberID = ?
                """;

        String sql3 = """
                    SELECT
                        Equipment.type,
                        Equipment.size
                    FROM
                        Equipment
                    JOIN RentalEquipment ON Equipment.equipmentID = RentalEquipment.equipmentID
                    JOIN GearRental ON RentalEquipment.rentalID = GearRental.rentalID
                    WHERE
                        GearRental.memberID = ?
                """;

        boolean hasSkiPass = false;
        boolean hasRental = false;

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, memberID);
            ResultSet res = pstmt.executeQuery();
            if (res.next()) {
                hasSkiPass = true;
                System.out.println("Ski pass ID: " + res.getInt("skiPassID"));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching ski pass: " + e.getMessage());
        }

        if (hasSkiPass) {
            try (PreparedStatement pstmt2 = conn.prepareStatement(sql2)) {
                pstmt2.setInt(1, memberID);
                ResultSet res2 = pstmt2.executeQuery();
                if (res2.next()) {
                    hasRental = true;
                    System.out.println("Rental ID: " + res2.getInt("rentalID"));
                }
            } catch (SQLException e) {
                System.err.println("Error fetching rental: " + e.getMessage());
            }
        } else {
            System.out.println("Member does not have a ski pass.");
        }

        if (hasRental) {
            try (PreparedStatement pstmt3 = conn.prepareStatement(sql3)) {
                pstmt3.setInt(1, memberID);
                ResultSet res3 = pstmt3.executeQuery();
                System.out.println("Equipment owned:");
                while (res3.next()) {
                    String type = res3.getString("type");
                    String size = res3.getString("size");
                    System.out.println("- " + type + " (" + size + ")");
                }
            } catch (SQLException e) {
                System.err.println("Error fetching equipment: " + e.getMessage());
            }
        } else {
            System.out.println("Member does not have any rentals.");
        }
    }

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
    // #endregion // * queries
}
