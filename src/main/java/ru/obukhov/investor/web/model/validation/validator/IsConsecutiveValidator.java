package ru.obukhov.investor.web.model.validation.validator;

import ru.obukhov.investor.web.model.IntervalContainer;
import ru.obukhov.investor.web.model.validation.constraint.IsConsecutive;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.OffsetDateTime;

/**
 * Validates that {@link IntervalContainer#getTo} is after {@link IntervalContainer#getFrom}
 */
public class IsConsecutiveValidator implements ConstraintValidator<IsConsecutive, IntervalContainer> {

    @Override
    public boolean isValid(IntervalContainer intervalContainer, ConstraintValidatorContext constraintValidatorContext) {
        OffsetDateTime from = intervalContainer.getFrom();
        OffsetDateTime to = intervalContainer.getTo();

        return from == null || to == null || to.isAfter(from);

    }

}