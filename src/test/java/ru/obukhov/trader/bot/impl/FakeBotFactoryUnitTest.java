package ru.obukhov.trader.bot.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import ru.obukhov.trader.BaseMockedTest;
import ru.obukhov.trader.bot.interfaces.Bot;
import ru.obukhov.trader.bot.strategy.Strategy;
import ru.obukhov.trader.bot.strategy.impl.ConservativeStrategy;
import ru.obukhov.trader.bot.strategy.impl.DumbStrategy;
import ru.obukhov.trader.bot.strategy.impl.TrendReversalStrategy;
import ru.obukhov.trader.config.TradingProperties;
import ru.obukhov.trader.market.impl.FakeTinkoffService;
import ru.obukhov.trader.market.impl.MarketServiceImpl;
import ru.obukhov.trader.market.impl.OperationsServiceImpl;
import ru.obukhov.trader.market.impl.OrdersServiceImpl;
import ru.obukhov.trader.market.impl.PortfolioServiceImpl;
import ru.obukhov.trader.market.impl.RealTinkoffService;
import ru.obukhov.trader.market.interfaces.MarketService;
import ru.obukhov.trader.test.utils.AssertUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

class FakeBotFactoryUnitTest extends BaseMockedTest {

    @Mock
    private TradingProperties tradingProperties;
    @Mock
    private MarketService marketService;
    @Mock
    private RealTinkoffService tinkoffService;

    @Test
    void createBots_createsBots() {
        try (MockedStatic<FakeBotImpl> fakeBotImplStaticMock =
                     Mockito.mockStatic(FakeBotImpl.class, Mockito.CALLS_REAL_METHODS)) {

            Strategy strategy1 = new ConservativeStrategy(tradingProperties);
            Strategy strategy2 = new DumbStrategy(tradingProperties);

            Integer lastPricesCount = 100;
            Integer extremumPriceIndex = 95;
            Strategy strategy3 = new TrendReversalStrategy(tradingProperties, lastPricesCount, extremumPriceIndex);

            Collection<Strategy> strategies = Arrays.asList(strategy1, strategy2, strategy3);

            FakeBotFactory factory = new FakeBotFactory(
                    tradingProperties,
                    marketService,
                    tinkoffService,
                    strategies
            );

            Set<Bot> bots = factory.createBots();

            Assertions.assertEquals(strategies.size(), bots.size());
            for (Bot bot : bots) {
                Assertions.assertEquals(FakeBotImpl.class, bot.getClass());
            }

            Set<String> fakeBotsNames = bots.stream()
                    .map(bot -> ((FakeBotImpl) bot).getName())
                    .collect(Collectors.toSet());
            Assertions.assertTrue(fakeBotsNames.contains("Conservative bot"));
            Assertions.assertTrue(fakeBotsNames.contains("Dumb bot"));
            Assertions.assertTrue(fakeBotsNames.contains("Trend reversal bot (95|100)"));

            verifyFakeBotCreated(fakeBotImplStaticMock, "Conservative bot", strategy1);
            verifyFakeBotCreated(fakeBotImplStaticMock, "Dumb bot", strategy2);
            verifyFakeBotCreated(fakeBotImplStaticMock, "Trend reversal bot (95|100)", strategy3);
        }
    }

    @Test
    void createBots_throwsIllegalArgumentException_whenGetsUnknownTypeOfStrategy() {
        try (MockedStatic<FakeBotImpl> fakeBotImplStaticMock =
                     Mockito.mockStatic(FakeBotImpl.class, Mockito.CALLS_REAL_METHODS)) {

            Collection<Strategy> strategies = Arrays.asList(
                    new ConservativeStrategy(tradingProperties),
                    new TestStrategy()
            );

            FakeBotFactory factory = new FakeBotFactory(
                    tradingProperties,
                    marketService,
                    tinkoffService,
                    strategies
            );

            AssertUtils.assertThrowsWithMessage(
                    factory::createBots,
                    IllegalArgumentException.class,
                    "Unknown strategy class: " + TestStrategy.class
            );
        }
    }

    private void verifyFakeBotCreated(MockedStatic<FakeBotImpl> fakeBotStaticMock, String name, Strategy strategy) {
        fakeBotStaticMock.verify(
                () -> FakeBotImpl.create(
                        Mockito.eq(name),
                        Mockito.eq(strategy),
                        Mockito.any(MarketServiceImpl.class),
                        Mockito.any(OperationsServiceImpl.class),
                        Mockito.any(OrdersServiceImpl.class),
                        Mockito.any(PortfolioServiceImpl.class),
                        Mockito.any(FakeTinkoffService.class)
                )
        );
    }

}