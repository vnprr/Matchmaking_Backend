package com.matchmaking.backend.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = StrongPasswordValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface StrongPassword {
    String message() default "Hasło musi zawierać co najmniej jedną wielką literę, jedną małą literę i jedną cyfrę";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}