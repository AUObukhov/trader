package ru.obukhov.trader.web.model.validation.validator;

import lombok.Data;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.obukhov.trader.common.util.DateUtils;
import ru.obukhov.trader.web.model.IntervalContainer;

import java.time.OffsetDateTime;

class IsConsecutiveValidatorUnitTest {

    @Test
    void isValid_returnsTrue_whenFromIsNullAndToIsNotNull() {
        IsConsecutiveValidator validator = new IsConsecutiveValidator();
        OffsetDateTime from = null;
        OffsetDateTime to = DateUtils.getDateTime(2021, 1, 1, 10, 0, 1);
        IntervalContainer intervalContainer = new IntervalContainerImpl(from, to);

        boolean result = validator.isValid(intervalContainer, null);

        Assertions.assertTrue(result);
    }

    @Test
    void isValid_returnsTrue_whenFromIsNotNullAndToIsNull() {
        IsConsecutiveValidator validator = new IsConsecutiveValidator();
        OffsetDateTime from = DateUtils.getDateTime(2021, 1, 1, 10, 0, 0);
        OffsetDateTime to = null;
        IntervalContainer intervalContainer = new IntervalContainerImpl(from, to);

        boolean result = validator.isValid(intervalContainer, null);

        Assertions.assertTrue(result);
    }

    @Test
    void isValid_returnsTrue_whenFromIsNullAndToIsNull() {
        IsConsecutiveValidator validator = new IsConsecutiveValidator();
        OffsetDateTime from = null;
        OffsetDateTime to = null;
        IntervalContainer intervalContainer = new IntervalContainerImpl(from, to);

        boolean result = validator.isValid(intervalContainer, null);

        Assertions.assertTrue(result);
    }

    @Test
    void isValid_returnsTrue_whenToIsAfterFrom() {
        IsConsecutiveValidator validator = new IsConsecutiveValidator();
        OffsetDateTime from = DateUtils.getDateTime(2021, 1, 1, 10, 0, 0);
        OffsetDateTime to = DateUtils.getDateTime(2021, 1, 1, 10, 0, 1);
        IntervalContainer intervalContainer = new IntervalContainerImpl(from, to);

        boolean result = validator.isValid(intervalContainer, null);

        Assertions.assertTrue(result);
    }

    @Test
    void isValid_returnsFalse_whenToIsEqualToFrom() {
        IsConsecutiveValidator validator = new IsConsecutiveValidator();
        OffsetDateTime from = DateUtils.getDateTime(2021, 1, 1, 10, 0, 0);
        OffsetDateTime to = DateUtils.getDateTime(2021, 1, 1, 10, 0, 0);
        IntervalContainer intervalContainer = new IntervalContainerImpl(from, to);

        boolean result = validator.isValid(intervalContainer, null);

        Assertions.assertFalse(result);
    }

    @Test
    void isValid_returnsFalse_whenToIsBeforeFrom() {
        IsConsecutiveValidator validator = new IsConsecutiveValidator();
        OffsetDateTime from = DateUtils.getDateTime(2021, 1, 1, 10, 0, 0);
        OffsetDateTime to = DateUtils.getDateTime(2021, 1, 1, 9, 0, 1);
        IntervalContainer intervalContainer = new IntervalContainerImpl(from, to);

        boolean result = validator.isValid(intervalContainer, null);

        Assertions.assertFalse(result);
    }

    @Data
    private static class IntervalContainerImpl implements IntervalContainer {
        private final OffsetDateTime from;
        private final OffsetDateTime to;
    }

}