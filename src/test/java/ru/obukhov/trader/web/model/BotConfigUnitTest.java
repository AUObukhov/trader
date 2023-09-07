package ru.obukhov.trader.web.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.obukhov.trader.common.util.DecimalUtils;
import ru.obukhov.trader.test.utils.model.TestData;
import ru.obukhov.trader.test.utils.model.share.TestShares;
import ru.obukhov.trader.trading.model.StrategyType;
import ru.tinkoff.piapi.contract.v1.CandleInterval;

import java.util.LinkedHashMap;
import java.util.Map;

class BotConfigUnitTest {

    @Test
    void testToString() {
        final Map<String, Object> strategyParams = new LinkedHashMap<>();
        strategyParams.put("minimumProfit", 0.01);
        strategyParams.put("indexCoefficient", 0.5);

        final BotConfig botConfig = new BotConfig(
                TestData.ACCOUNT_ID1,
                TestShares.APPLE.share().figi(),
                CandleInterval.CANDLE_INTERVAL_1_MIN,
                DecimalUtils.setDefaultScale(0.03),
                StrategyType.CONSERVATIVE,
                strategyParams
        );

        final String actualResult = botConfig.toString();
        final String expectedResult = "BotConfig{" +
                "accountId=2000124699, " +
                "figi=BBG000B9XRY4, " +
                "candleInterval=CANDLE_INTERVAL_1_MIN, " +
                "commission=0.030000000, " +
                "strategyType=conservative, " +
                "strategyParams={minimumProfit=0.01, indexCoefficient=0.5}}";

        Assertions.assertEquals(expectedResult, actualResult);
    }

}
