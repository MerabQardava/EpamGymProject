package com.epam.hw.messaging;

import com.epam.hw.dto.ActionType;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
@Slf4j
public class MessageProducer {

    private final JmsTemplate jmsTemplate;
    private final ObjectMapper objectMapper;

    @Autowired
    public MessageProducer(JmsTemplate jmsTemplate, ObjectMapper objectMapper) {
        this.jmsTemplate = jmsTemplate;
        this.objectMapper = objectMapper;
    }

    public void sendMessage(Object payload, ActionType actionType) {
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
                return message;
            });
            log.info("Sent async message with ActionType: {}, TransactionID: {}", actionType, transactionId);
        } catch (Exception e) {
            log.error("Failed to send message with ActionType: {}, TransactionID: {}: {}", actionType, transactionId, e.getMessage());
            throw new RuntimeException("Failed to send JMS message", e);
        }
    }
}