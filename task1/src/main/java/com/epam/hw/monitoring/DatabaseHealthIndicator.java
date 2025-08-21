package com.epam.hw.monitoring;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


@Component
public class DatabaseHealthIndicator implements HealthIndicator {

    @Value("${spring.datasource.url}")
    private String databaseUrl;

    @Value("${spring.datasource.username}")
    private String databaseUsername;

    @Value("${spring.datasource.password}")
    private String databasePassword;

    @Value("${spring.datasource.rdbms}")
    private String rdbms;


    private boolean isDatabaseUp() {
        try {
            Connection connection = DriverManager.getConnection(databaseUrl,databaseUsername,databasePassword);
            connection.close();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    @Override
    public Health health() {
        try {
            if (isDatabaseUp()) {
                return Health.up()
                        .withDetail("database", rdbms)
                        .withDetail("url", databaseUrl)
                        .withDetail("status", "Connected")
                        .build();
            } else {
                return Health.down()
                        .withDetail("database", rdbms)
                        .withDetail("url", databaseUrl)
                        .withDetail("status", "Connection failed")
                        .build();
            }
        } catch (Exception e) {
            return Health.down()
                    .withDetail("database", rdbms)
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}
