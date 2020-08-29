package ru.obukhov.investor.web.model.validation.constraint;

import ru.obukhov.investor.web.model.validation.validator.IsConsecutiveValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = IsConsecutiveValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface IsConsecutive {

    String message() default "'from' expected to be before 'to'";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
