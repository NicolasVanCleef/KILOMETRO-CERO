package com.uruguaymnatural.backend.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Small corrections to tables created by earlier versions of the app, run when the backend starts.
 *
 * Hibernate (ddl-auto=update) adds new tables and columns by itself, but it never changes a
 * rule that already exists. When the "businesses" table was first created it got a rule
 * saying the status can only be PENDING, APPROVED or REJECTED; the REMOVED status came later
 * and that old rule rejects it. Dropping the rule is safe: the status is still limited to
 * the values of the BusinessStatus enum in the code.
 */
@Component
@Order(0)
public class SchemaFixes implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    public SchemaFixes(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        jdbcTemplate.execute("alter table businesses drop constraint if exists businesses_status_check");
    }
}
