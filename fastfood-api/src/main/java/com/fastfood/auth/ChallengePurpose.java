package com.fastfood.auth;

/** Pourquoi un code a été envoyé par e-mail. */
public enum ChallengePurpose {
    /** Confirmer l'adresse après l'inscription : le compte n'est actif qu'ensuite. */
    VERIFY_EMAIL,
    /** Deuxième étape de chaque connexion, après le mot de passe. */
    LOGIN
}
