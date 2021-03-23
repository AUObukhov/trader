package ru.obukhov.trader.web.model.validation.validator;

import ru.obukhov.trader.web.model.IntervalContainer;
import ru.obukhov.trader.web.model.validation.constraint.IsConsecutive;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.OffsetDateTime;

/**
 * Validates that {@link IntervalContainer#getTo} is after {@link IntervalContainer#getFrom} or one of them is null
 */
public class IsConsecutiveValidator implements ConstraintValidator<IsConsecutive, IntervalContainer> {

    @Override
    public boolean isValid(IntervalContainer intervalContainer, ConstraintValidatorContext constraintValidatorContext) {
        OffsetDateTime from = intervalContainer.getFrom();
        OffsetDateTime to = intervalContainer.getTo();

        return from == null || to == null || to.isAfter(from);
    }

}