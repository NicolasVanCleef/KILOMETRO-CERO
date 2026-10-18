package com.uruguaymnatural.backend.service;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.springframework.stereotype.Service;

/**
 * Checks that the backend can reach the database.
 */
@Service
public class HealthService {

    private static final int TIMEOUT_SECONDS = 2;

    private final DataSource dataSource;

    public HealthService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public boolean isDatabaseUp() {
        try (Connection connection = dataSource.getConnection()) {
            return connection.isValid(TIMEOUT_SECONDS);
        } catch (SQLException e) {
            return false;
        }
    }
}
