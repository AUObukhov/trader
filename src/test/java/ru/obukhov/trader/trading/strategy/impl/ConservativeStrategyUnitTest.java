package ru.obukhov.trader.trading.strategy.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.common.util.DecimalUtils;
import ru.obukhov.trader.market.impl.ExtMarketDataService;
import ru.obukhov.trader.market.model.Candle;
import ru.obukhov.trader.market.model.Share;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.test.utils.model.DateTimeTestData;
import ru.obukhov.trader.test.utils.model.TestData;
import ru.obukhov.trader.test.utils.model.account.TestAccounts;
import ru.obukhov.trader.test.utils.model.share.TestShares;
import ru.obukhov.trader.trading.model.Decision;
import ru.obukhov.trader.trading.model.DecisionAction;
import ru.obukhov.trader.trading.model.DecisionData;
import ru.obukhov.trader.trading.model.DecisionsData;
import ru.obukhov.trader.trading.model.StrategyType;
import ru.obukhov.trader.web.model.BotConfig;
import ru.tinkoff.piapi.contract.v1.CandleInterval;
import ru.tinkoff.piapi.contract.v1.Operation;
import ru.tinkoff.piapi.contract.v1.OperationState;

import java.time.OffsetDateTime;
import java.util.Collections;
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
    void decide_throwsIllegalArgumentException_whenNoDecisionData() {
        final String accountId = TestAccounts.TINKOFF.getId();
        final Share share = TestShares.SBER.share();
        final String figi = share.figi();

        final DecisionsData decisionsData = new DecisionsData();
        decisionsData.setDecisionDataList(Collections.emptyList());

        final CandleInterval candleInterval = CandleInterval.CANDLE_INTERVAL_1_MIN;
        final BotConfig botConfig = new BotConfig(accountId, List.of(figi), candleInterval, DecimalUtils.ZERO, StrategyType.CONSERVATIVE, Map.of());
        final OffsetDateTime from = DateTimeTestData.newDateTime(2023, 9, 10);
        final OffsetDateTime to = DateTimeTestData.newDateTime(2023, 9, 11);
        final Interval interval = Interval.of(from, to);

        final Candle candle = new Candle().setClose(DecimalUtils.setDefaultScale(100));
        final List<Candle> candles = List.of(candle);
        Mockito.when(extMarketDataService.getCandles(figi, interval, candleInterval)).thenReturn(candles);

        final Executable executable = () -> strategy.decide(decisionsData, strategy.initCache(botConfig, interval));
        final String expectedMessage = "Conservative strategy supports 1 instrument only";
        AssertUtils.assertThrowsWithMessage(IllegalArgumentException.class, executable, expectedMessage);
    }

    @Test
    void decide_throwsIllegalArgumentException_whenMultipleDecisionData() {
        final String accountId = TestAccounts.TINKOFF.getId();
        final Share share = TestShares.SBER.share();
        final String figi = share.figi();

        final DecisionsData decisionsData = new DecisionsData();
        decisionsData.setDecisionDataList(List.of(new DecisionData(), new DecisionData()));

        final CandleInterval candleInterval = CandleInterval.CANDLE_INTERVAL_1_MIN;
        final BotConfig botConfig = new BotConfig(accountId, List.of(figi), candleInterval, DecimalUtils.ZERO, StrategyType.CONSERVATIVE, Map.of());
        final OffsetDateTime from = DateTimeTestData.newDateTime(2023, 9, 10);
        final OffsetDateTime to = DateTimeTestData.newDateTime(2023, 9, 11);
        final Interval interval = Interval.of(from, to);

        final Candle candle = new Candle().setClose(DecimalUtils.setDefaultScale(100));
        final List<Candle> candles = List.of(candle);
        Mockito.when(extMarketDataService.getCandles(figi, interval, candleInterval)).thenReturn(candles);

        final Executable executable = () -> strategy.decide(decisionsData, strategy.initCache(botConfig, interval));
        final String expectedMessage = "Conservative strategy supports 1 instrument only";
        AssertUtils.assertThrowsWithMessage(IllegalArgumentException.class, executable, expectedMessage);
    }

    @Test
    void decide_returnsWait_whenExistsOperationStateInUnspecified() {
        final String accountId = TestAccounts.TINKOFF.getId();
        final Share share = TestShares.SBER.share();
        final String figi = share.figi();

        final Operation operation1 = TestData.newOperation(OperationState.OPERATION_STATE_EXECUTED);
        final Operation operation2 = TestData.newOperation(OperationState.OPERATION_STATE_UNSPECIFIED);
        final Operation operation3 = TestData.newOperation(OperationState.OPERATION_STATE_CANCELED);

        final DecisionData decisionData = new DecisionData();
        decisionData.setShare(share);
        decisionData.setLastOperations(List.of(operation1, operation2, operation3));

        final DecisionsData decisionsData = new DecisionsData();
        decisionsData.setDecisionDataList(List.of(decisionData));

        final CandleInterval candleInterval = CandleInterval.CANDLE_INTERVAL_1_MIN;
        final BotConfig botConfig = new BotConfig(accountId, List.of(figi), candleInterval, DecimalUtils.ZERO, StrategyType.CONSERVATIVE, Map.of());
        final OffsetDateTime from = DateTimeTestData.newDateTime(2023, 9, 10);
        final OffsetDateTime to = DateTimeTestData.newDateTime(2023, 9, 11);
        final Interval interval = Interval.of(from, to);

        final Candle candle = new Candle().setClose(DecimalUtils.setDefaultScale(100));
        final List<Candle> candles = List.of(candle);
        Mockito.when(extMarketDataService.getCandles(figi, interval, candleInterval)).thenReturn(candles);

        final Map<String, Decision> decisions = strategy.decide(decisionsData, strategy.initCache(botConfig, interval));

        Assertions.assertEquals(1, decisions.size());
        Assertions.assertEquals(DecisionAction.WAIT, decisions.get(share.figi()).getAction());
        Assertions.assertNull(decisions.get(share.figi()).getQuantity());
    }

    @Test
    void decide_returnsWait_whenNoAvailableLots() {
        final String accountId = TestAccounts.TINKOFF.getId();
        final Share share = TestShares.SBER.share();
        final String figi = share.figi();

        final DecisionData decisionData = TestData.newDecisionData(share, 0L);

        final DecisionsData decisionsData = new DecisionsData();
        decisionsData.setDecisionDataList(List.of(decisionData));

        final CandleInterval candleInterval = CandleInterval.CANDLE_INTERVAL_1_MIN;
        final BotConfig botConfig = new BotConfig(accountId, List.of(figi), candleInterval, DecimalUtils.ZERO, StrategyType.CONSERVATIVE, Map.of());
        final OffsetDateTime from = DateTimeTestData.newDateTime(2023, 9, 10);
        final OffsetDateTime to = DateTimeTestData.newDateTime(2023, 9, 11);
        final Interval interval = Interval.of(from, to);

        final Candle candle = new Candle().setClose(DecimalUtils.setDefaultScale(100));
        final List<Candle> candles = List.of(candle);
        Mockito.when(extMarketDataService.getCandles(figi, interval, candleInterval)).thenReturn(candles);

        final Map<String, Decision> decisions = strategy.decide(decisionsData, strategy.initCache(botConfig, interval));

        Assertions.assertEquals(1, decisions.size());
        Assertions.assertEquals(DecisionAction.WAIT, decisions.get(share.figi()).getAction());
        Assertions.assertNull(decisions.get(share.figi()).getQuantity());
    }

    @Test
    void decide_returnsBuy_whenThereAreAvailableLots() {
        final String accountId = TestAccounts.TINKOFF.getId();
        final Share share = TestShares.SBER.share();
        final String figi = share.figi();

        final long availableLots = 4;
        final DecisionData decisionData = TestData.newDecisionData(share, availableLots);

        final DecisionsData decisionsData = new DecisionsData();
        decisionsData.setDecisionDataList(List.of(decisionData));

        final CandleInterval candleInterval = CandleInterval.CANDLE_INTERVAL_1_MIN;
        final BotConfig botConfig = new BotConfig(accountId, List.of(figi), candleInterval, DecimalUtils.ZERO, StrategyType.CONSERVATIVE, Map.of());
        final OffsetDateTime from = DateTimeTestData.newDateTime(2023, 9, 10);
        final OffsetDateTime to = DateTimeTestData.newDateTime(2023, 9, 11);
        final Interval interval = Interval.of(from, to);

        final Candle candle = new Candle().setClose(DecimalUtils.setDefaultScale(100));
        final List<Candle> candles = List.of(candle);
        Mockito.when(extMarketDataService.getCandles(figi, interval, candleInterval)).thenReturn(candles);

        final Map<String, Decision> decisions = strategy.decide(decisionsData, strategy.initCache(botConfig, interval));

        Assertions.assertEquals(1, decisions.size());
        Assertions.assertEquals(DecisionAction.BUY, decisions.get(share.figi()).getAction());
        Assertions.assertEquals(availableLots, decisions.get(share.figi()).getQuantity());
    }

    // endregion

    @Test
    void initCache_returnsNotNull() {
        final String accountId = TestAccounts.TINKOFF.getId();
        final String figi = TestShares.SBER.share().figi();
        final CandleInterval candleInterval = CandleInterval.CANDLE_INTERVAL_1_MIN;
        final BotConfig botConfig = new BotConfig(accountId, List.of(figi), candleInterval, DecimalUtils.ZERO, StrategyType.CONSERVATIVE, Map.of());
        final OffsetDateTime from = DateTimeTestData.newDateTime(2023, 9, 10);
        final OffsetDateTime to = DateTimeTestData.newDateTime(2023, 9, 11);
        final Interval interval = Interval.of(from, to);

        final Candle candle = new Candle().setClose(DecimalUtils.setDefaultScale(100));
        final List<Candle> candles = List.of(candle);
        Mockito.when(extMarketDataService.getCandles(figi, interval, candleInterval)).thenReturn(candles);

        Assertions.assertNotNull(strategy.initCache(botConfig, interval));
    }

}