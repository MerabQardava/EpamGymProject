package com.epam.hw.messaging;

import com.epam.hw.dto.ActionType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.jms.JMSException;
import jakarta.jms.TextMessage;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import jakarta.jms.Message;



@Service
@Slf4j
public class MessageProducer {

    private final JmsTemplate jmsTemplate;
    private final ObjectMapper objectMapper;

    @Autowired
    public MessageProducer(JmsTemplate jmsTemplate, ObjectMapper objectMapper) {
        this.jmsTemplate = jmsTemplate;
        this.objectMapper = objectMapper;
        this.jmsTemplate.setReceiveTimeout(5000);
    }

    public void sendMessage(String username,Object payload, ActionType actionType) {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (requestAttributes == null) {
            log.warn("No request attributes available for JMS message");
            throw new IllegalStateException("No request context available");
        }

        HttpServletRequest request = requestAttributes.getRequest();
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("Missing or invalid Authorization header");
            throw new IllegalStateException("Missing or invalid Authorization header");
        }

        String transactionId = (String) request.getAttribute("X-Transaction-ID");
        if (transactionId == null) {
            log.warn("Missing X-Transaction-ID");
            throw new IllegalStateException("Missing X-Transaction-ID");
        }

        String jwtToken = authHeader.substring(7);
        try {
            String payloadJson = objectMapper.writeValueAsString(payload);
            jmsTemplate.convertAndSend("workload.queue", payloadJson, message -> {
                message.setStringProperty("Authorization", "Bearer " + jwtToken);
                message.setStringProperty("ActionType", actionType.name());
                message.setStringProperty("TransactionID", transactionId);
                message.setStringProperty("Username", username);
                return message;
            });
            log.info("Sent async message with ActionType: {}, TransactionID: {}", actionType, transactionId);
        } catch (Exception e) {
            log.error("Failed to send message with ActionType: {}, TransactionID: {}: {}", actionType, transactionId, e.getMessage());
            throw new RuntimeException("Failed to send JMS message", e);
        }
    }


    public String sendAndReceive(String username, Object payload, ActionType actionType) {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (requestAttributes == null) {
            log.warn("No request attributes available for JMS message");
            throw new IllegalStateException("No request context available");
        }

        HttpServletRequest request = requestAttributes.getRequest();
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("Missing or invalid Authorization header");
            throw new IllegalStateException("Missing or invalid Authorization header");
        }

        String transactionId = (String) request.getAttribute("X-Transaction-ID");
        if (transactionId == null) {
            log.warn("Missing X-Transaction-ID");
            throw new IllegalStateException("Missing X-Transaction-ID");
        }

        String jwtToken = authHeader.substring(7);
        try {
            String payloadJson = objectMapper.writeValueAsString(payload);
            Message response = jmsTemplate.sendAndReceive("workload.queue", session -> {
                TextMessage msg = session.createTextMessage(payloadJson);
                msg.setStringProperty("Authorization", "Bearer " + jwtToken);
                msg.setStringProperty("ActionType", actionType.name());
                msg.setStringProperty("TransactionID", transactionId);
                msg.setStringProperty("Username", username);
                return msg;
            });

            if (response instanceof TextMessage) {
                String responseText = ((TextMessage) response).getText();
                log.info("Sent and received message with ActionType: {}, TransactionID: {}, Response: {}",
                        actionType, transactionId, responseText);
                return responseText;
            } else if (response == null) {
                log.warn("No response received for ActionType: {}, TransactionID: {}", actionType, transactionId);
                return null;
            } else {
                log.warn("Unexpected response type: {} for ActionType: {}, TransactionID: {}",
                        response.getClass().getName(), actionType, transactionId);
                return null;
            }
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize payload for ActionType: {}, TransactionID: {}: {}",
                    actionType, transactionId, e.getMessage());
            throw new RuntimeException("Failed to serialize JMS message payload", e);
        } catch (JMSException e) {
            log.error("Failed to send/receive message with ActionType: {}, TransactionID: {}: {}",
                    actionType, transactionId, e.getMessage());
            throw new RuntimeException("Failed to send/receive JMS message", e);
        }
    }

}