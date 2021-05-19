package ru.obukhov.trader.test.utils.matchers;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.mockito.ArgumentMatcher;
import org.quartz.CronExpression;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CronExpressionMatcher implements ArgumentMatcher<CronExpression> {

    private final CronExpression value;

    public static CronExpressionMatcher of(CronExpression value) {
        return new CronExpressionMatcher(value);
    }

    @Override
    public boolean matches(CronExpression otherValue) {
        return value == null
                ? otherValue == null
                : value.toString().equals(otherValue.toString());
    }

}