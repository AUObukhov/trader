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

import java.util.Collection;
import java.util.List;
import java.util.Set;

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

            Collection<Strategy> strategies = List.of(strategy1, strategy2, strategy3);

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

            verifyFakeBotCreated(fakeBotImplStaticMock, strategy1);
            verifyFakeBotCreated(fakeBotImplStaticMock, strategy2);
            verifyFakeBotCreated(fakeBotImplStaticMock, strategy3);
        }
    }

    private void verifyFakeBotCreated(MockedStatic<FakeBotImpl> fakeBotStaticMock, Strategy strategy) {
        fakeBotStaticMock.verify(
                () -> FakeBotImpl.create(
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