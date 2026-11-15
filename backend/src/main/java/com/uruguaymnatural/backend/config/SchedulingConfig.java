package com.uruguaymnatural.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Turns on the automatic tasks (methods marked with @Scheduled).
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {
}
