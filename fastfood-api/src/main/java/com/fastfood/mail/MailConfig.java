package com.fastfood.mail;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class MailConfig {

    @Bean
    public EmailSender emailSender(@Value("${app.mail.resend-api-key}") String apiKey,
                                   @Value("${app.mail.from}") String from) {
        if (apiKey.isBlank()) {
            log.warn("Aucune clé Resend (RESEND_API_KEY) : les e-mails seront affichés dans la console au lieu d'être envoyés");
            return new ConsoleEmailSender();
        }
        log.info("E-mails envoyés avec Resend depuis {}", from);
        return new ResendEmailSender(apiKey.trim(), from);
    }
}
