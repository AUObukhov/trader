package ru.obukhov.trader.test.utils.model.dividend;

import ru.obukhov.trader.common.util.DecimalUtils;
import ru.obukhov.trader.market.model.Currencies;
import ru.obukhov.trader.market.model.Dividend;
import ru.obukhov.trader.test.utils.model.DateTimeTestData;

public class TestDividends {

    private static final Dividend DIVIDEND1 = new Dividend(
            DecimalUtils.setDefaultScale(0.1),
            DateTimeTestData.newDateTime(2011, 8, 29, 3),
            DateTimeTestData.newDateTime(2011, 6, 30, 3),
            DateTimeTestData.newDateTime(2011, 5, 23, 3),
            "Regular Cash",
            DateTimeTestData.newDateTime(2011, 5, 25, 3),
            "Quarter",
            DecimalUtils.setDefaultScale(317.090000000),
            DecimalUtils.setDefaultScale(0.030000000),
            DateTimeTestData.newDateTime(2020, 12, 8, 14, 5, 33, 6257739)
    );

    private static final Dividend DIVIDEND2 = new Dividend(
            DecimalUtils.setDefaultScale(100),
            DateTimeTestData.newDateTime(2021, 8, 29, 3),
            DateTimeTestData.newDateTime(2021, 6, 30, 3),
            DateTimeTestData.newDateTime(2021, 5, 23, 3),
            "Cancelled",
            DateTimeTestData.newDateTime(2021, 5, 25, 3),
            "Annual",
            DecimalUtils.setDefaultScale(2000),
            DecimalUtils.setDefaultScale(0.04),
            DateTimeTestData.newDateTime(2022, 12, 8, 14, 5, 33, 6257739)
    );

    public static final TestDividend TEST_DIVIDEND1 = new TestDividend(DIVIDEND1, Currencies.RUB);
    public static final TestDividend TEST_DIVIDEND2 = new TestDividend(DIVIDEND2, Currencies.RUB);

}