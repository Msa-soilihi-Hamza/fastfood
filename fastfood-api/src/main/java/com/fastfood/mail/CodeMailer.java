package com.fastfood.mail;

import com.fastfood.auth.ChallengePurpose;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;

/** Rédige et envoie l'e-mail contenant un code de vérification. */
@Component
@RequiredArgsConstructor
public class CodeMailer {

    private final EmailSender emailSender;

    public void sendCode(String to, String firstName, ChallengePurpose purpose, String code, long validMinutes) {
        String action = purpose == ChallengePurpose.VERIFY_EMAIL
                ? "confirmer votre adresse e-mail et activer votre compte"
                : "terminer votre connexion";
        String subject = purpose == ChallengePurpose.VERIFY_EMAIL
                ? "Confirmez votre inscription : " + code
                : "Votre code de connexion : " + code;

        String text = """
                Bonjour %s,

                Voici votre code pour %s :

                %s

                Il est valable %d minutes. Si vous n'êtes pas à l'origine de cette demande, ignorez cet e-mail \
                et ne communiquez ce code à personne.
                """.formatted(firstName, action, code, validMinutes);

        String html = """
                <div style="font-family:Arial,sans-serif;max-width:480px;margin:auto;color:#000">
                  <p style="font-size:20px;font-weight:bold">Fastfood<span style="color:#048848">.</span></p>
                  <p>Bonjour %s,</p>
                  <p>Voici votre code pour %s :</p>
                  <p style="font-size:32px;font-weight:bold;letter-spacing:8px;background:#f3f3f3;padding:16px;text-align:center;border-radius:12px">%s</p>
                  <p style="color:#5e5e5e;font-size:14px">Il est valable %d minutes. Si vous n'êtes pas à l'origine de cette demande,
                  ignorez cet e-mail et ne communiquez ce code à personne.</p>
                </div>
                """.formatted(HtmlUtils.htmlEscape(firstName), action, code, validMinutes);

        emailSender.send(to, subject, html, text);
    }
}
