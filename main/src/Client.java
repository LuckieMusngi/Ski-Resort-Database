
import java.sql.*;

public class Client {

    public static void main(String[] args) throws Exception {
        System.out.println("Hello, World!");
    }

    // region // * update
    // * update member in the database
    public static boolean updateMember(Connection conn, int memberId, String newPhone, String newEmail,
            String newEmergencyContact) {
        String sql = "UPDATE Member SET phoneNumber = ?, email = ?, emergencyContact = ? WHERE memberID = ?";
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
        String sql = "UPDATE Equipment SET type = ?, size = ? WHERE equipmentID = ?";
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

    // #endregion update
    // #region // * delete
    // * delete member in the database
    public static boolean deleteMember(Connection conn, int memberId) {
        try {
            // Check active ski passes, rentals, lessons
            String activeSkiPass = "SELECT COUNT(*) FROM SkiPass WHERE memberID = ? AND expirationDate >= CURRENT_DATE";
            String activeRental = "SELECT COUNT(*) FROM EquipmentRental WHERE memberID = ? AND returnStatus = 'not returned'";
            String activeLesson = "SELECT COUNT(*) FROM LessonOrder WHERE memberID = ? AND usedStatus = 'unused'";

            if (hasOpenRecords(conn, activeSkiPass, memberId)
                    || hasOpenRecords(conn, activeRental, memberId)
                    || hasOpenRecords(conn, activeLesson, memberId)) {
                System.out.println("Cannot delete member: active ski passes, open rentals, or unused lessons exist.");
                return false;
            }

            conn.setAutoCommit(false);
            try {
                // Delete lessons
                try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM LessonOrder WHERE memberID = ?")) {
                    stmt.setInt(1, memberId);
                    stmt.executeUpdate();
                }

                // Delete rentals
                try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM EquipmentRental WHERE memberID = ?")) {
                    stmt.setInt(1, memberId);
                    stmt.executeUpdate();
                }

                // Delete ski passes
                try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM SkiPass WHERE memberID = ?")) {
                    stmt.setInt(1, memberId);
                    stmt.executeUpdate();
                }

                // Delete member
                try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM Member WHERE memberID = ?")) {
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
        String checkSql = "SELECT rentalStatus FROM EquipmentInventory WHERE equipmentID = ?";
        try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            checkStmt.setInt(1, equipmentId);
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next()) {
                    String status = rs.getString("rentalStatus");
                    if ("rented".equalsIgnoreCase(status) || "reserved".equalsIgnoreCase(status)) {
                        System.out.println("Cannot delete equipment: it is currently rented or reserved.");
                        return false;
                    }

                    // Archive equipment
                    String archiveSql = "UPDATE EquipmentInventory SET status = 'archived' WHERE equipmentID = ?";
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
        String checkSql = "SELECT remainingSessions, totalSessions FROM LessonOrder WHERE orderID = ?";
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

                    String deleteSql = "DELETE FROM LessonOrder WHERE orderID = ?";
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
        String checkSql = "SELECT returnStatus FROM EquipmentRental WHERE rentalID = ?";
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
                        Instructor.name,
                        LessonOrder.scheduledTime
                    FROM
                        LessonOrder
                    JOIN LessonToOrder ON LessonOrder.orderID = LessonToOrder.orderID
                    JOIN Lesson ON LessonToOrder.LessonID = Lesson.LessonID
                    JOIN Instructor ON Lesson.instructorID = Instructor.instructorID
                    WHERE
                        LessonOrder.memberID = ?
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
                        GearRental.returnStatus,
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
    public static void getTrailINfo(Connection conn) {
        String sql = """
                    SELECT
                        Trail.trailName,
                        Trail.category,
                        Lift.liftName
                    FROM
                        Trail
                    JOIN TrailLift ON Trail.trailName = TrailLift.trailName
                    JOIN Lift ON TrailLift.liftName = Lift.liftName
                    WHERE
                        (Trail.difficulty = 'Beginner-Intermediate' OR Trail.difficulty = 'Intermediate-Expert')
                        AND Trail.status = 'Open'
                        AND Lift.status = 'Open'
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
    public static void getMemberInfo(Connection conn, int MemberID) {
        String sql = """
                    SELECT
                        SkiPass.skiPassID
                    FROM
                        SkiPass
                    WHERE
                        SkiPass.memberID = ?
                """;
        String sql2 = """
                Select
                    GearRental.RentalID
                FROM
                    GEAR
                WhHERE
                    GearRental.memberID = ?
                """;
        String sql3 = """
                    SELECT
                        Equipment.type,
                        Equipment.size
                    FROM
                        Equipment
                    JOIN GearRental ON RentalEquipment.rentalID = GearRental.rentalID
                    WHERE
                        GearRental.memberID = ?
                """;
        boolean hasSkiPass = false;
        boolean hasRental = false;

        try (
                PreparedStatement pstmt = conn.prepareStatement(sql);) {
            hasSkiPass = true;
        } catch (SQLException e) {
            System.err.println("Error fetching skiPass: " + e.getMessage());
        }
        if (hasSkiPass) {
            try (
                    PreparedStatement pstmt2 = conn.prepareStatement(sql2);) {
                hasRental = true;
            } catch (SQLException e) {
                System.err.println("Error fetching rental: " + e.getMessage());
            }
        } else {
            System.out.println("Member does not have a ski pass.");
        }
        if (hasRental) {
            try (
                    PreparedStatement pstmt3 = conn.prepareStatement(sql3);) {

            } catch (SQLException e) {
                System.err.println("Error fetching equipment: " + e.getMessage());
            }
        } else {
            System.out.println("Member does not have a rental.");
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
    //#endregion // * queries
}
