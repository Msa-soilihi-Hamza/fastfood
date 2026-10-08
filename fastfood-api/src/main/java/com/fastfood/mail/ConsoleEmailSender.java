package com.fastfood.mail;

import lombok.extern.slf4j.Slf4j;

/**
 * Développement sans clé Resend : l'e-mail n'est pas envoyé, son contenu (et donc le code)
 * est écrit dans la console de l'API. À ne jamais utiliser en production.
 */
@Slf4j
public class ConsoleEmailSender implements EmailSender {

    @Override
    public void send(String to, String subject, String html, String text) {
        log.info("""

                ===== E-mail non envoyé (aucune clé Resend) =====
                À       : {}
                Objet   : {}
                {}
                =================================================""", to, subject, text);
    }
}
