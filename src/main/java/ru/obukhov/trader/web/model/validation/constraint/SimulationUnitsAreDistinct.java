package ru.obukhov.trader.web.model.validation.constraint;

import ru.obukhov.trader.web.model.pojo.SimulationUnit;
import ru.obukhov.trader.web.model.validation.validator.SimulationUnitsAreDistinctValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Collection field of base type {@link SimulationUnit} and annotated by @SimulationUnitsAreDistinct is validated by
 * {@link SimulationUnitsAreDistinctValidator}
 */
@Documented
@Constraint(validatedBy = SimulationUnitsAreDistinctValidator.class)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@SuppressWarnings("unused")
public @interface SimulationUnitsAreDistinct {

    String message() default "simulation units in collection must contain different tickers";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}