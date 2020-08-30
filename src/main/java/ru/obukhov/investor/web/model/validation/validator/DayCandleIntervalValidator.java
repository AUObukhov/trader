package ru.obukhov.investor.web.model.validation.validator;

import com.google.common.collect.ImmutableList;
import ru.obukhov.investor.web.model.validation.constraint.DayCandleIntervalConstraint;
import ru.tinkoff.invest.openapi.models.market.CandleInterval;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.List;

public class DayCandleIntervalValidator implements ConstraintValidator<DayCandleIntervalConstraint, CandleInterval> {

    private static final List<CandleInterval> ALLOWED_DAILY_CANDLE_INTERVALS = ImmutableList.of(
            CandleInterval.ONE_MIN,
            CandleInterval.TWO_MIN,
            CandleInterval.THREE_MIN,
            CandleInterval.FIVE_MIN,
            CandleInterval.TEN_MIN,
            CandleInterval.QUARTER_HOUR,
            CandleInterval.HALF_HOUR,
            CandleInterval.HOUR,
            CandleInterval.TWO_HOURS,
            CandleInterval.FOUR_HOURS
    );

    @Override
    public boolean isValid(CandleInterval value, ConstraintValidatorContext context) {
        return ALLOWED_DAILY_CANDLE_INTERVALS.contains(value);
    }

}