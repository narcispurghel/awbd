package com.github.irinabotea.webui.web.form;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PasswordsMatchValidator.class)
public @interface PasswordsMatch {
    String message() default "Passwords do not match";

    String field() default "newPassword";

    String confirmField() default "confirmPassword";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
