package scheduling;

import com.microsoft.azure.functions.annotation.*;
import com.microsoft.azure.functions.*;

import java.util.logging.Logger;
import java.util.ArrayList;
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
            String requestBody = request.getBody().orElseThrow(() -> new IllegalArgumentException("Request body is missing"));
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
    
}
