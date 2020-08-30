package ru.obukhov.investor.web.model.validation.constraint;

import ru.obukhov.investor.web.model.validation.validator.DayCandleIntervalValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Field fo type {@link ru.tinkoff.invest.openapi.models.market.CandleInterval}, annotated
 * by @GetSaldosRequestIntervalConstraint is validated by {@link DayCandleIntervalValidator}
 */
@Documented
@Constraint(validatedBy = DayCandleIntervalValidator.class)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DayCandleIntervalConstraint {

    String message() default "candleInterval must be one of: " +
            "['1min', '2min', '3min', '5min', '10min', '15min', '30min', 'hour', '2hour', '4hour']";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}