package scheduling;

import com.microsoft.azure.functions.annotation.*;
import com.microsoft.azure.functions.*;

import java.util.logging.Logger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class RestApiFunc {
    @FunctionName("RestApiFunction")
    public HttpResponseMessage getHello(
        @HttpTrigger(name = "req",
                     methods = {HttpMethod.GET},
                     authLevel = AuthorizationLevel.ANONYMOUS,
                     route = "get") HttpRequestMessage<Optional<String>> request,
        final ExecutionContext context) {


        Logger logger = context.getLogger();
        logger.info("HTTP trigger function processed a request.");


        return request.createResponseBuilder(HttpStatus.OK)
                      .header("Content-Type", "application/json")
                      .body("Hello, Azure Functions!")
                      .build();
    }

    @FunctionName("CreateAppointment")
    public HttpResponseMessage createAppointment(
        @HttpTrigger(name = "req",
                     methods = {HttpMethod.POST},
                     authLevel = AuthorizationLevel.ANONYMOUS,
                     route = "appointments") HttpRequestMessage<Optional<String>> request,
        final ExecutionContext context) {

        Logger logger = context.getLogger();
        logger.info("Processing CreateAppointment request.");

        try {
            // Parse the request body
            String requestBody = request.getBody().orElseThrow(()-> new IllegalArgumentException("Request body is missing"));
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(requestBody);

            // Extract data from JSON
            String scheduledDateTime = jsonNode.get("ScheduledDateTime").asText();
            String username = jsonNode.get("Username").asText();
            String universalBankNumber = jsonNode.get("UniversalBankNumber").asText();
            String notes = jsonNode.has("Notes") ? jsonNode.get("Notes").asText() : null;

            // Call the database operation to insert the appointment
            int appointmentId = DatabaseOperations.insertAppointment(scheduledDateTime, username, universalBankNumber, notes);

            // Build the response
            Map<String, Object> response = new HashMap<>();

            response.put("AppointmentID", appointmentId);
            response.put("ScheduledDateTime", scheduledDateTime);
            response.put("Username", username);
            response.put("UniversalBankNumber", universalBankNumber);
            response.put("Notes", notes);
            response.put("Status", "Scheduled");

            return request.createResponseBuilder(HttpStatus.CREATED)
                          .header("Content-Type", "application/json")
                          .body(objectMapper.writeValueAsString(response))
                          .build();

        } catch (Exception e) {
            logger.severe("Error creating appointment: " + e.getMessage());
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                          .body("Error creating appointment: " + e.getMessage())
                          .build();
        }
    }

    @FunctionName("GetAppointmentDetails")
    public HttpResponseMessage getAppointmentDetails(
        @HttpTrigger(name = "req",
                     methods = {HttpMethod.GET},
                     authLevel = AuthorizationLevel.ANONYMOUS,
                     route = "appointments/{appointmentId}") HttpRequestMessage<Optional<String>> request,
        @BindingName("appointmentId") int appointmentId,
        final ExecutionContext context) {

        Logger logger = context.getLogger();
        logger.info("Processing GetAppointmentDetails request for AppointmentID: " + appointmentId);

        try {
            // Fetch appointment details from the database
            Map<String, Object> appointmentDetails = DatabaseOperations.getAppointmentDetails(appointmentId);

            if (appointmentDetails == null) {
                return request.createResponseBuilder(HttpStatus.NOT_FOUND)
                              .body("Appointment with ID " + appointmentId + " not found.")
                              .build();
            }

            // Return the appointment details as JSON
            return request.createResponseBuilder(HttpStatus.OK)
                          .header("Content-Type", "application/json")
                          .body(appointmentDetails)
                          .build();

        } catch (Exception e) {
            logger.severe("Error fetching appointment details: " + e.getMessage());
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                          .body("Error fetching appointment details: " + e.getMessage())
                          .build();
        }
    }


    @FunctionName("GetAppointmentsByUsername")
    public HttpResponseMessage getAppointmentsByUsername(
        @HttpTrigger(name = "req",
                     methods = {HttpMethod.GET},
                     authLevel = AuthorizationLevel.ANONYMOUS,
                     route = "appointments/username/{username}") HttpRequestMessage<Optional<String>> request,
        @BindingName("username") String username,
        final ExecutionContext context) {

        Logger logger = context.getLogger();
        logger.info("Processing GetAppointmentsByUsername request for Username: " + username);

        try {
            // Fetch the list of appointments for the given username
            List<Map<String, Object>> appointments = DatabaseOperations.getAppointmentsByUsername(username);

            if (appointments.isEmpty()) {
                // No appointments found for the user
                return request.createResponseBuilder(HttpStatus.NOT_FOUND)
                              .body("No appointments found for username: " + username)
                              .build();
            }

            // Return the list of appointments as JSON
            return request.createResponseBuilder(HttpStatus.OK)
                          .header("Content-Type", "application/json")
                          .body(appointments)
                          .build();

        } catch (Exception e) {
            logger.severe("Error fetching appointments for username: " + username + " - " + e.getMessage());
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                          .body("Error fetching appointments: " + e.getMessage())
                          .build();
        }
    }

    @FunctionName("DeleteAppointment")
    public HttpResponseMessage deleteAppointment(
        @HttpTrigger(name = "req",
                     methods = {HttpMethod.DELETE},
                     authLevel = AuthorizationLevel.ANONYMOUS,
                     route = "appointments/{appointmentId}") HttpRequestMessage<Optional<String>> request,
        @BindingName("appointmentId") int appointmentId,
        final ExecutionContext context) {

        Logger logger = context.getLogger();
        logger.info("Processing DeleteAppointment request for AppointmentID: " + appointmentId);

        try {
            // Perform the deletion in the database
            boolean deleted = DatabaseOperations.deleteAppointment(appointmentId);

            if (!deleted) {
                // Appointment not found
                return request.createResponseBuilder(HttpStatus.NOT_FOUND)
                              .body("Appointment with ID " + appointmentId + " not found.")
                              .build();
            }

            // Return a success response
            return request.createResponseBuilder(HttpStatus.NO_CONTENT)
                          .build();

        } catch (Exception e) {
            logger.severe("Error deleting appointment: " + e.getMessage());
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                          .body("Error deleting appointment: " + e.getMessage())
                          .build();
        }
    }    
}
