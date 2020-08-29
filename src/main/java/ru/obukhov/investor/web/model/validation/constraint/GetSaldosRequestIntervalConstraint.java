package ru.obukhov.investor.web.model.validation.constraint;

import ru.obukhov.investor.web.model.validation.validator.GetSaldosRequestIntervalValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = GetSaldosRequestIntervalValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface GetSaldosRequestIntervalConstraint {

    String message() default "Interval between 'from' and 'to' is shorter then 'candleInterval'";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}