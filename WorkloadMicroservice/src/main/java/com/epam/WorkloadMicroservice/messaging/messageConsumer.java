package com.epam.WorkloadMicroservice.messaging;

import com.epam.WorkloadMicroservice.dto.GetWorkingHoursDTO;
import com.epam.WorkloadMicroservice.dto.UpdateWorkingHoursDTO;
import com.epam.WorkloadMicroservice.security.JWTService;
import com.epam.WorkloadMicroservice.service.TrainerService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.jboss.logging.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class messageConsumer {
    private final JWTService jwtService;
    private final TrainerService trainerService;

    @Autowired
    public messageConsumer(JWTService jwtService, TrainerService trainerService) {
        this.jwtService = jwtService;
        this.trainerService = trainerService;
    }

    @JmsListener(destination = "workload.queue")
    public String handleMessage(String message, @Header("Authorization") String jwt,
                              @Header("ActionType") String action,
                              @Header("Username") String username,
                              @Header("TransactionID") String transactionId) throws JsonProcessingException {

        if (jwt == null || !jwt.startsWith("Bearer ") ||
                !jwtService.validateToken(jwt.substring(7))) {
            throw new SecurityException("Invalid JWT. Message rejected.");
        }
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());


        String txId = transactionId;
        if (txId == null || txId.isBlank()) {
            txId = UUID.randomUUID().toString().substring(0, 8);
        }

        MDC.put("txId", txId);


        if(action.equals("ADD")) {
            trainerService.addTraining(username,objectMapper.readValue(message, UpdateWorkingHoursDTO.class));
        }else if(action.equals("REMOVE")) {
            trainerService.removeTraining(username,objectMapper.readValue(message, UpdateWorkingHoursDTO.class));
        }else if (action.equals("HOURS")) {
            try{
                GetWorkingHoursDTO dto = objectMapper.readValue(message, GetWorkingHoursDTO.class);
                return String.valueOf(trainerService.getTotalHours(username,dto.YearNumber(),dto.MonthNumber()));
            }catch (IllegalArgumentException e) {
                System.out.println("Error processing message: " + e.getMessage());
                return "Error: " + e.getMessage();
            } catch (Exception e) {
                System.out.println("Unexpected error processing message: " + e.getMessage());
                return "Error: Unexpected error - " + e.getMessage();
            }
        }else{
            throw new IllegalArgumentException("Unknown ActionType: " + action);
        }

        System.out.println("Received message: " + message);
        System.out.println("Received token: " + jwt);
        return null;
    }

}
