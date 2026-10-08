package com.fastfood.auth.password;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Mot de passe robuste : 12 caractères minimum, dont au moins un caractère spécial (! ? @ # …). */
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = StrongPasswordValidator.class)
public @interface StrongPassword {

    String message() default "Le mot de passe doit contenir au moins 12 caractères, dont un symbole (! ? @ # …)";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
