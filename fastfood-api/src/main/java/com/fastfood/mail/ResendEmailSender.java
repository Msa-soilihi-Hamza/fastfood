package com.fastfood.mail;

import com.fastfood.common.ApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;
import java.util.Map;

/** Envoie les e-mails avec l'API HTTP de Resend (https://resend.com/docs/api-reference/emails/send-email). */
@Slf4j
public class ResendEmailSender implements EmailSender {

    private final RestClient client;
    private final String from;

    public ResendEmailSender(String apiKey, String from) {
        this.from = from;
        this.client = RestClient.builder()
                .baseUrl("https://api.resend.com")
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .build();
    }

    @Override
    public void send(String to, String subject, String html, String text) {
        try {
            client.post()
                    .uri("/emails")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("from", from, "to", List.of(to), "subject", subject, "html", html, "text", text))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientResponseException e) {
            // La réponse de Resend explique la cause (clé invalide, domaine non vérifié…) ; jamais la clé elle-même
            log.error("Resend a refusé l'envoi à {} : HTTP {} {}", to, e.getStatusCode().value(), e.getResponseBodyAsString());
            throw unavailable();
        } catch (RestClientException e) {
            log.error("Resend injoignable : {}", e.getMessage());
            throw unavailable();
        }
    }

    private static ApiException unavailable() {
        return new ApiException(HttpStatus.SERVICE_UNAVAILABLE,
                "L'e-mail n'a pas pu être envoyé. Réessayez dans un instant.");
    }
}
