package ru.obukhov.trader.test.utils.matchers;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.mockito.ArgumentMatcher;
import ru.obukhov.trader.common.util.DecimalUtils;

import java.math.BigDecimal;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class BigDecimalMatcher implements ArgumentMatcher<BigDecimal> {

    private final BigDecimal value;

    public static BigDecimalMatcher of(final double value) {
        return new BigDecimalMatcher(BigDecimal.valueOf(value));
    }

    @Override
    public boolean matches(final BigDecimal otherValue) {
        return DecimalUtils.numbersEqual(value, otherValue);
    }

}