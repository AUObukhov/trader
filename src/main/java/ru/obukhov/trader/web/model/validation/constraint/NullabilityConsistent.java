package ru.obukhov.trader.web.model.validation.constraint;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import ru.obukhov.trader.web.model.validation.validator.NullabilityConsistentValidator;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Type, annotated by @NullabilityConsistent is validated by {@link NullabilityConsistentValidator}
 */
@Documented
@Constraint(validatedBy = NullabilityConsistentValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@SuppressWarnings("unused")
public @interface NullabilityConsistent {

    String message() default "several fields must be all null or not null";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    String[] fields();

}