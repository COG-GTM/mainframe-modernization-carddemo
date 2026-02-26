package com.carddemo.transaction.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom validation constraint ensuring that at least one of
 * accountId or cardNumber is provided.
 *
 * Replaces the COBOL EVALUATE TRUE block in VALIDATE-INPUT-KEY-FIELDS
 * which checks: "Account or Card Number must be entered..."
 */
@Documented
@Constraint(validatedBy = AccountOrCardValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface AccountOrCard {

    String message() default "Account ID or Card Number must be provided";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
