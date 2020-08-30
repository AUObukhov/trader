package ru.obukhov.investor.web.model.validation.validator;

import com.google.common.collect.ImmutableMap;
import org.springframework.util.Assert;
import ru.obukhov.investor.util.DateUtils;
import ru.obukhov.investor.web.model.GetDailySaldosRequest;
import ru.obukhov.investor.web.model.validation.constraint.GetSaldosRequestIntervalConstraint;
import ru.tinkoff.invest.openapi.models.market.CandleInterval;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Map;

/**
 * Validates that {@link GetDailySaldosRequest#candleInterval} is less than interval between {@link GetDailySaldosRequest#from}
 * and {@link GetDailySaldosRequest#to}
 */
public class GetSaldosRequestIntervalValidator implements ConstraintValidator<GetSaldosRequestIntervalConstraint, GetDailySaldosRequest> {

    private static final Map<CandleInterval, Long> CANDLE_INTERVALS_TO_MINUTES =
            ImmutableMap.<CandleInterval, Long>builder()
                    .put(CandleInterval.ONE_MIN, 1L)
                    .put(CandleInterval.TWO_MIN, 2L)
                    .put(CandleInterval.THREE_MIN, 3L)
                    .put(CandleInterval.FIVE_MIN, 5L)
                    .put(CandleInterval.TEN_MIN, 10L)
                    .put(CandleInterval.QUARTER_HOUR, 15L)
                    .put(CandleInterval.HALF_HOUR, 30L)
                    .put(CandleInterval.HOUR, 60L)
                    .put(CandleInterval.TWO_HOURS, 2 * 60L)
                    .put(CandleInterval.FOUR_HOURS, 4 * 60L)
                    .put(CandleInterval.DAY, 24 * 60L)
                    .put(CandleInterval.WEEK, 7 * 24 * 60L)
                    .put(CandleInterval.MONTH, 28 * 24 * 60L)
                    .build();

    @Override
    public boolean isValid(GetDailySaldosRequest request, ConstraintValidatorContext constraintValidatorContext) {
        OffsetDateTime from = DateUtils.getDefaultFromIfNull(request.getFrom());
        OffsetDateTime to = DateUtils.getDefaultToIfNull(request.getTo());

        Long minutes = CANDLE_INTERVALS_TO_MINUTES.get(request.getCandleInterval());
        Assert.notNull(minutes, "Unknown candle interval - " + request.getCandleInterval());

        return Duration.between(from, to).toMinutes() >= minutes;
    }

}