package ru.obukhov.trader.trading.strategy.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.common.util.DecimalUtils;
import ru.obukhov.trader.market.impl.ExtMarketDataService;
import ru.obukhov.trader.market.model.Candle;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.test.utils.model.DateTimeTestData;
import ru.obukhov.trader.test.utils.model.TestData;
import ru.obukhov.trader.test.utils.model.account.TestAccounts;
import ru.obukhov.trader.test.utils.model.share.TestShares;
import ru.obukhov.trader.trading.model.Decision;
import ru.obukhov.trader.trading.model.DecisionAction;
import ru.obukhov.trader.trading.model.DecisionData;
import ru.obukhov.trader.trading.model.StrategyType;
import ru.obukhov.trader.web.model.BotConfig;
import ru.tinkoff.piapi.contract.v1.CandleInterval;
import ru.tinkoff.piapi.contract.v1.Operation;
import ru.tinkoff.piapi.contract.v1.OperationState;

import java.util.List;
import java.util.Map;

@ExtendWith(MockitoExtension.class)
class ConservativeStrategyUnitTest {

    @Mock
    private ExtMarketDataService extMarketDataService;

    private ConservativeStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new ConservativeStrategy(StrategyType.CONSERVATIVE.name(), extMarketDataService);
    }

    @Test
    void getName_returnsProperName() {
        Assertions.assertEquals("CONSERVATIVE", strategy.getName());
    }

    // region decide tests

    @Test
    void decide_returnsWait_whenExistsOperationStateInUnspecified() {
        final Operation operation1 = TestData.newOperation(OperationState.OPERATION_STATE_EXECUTED);
        final Operation operation2 = TestData.newOperation(OperationState.OPERATION_STATE_UNSPECIFIED);
        final Operation operation3 = TestData.newOperation(OperationState.OPERATION_STATE_CANCELED);

        final DecisionData data = new DecisionData();
        data.setLastOperations(List.of(operation1, operation2, operation3));

        final String figi = TestShares.SBER.share().figi();
        final CandleInterval candleInterval = CandleInterval.CANDLE_INTERVAL_1_MIN;
        final BotConfig botConfig = new BotConfig(
                TestAccounts.TINKOFF.account().id(),
                figi,
                candleInterval,
                DecimalUtils.ZERO,
                StrategyType.CONSERVATIVE,
                Map.of()
        );
        final Interval interval = Interval.of(
                DateTimeTestData.newDateTime(2023, 9, 10),
                DateTimeTestData.newDateTime(2023, 9, 11)
        );

        final Candle candle = new Candle().setClose(DecimalUtils.setDefaultScale(100));
        final List<Candle> candles = List.of(candle);
        Mockito.when(extMarketDataService.getCandles(figi, interval, candleInterval)).thenReturn(candles);

        final Decision decision = strategy.decide(data, 1, strategy.initCache(botConfig, interval));

        Assertions.assertEquals(DecisionAction.WAIT, decision.getAction());
        Assertions.assertNull(decision.getQuantity());
    }

    @Test
    void decide_returnsWait_whenNoAvailableLots() {
        final DecisionData data = TestData.newDecisionData(2000.0, 1, 0.003);
        final String figi = TestShares.SBER.share().figi();
        final CandleInterval candleInterval = CandleInterval.CANDLE_INTERVAL_1_MIN;
        final BotConfig botConfig = new BotConfig(
                TestAccounts.TINKOFF.account().id(),
                figi,
                candleInterval,
                DecimalUtils.ZERO,
                StrategyType.CONSERVATIVE,
                Map.of()
        );
        final Interval interval = Interval.of(
                DateTimeTestData.newDateTime(2023, 9, 10),
                DateTimeTestData.newDateTime(2023, 9, 11)
        );

        final Candle candle = new Candle().setClose(DecimalUtils.setDefaultScale(100));
        final List<Candle> candles = List.of(candle);
        Mockito.when(extMarketDataService.getCandles(figi, interval, candleInterval)).thenReturn(candles);

        final Decision decision = strategy.decide(data, 0, strategy.initCache(botConfig, interval));

        Assertions.assertEquals(DecisionAction.WAIT, decision.getAction());
        Assertions.assertNull(decision.getQuantity());
    }

    @Test
    void decide_returnsBuy_whenThereAreAvailableLots() {
        final DecisionData data = TestData.newDecisionData(10000.0, 1, 0.003);
        final String figi = TestShares.SBER.share().figi();
        final CandleInterval candleInterval = CandleInterval.CANDLE_INTERVAL_1_MIN;
        final BotConfig botConfig = new BotConfig(
                TestAccounts.TINKOFF.account().id(),
                figi,
                candleInterval,
                DecimalUtils.ZERO,
                StrategyType.CONSERVATIVE,
                Map.of()
        );
        final Interval interval = Interval.of(
                DateTimeTestData.newDateTime(2023, 9, 10),
                DateTimeTestData.newDateTime(2023, 9, 11)
        );
        final int availableLots = 4;

        final Candle candle = new Candle().setClose(DecimalUtils.setDefaultScale(100));
        final List<Candle> candles = List.of(candle);
        Mockito.when(extMarketDataService.getCandles(figi, interval, candleInterval)).thenReturn(candles);

        final Decision decision = strategy.decide(data, availableLots, strategy.initCache(botConfig, interval));

        Assertions.assertEquals(DecisionAction.BUY, decision.getAction());
        AssertUtils.assertEquals(availableLots, decision.getQuantity());
    }

    // endregion

    @Test
    void initCache_returnsNotNull() {
        final String figi = TestShares.SBER.share().figi();
        final CandleInterval candleInterval = CandleInterval.CANDLE_INTERVAL_1_MIN;
        final BotConfig botConfig = new BotConfig(
                TestAccounts.TINKOFF.account().id(),
                figi,
                candleInterval,
                DecimalUtils.ZERO,
                StrategyType.CONSERVATIVE,
                Map.of()
        );
        final Interval interval = Interval.of(
                DateTimeTestData.newDateTime(2023, 9, 10),
                DateTimeTestData.newDateTime(2023, 9, 11)
        );

        final Candle candle = new Candle().setClose(DecimalUtils.setDefaultScale(100));
        final List<Candle> candles = List.of(candle);
        Mockito.when(extMarketDataService.getCandles(figi, interval, candleInterval)).thenReturn(candles);

        Assertions.assertNotNull(strategy.initCache(botConfig, interval));
    }

}