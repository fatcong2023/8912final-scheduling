package scheduling;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

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

            // Set parameters
            preparedStatement.setString(1, scheduledDateTime);
            preparedStatement.setString(2, username);
            preparedStatement.setString(3, universalBankNumber);
            preparedStatement.setString(4, notes);

            // Execute query and retrieve generated AppointmentID
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("AppointmentID");
            } else {
                throw new Exception("Failed to retrieve AppointmentID after insertion.");
            }
        }
    }

}