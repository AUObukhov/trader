package ru.obukhov.trader.web.model.validation.constraint;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import ru.obukhov.trader.web.model.validation.validator.NotAllNullValidator;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = NotAllNullValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@SuppressWarnings("unused")
public @interface NotAllNull {

    String message() default "at least one of params must be not null";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}