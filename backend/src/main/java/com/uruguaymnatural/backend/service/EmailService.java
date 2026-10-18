package com.uruguaymnatural.backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Sends emails to users.
 *
 * For now the emails are simulated: they are written to the backend log instead of being
 * sent through a real mail server. Replacing this class with a real implementation
 * (for example, Spring's JavaMailSender) does not change the rest of the code.
 */
@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    public void send(String to, String subject, String body) {
        log.info("""

                ===== CORREO (simulado) =====
                Para:    {}
                Asunto:  {}
                {}
                =============================""", to, subject, body);
    }
}
