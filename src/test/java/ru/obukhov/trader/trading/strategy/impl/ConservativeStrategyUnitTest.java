package ru.obukhov.trader.trading.strategy.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.common.util.DecimalUtils;
import ru.obukhov.trader.market.model.Share;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.test.utils.model.DateTimeTestData;
import ru.obukhov.trader.test.utils.model.TestData;
import ru.obukhov.trader.test.utils.model.account.TestAccounts;
import ru.obukhov.trader.test.utils.model.share.TestShare;
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

    private ConservativeStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new ConservativeStrategy(StrategyType.CONSERVATIVE.name());
    }

    @Test
    void getName_returnsProperName() {
        Assertions.assertEquals("CONSERVATIVE", strategy.getName());
    }

    // region decide tests

    @Test
    void decide_throwsIllegalArgumentException_whenNoDecisionData() {
        final String accountId = TestAccounts.TINKOFF.getId();
        final String figi = TestShares.SBER.getFigi();

        final DecisionsData decisionsData = new DecisionsData();
        decisionsData.setDecisionDatas(Collections.emptyList());

        final CandleInterval candleInterval = CandleInterval.CANDLE_INTERVAL_1_MIN;
        final BotConfig botConfig = new BotConfig(accountId, List.of(figi), candleInterval, DecimalUtils.ZERO, StrategyType.CONSERVATIVE, Map.of());
        final OffsetDateTime from = DateTimeTestData.newDateTime(2023, 9, 10);
        final OffsetDateTime to = DateTimeTestData.newDateTime(2023, 9, 11);
        final Interval interval = Interval.of(from, to);

        final Executable executable = () -> strategy.decide(decisionsData, botConfig, interval);
        final String expectedMessage = "Conservative strategy supports 1 instrument only";
        AssertUtils.assertThrowsWithMessage(IllegalArgumentException.class, executable, expectedMessage);
    }

    @Test
    void decide_throwsIllegalArgumentException_whenMultipleDecisionData() {
        final String accountId = TestAccounts.TINKOFF.getId();
        final String figi = TestShares.SBER.getFigi();

        final DecisionsData decisionsData = new DecisionsData();
        decisionsData.setDecisionDatas(List.of(new DecisionData(), new DecisionData()));

        final CandleInterval candleInterval = CandleInterval.CANDLE_INTERVAL_1_MIN;
        final BotConfig botConfig = new BotConfig(accountId, List.of(figi), candleInterval, DecimalUtils.ZERO, StrategyType.CONSERVATIVE, Map.of());
        final OffsetDateTime from = DateTimeTestData.newDateTime(2023, 9, 10);
        final OffsetDateTime to = DateTimeTestData.newDateTime(2023, 9, 11);
        final Interval interval = Interval.of(from, to);

        final Executable executable = () -> strategy.decide(decisionsData, botConfig, interval);
        final String expectedMessage = "Conservative strategy supports 1 instrument only";
        AssertUtils.assertThrowsWithMessage(IllegalArgumentException.class, executable, expectedMessage);
    }

    @Test
    void decide_returnsWait_whenExistsOperationStateInUnspecified() {
        final String accountId = TestAccounts.TINKOFF.getId();
        final TestShare share = TestShares.SBER;
        final String figi = share.getFigi();

        final Operation operation1 = TestData.newOperation(OperationState.OPERATION_STATE_EXECUTED);
        final Operation operation2 = TestData.newOperation(OperationState.OPERATION_STATE_UNSPECIFIED);
        final Operation operation3 = TestData.newOperation(OperationState.OPERATION_STATE_CANCELED);

        final DecisionData decisionData = new DecisionData();
        decisionData.setShare(share.share());
        decisionData.setLastOperations(List.of(operation1, operation2, operation3));

        final DecisionsData decisionsData = new DecisionsData();
        decisionsData.setDecisionDatas(List.of(decisionData));

        final CandleInterval candleInterval = CandleInterval.CANDLE_INTERVAL_1_MIN;
        final BotConfig botConfig = new BotConfig(accountId, List.of(figi), candleInterval, DecimalUtils.ZERO, StrategyType.CONSERVATIVE, Map.of());
        final OffsetDateTime from = DateTimeTestData.newDateTime(2023, 9, 10);
        final OffsetDateTime to = DateTimeTestData.newDateTime(2023, 9, 11);
        final Interval interval = Interval.of(from, to);

        final Map<String, Decision> decisions = strategy.decide(decisionsData, botConfig, interval);

        Assertions.assertEquals(1, decisions.size());
        Assertions.assertEquals(DecisionAction.WAIT, decisions.get(share.getFigi()).getAction());
        Assertions.assertNull(decisions.get(share.getFigi()).getQuantity());
    }

    @Test
    void decide_returnsWait_whenNoAvailableLots() {
        final String accountId = TestAccounts.TINKOFF.getId();
        final Share share = TestShares.SBER.share();
        final String figi = share.figi();

        final DecisionData decisionData = TestData.newDecisionData1(share, 0L);

        final DecisionsData decisionsData = new DecisionsData();
        decisionsData.setDecisionDatas(List.of(decisionData));

        final CandleInterval candleInterval = CandleInterval.CANDLE_INTERVAL_1_MIN;
        final BotConfig botConfig = new BotConfig(accountId, List.of(figi), candleInterval, DecimalUtils.ZERO, StrategyType.CONSERVATIVE, Map.of());
        final OffsetDateTime from = DateTimeTestData.newDateTime(2023, 9, 10);
        final OffsetDateTime to = DateTimeTestData.newDateTime(2023, 9, 11);
        final Interval interval = Interval.of(from, to);

        final Map<String, Decision> decisions = strategy.decide(decisionsData, botConfig, interval);

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
        final DecisionData decisionData = TestData.newDecisionData1(share, availableLots);

        final DecisionsData decisionsData = new DecisionsData();
        decisionsData.setDecisionDatas(List.of(decisionData));

        final CandleInterval candleInterval = CandleInterval.CANDLE_INTERVAL_1_MIN;
        final BotConfig botConfig = new BotConfig(accountId, List.of(figi), candleInterval, DecimalUtils.ZERO, StrategyType.CONSERVATIVE, Map.of());
        final OffsetDateTime from = DateTimeTestData.newDateTime(2023, 9, 10);
        final OffsetDateTime to = DateTimeTestData.newDateTime(2023, 9, 11);
        final Interval interval = Interval.of(from, to);

        final Map<String, Decision> decisions = strategy.decide(decisionsData, botConfig, interval);

        Assertions.assertEquals(1, decisions.size());
        Assertions.assertEquals(DecisionAction.BUY, decisions.get(share.figi()).getAction());
        Assertions.assertEquals(availableLots, decisions.get(share.figi()).getQuantity());
    }

    // endregion

}