package ru.obukhov.trader.test.utils.matchers;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.mockito.ArgumentMatcher;
import ru.obukhov.trader.common.util.DecimalUtils;
import ru.obukhov.trader.market.model.PortfolioPosition;
import ru.obukhov.trader.test.utils.TestUtils;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PortfolioPositionMatcher implements ArgumentMatcher<PortfolioPosition> {

    private final PortfolioPosition value;

    public static PortfolioPositionMatcher of(final PortfolioPosition value) {
        return new PortfolioPositionMatcher(value);
    }

    @Override
    public boolean matches(final PortfolioPosition position) {
        return StringUtils.equals(value.ticker(), position.ticker())
                && value.instrumentType() == position.instrumentType()
                && DecimalUtils.numbersEqual(value.quantity(), position.quantity())
                && TestUtils.equals(value.averagePositionPrice(), position.averagePositionPrice())
                && DecimalUtils.numbersEqual(value.expectedYield(), position.expectedYield())
                && TestUtils.equals(value.currentPrice(), position.currentPrice())
                && DecimalUtils.numbersEqual(value.quantityLots(), position.quantityLots());
    }

}