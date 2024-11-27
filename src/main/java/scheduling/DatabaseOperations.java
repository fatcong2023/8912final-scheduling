package scheduling;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatabaseOperations {
    private static final String CONNECTION_URL = "jdbc:sqlserver://8912finallab.database.windows.net:1433;database=xiao8915;user=xiao@8912finallab;password=Zz300312!;encrypt=true;trustServerCertificate=false;hostNameInCertificate=*.database.windows.net;loginTimeout=30;";

    static {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Failed to load SQL Server JDBC driver", e);
        }
    }

    public static int insertAppointment(String scheduledDateTime, String username, String universalBankNumber, String notes) throws Exception {
        String insertSQL = "INSERT INTO BloodDonationSchedule (ScheduledDateTime, Username, UniversalBankNumber, Notes, CreatedTimestamp, UpdatedTimestamp) " +
                           "OUTPUT INSERTED.AppointmentID " +
                           "VALUES (?, ?, ?, ?, GETDATE(), GETDATE())";

        try (Connection connection = DriverManager.getConnection(CONNECTION_URL);
             PreparedStatement preparedStatement = connection.prepareStatement(insertSQL)) {

            preparedStatement.setString(1, scheduledDateTime);
            preparedStatement.setString(2, username);
            preparedStatement.setString(3, universalBankNumber);
            preparedStatement.setString(4, notes);

            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("AppointmentID");
            } else {
                throw new Exception("Failed to retrieve AppointmentID after insertion.");
            }
        }
    }

    public static Map<String, Object> getAppointmentDetails(int appointmentId) throws Exception {
        String selectSQL = "SELECT AppointmentID, ScheduledDateTime, Username, UniversalBankNumber, Status, Notes, CreatedTimestamp, UpdatedTimestamp " +
                           "FROM BloodDonationSchedule WHERE AppointmentID = ?";

        try (Connection connection = DriverManager.getConnection(CONNECTION_URL);
             PreparedStatement preparedStatement = connection.prepareStatement(selectSQL)) {

            // Set the parameter
            preparedStatement.setInt(1, appointmentId);

            // Execute query
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                // Map the result to a JSON-friendly structure
                Map<String, Object> appointmentDetails = new HashMap<>();
                appointmentDetails.put("AppointmentID", resultSet.getInt("AppointmentID"));
                appointmentDetails.put("ScheduledDateTime", resultSet.getString("ScheduledDateTime"));
                appointmentDetails.put("Username", resultSet.getString("Username"));
                appointmentDetails.put("UniversalBankNumber", resultSet.getString("UniversalBankNumber"));
                appointmentDetails.put("Status", resultSet.getString("Status"));
                appointmentDetails.put("Notes", resultSet.getString("Notes"));
                appointmentDetails.put("CreatedTimestamp", resultSet.getString("CreatedTimestamp"));
                appointmentDetails.put("UpdatedTimestamp", resultSet.getString("UpdatedTimestamp"));

                return appointmentDetails;
            } else {
                return null; // No record found
            }
        }
    }


    public static List<Map<String, Object>> getAppointmentsByUsername(String username) throws Exception {
        String selectSQL = "SELECT AppointmentID, ScheduledDateTime, Username, UniversalBankNumber, Status, Notes, CreatedTimestamp, UpdatedTimestamp " +
                           "FROM BloodDonationSchedule WHERE Username = ?";

        List<Map<String, Object>> appointments = new ArrayList<>();

        try (Connection connection = DriverManager.getConnection(CONNECTION_URL);
             PreparedStatement preparedStatement = connection.prepareStatement(selectSQL)) {

            // Set the parameter
            preparedStatement.setString(1, username);

            // Execute query
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                // Map each row to a JSON-friendly structure
                Map<String, Object> appointment = new HashMap<>();
                appointment.put("AppointmentID", resultSet.getInt("AppointmentID"));
                appointment.put("ScheduledDateTime", resultSet.getString("ScheduledDateTime"));
                appointment.put("Username", resultSet.getString("Username"));
                appointment.put("UniversalBankNumber", resultSet.getString("UniversalBankNumber"));
                appointment.put("Status", resultSet.getString("Status"));
                appointment.put("Notes", resultSet.getString("Notes"));
                appointment.put("CreatedTimestamp", resultSet.getString("CreatedTimestamp"));
                appointment.put("UpdatedTimestamp", resultSet.getString("UpdatedTimestamp"));

                appointments.add(appointment);
            }
        }

        return appointments;
    }


    public static boolean deleteAppointment(int appointmentId) throws Exception {
        String deleteSQL = "DELETE FROM BloodDonationSchedule WHERE AppointmentID = ?";

        try (Connection connection = DriverManager.getConnection(CONNECTION_URL);
             PreparedStatement preparedStatement = connection.prepareStatement(deleteSQL)) {

            // Set the parameter
            preparedStatement.setInt(1, appointmentId);

            // Execute the update
            int rowsAffected = preparedStatement.executeUpdate();

            // Return true if a row was deleted, false otherwise
            return rowsAffected > 0;
        }
    }

}