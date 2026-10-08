package com.fastfood.mail;

/** Envoi d'un e-mail. L'implémentation dépend de la configuration (Resend ou console). */
public interface EmailSender {

    void send(String to, String subject, String html, String text);
}
