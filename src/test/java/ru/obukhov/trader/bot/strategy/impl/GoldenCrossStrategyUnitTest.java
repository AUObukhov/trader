package ru.obukhov.trader.bot.strategy.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import ru.obukhov.trader.bot.model.Decision;
import ru.obukhov.trader.bot.model.DecisionAction;
import ru.obukhov.trader.bot.model.DecisionData;
import ru.obukhov.trader.common.util.TrendUtils;
import ru.obukhov.trader.config.TradingProperties;
import ru.obukhov.trader.test.utils.TestDataHelper;
import ru.tinkoff.invest.openapi.model.rest.Operation;
import ru.tinkoff.invest.openapi.model.rest.OperationStatus;

import java.util.List;

class GoldenCrossStrategyUnitTest {

    private static final TradingProperties TRADING_PROPERTIES = new TradingProperties();

    private final int smallWindow = 3;
    private final int bigWindow = 6;
    private final float indexCoefficient = 0.6f;

    private final GoldenCrossStrategy strategy =
            new GoldenCrossStrategy(TRADING_PROPERTIES, smallWindow, bigWindow, indexCoefficient);

    @BeforeAll
    static void setUp() {
        TRADING_PROPERTIES.setCommission(0.003);
    }

    @Test
    void decide_returnsWait_whenExistsOperationInProgress() {
        Operation operation1 = new Operation().status(OperationStatus.DONE);
        Operation operation2 = new Operation().status(OperationStatus.PROGRESS);
        Operation operation3 = new Operation().status(OperationStatus.DECLINE);

        DecisionData data = new DecisionData();
        data.setLastOperations(List.of(operation1, operation2, operation3));

        Decision decision = strategy.decide(data);

        Assertions.assertEquals(DecisionAction.WAIT, decision.getAction());
        Assertions.assertNull(decision.getLots());
    }

    @Test
    @SuppressWarnings("unused")
    void decide_returnsWait_whenCrossoverIsZero() {
        DecisionData data = TestDataHelper.createDecisionData(1000, 100, 1);

        try (MockedStatic<TrendUtils> trendUtilsStaticMock = mock_TrendUtils_getCrossoverIfLast(0)) {
            Decision decision = strategy.decide(data);

            Assertions.assertEquals(DecisionAction.WAIT, decision.getAction());
            Assertions.assertNull(decision.getLots());
        }
    }

    @Test
    @SuppressWarnings("unused")
    void decide_returnsBuy_whenCrossoverIsOne_andThereAreAvailableLots() {
        DecisionData data = TestDataHelper.createDecisionData(1000, 100, 1);

        try (MockedStatic<TrendUtils> trendUtilsStaticMock = mock_TrendUtils_getCrossoverIfLast(1)) {
            Decision decision = strategy.decide(data);

            Assertions.assertEquals(DecisionAction.BUY, decision.getAction());
            Assertions.assertEquals(9, decision.getLots());
        }
    }

    @Test
    @SuppressWarnings("unused")
    void decide_returnsWait_whenCrossoverIsOne_andThereAreNoAvailableLots() {
        DecisionData data = TestDataHelper.createDecisionData(1000, 1000, 1);

        try (MockedStatic<TrendUtils> trendUtilsStaticMock = mock_TrendUtils_getCrossoverIfLast(1)) {
            Decision decision = strategy.decide(data);

            Assertions.assertEquals(DecisionAction.WAIT, decision.getAction());
            Assertions.assertNull(decision.getLots());
        }
    }

    @Test
    @SuppressWarnings("unused")
    void decide_returnsSell_whenCrossoverIsMinusOne_andSellProfitIsGreaterThanMinimum() {
        DecisionData data = TestDataHelper.createDecisionData(1000, 200, 1);
        data.setPosition(TestDataHelper.createPortfolioPosition(100, 10));

        try (MockedStatic<TrendUtils> trendUtilsStaticMock = mock_TrendUtils_getCrossoverIfLast(-1)) {
            Decision decision = strategy.decide(data);

            Assertions.assertEquals(DecisionAction.SELL, decision.getAction());
            Assertions.assertEquals(10, decision.getLots());
        }
    }

    @Test
    @SuppressWarnings("unused")
    void decide_returnsBuy_whenCrossoverIsMinusOne_andSellProfitIsLowerThanMinimum_andThereAreAvailableLots() {
        DecisionData data = TestDataHelper.createDecisionData(1000, 200, 1);
        data.setPosition(TestDataHelper.createPortfolioPosition(199, 10));

        try (MockedStatic<TrendUtils> trendUtilsStaticMock = mock_TrendUtils_getCrossoverIfLast(-1)) {
            Decision decision = strategy.decide(data);

            Assertions.assertEquals(DecisionAction.BUY, decision.getAction());
            Assertions.assertEquals(4, decision.getLots());
        }
    }

    @Test
    @SuppressWarnings("unused")
    void decide_returnsWait_whenCrossoverIsMinusOne_andSellProfitIsLowerThanMinimum_andThereAreNoAvailableLots() {
        DecisionData data = TestDataHelper.createDecisionData(200, 200, 1);
        data.setPosition(TestDataHelper.createPortfolioPosition(199, 10));

        try (MockedStatic<TrendUtils> trendUtilsStaticMock = mock_TrendUtils_getCrossoverIfLast(-1)) {
            Decision decision = strategy.decide(data);

            Assertions.assertEquals(DecisionAction.WAIT, decision.getAction());
            Assertions.assertNull(decision.getLots());
        }
    }

    private static MockedStatic<TrendUtils> mock_TrendUtils_getCrossoverIfLast(int crossover) {
        MockedStatic<TrendUtils> trendUtilsStaticMock =
                Mockito.mockStatic(TrendUtils.class, Mockito.CALLS_REAL_METHODS);

        trendUtilsStaticMock.when(
                () -> TrendUtils.getCrossoverIfLast(Mockito.anyList(), Mockito.anyList(), Mockito.anyInt())
        ).thenReturn(crossover);

        return trendUtilsStaticMock;
    }

}