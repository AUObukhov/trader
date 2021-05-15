package ru.obukhov.trader.common.model.validation.constraint;

import ru.obukhov.trader.common.model.validation.validator.PredicateConstraintValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.function.Predicate;

/**
 * Type, annotated by @PredicateConstraint is validated by {@link PredicateConstraintValidator}
 */
@Documented
@Constraint(validatedBy = PredicateConstraintValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@SuppressWarnings("unused")
public @interface PredicateConstraint {

    String message() default "predicate condition is not met";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    Class<? extends Predicate<?>> predicate();

}