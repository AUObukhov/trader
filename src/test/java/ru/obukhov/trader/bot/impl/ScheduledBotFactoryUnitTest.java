package ru.obukhov.trader.bot.impl;

import org.apache.commons.lang3.NotImplementedException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import ru.obukhov.trader.BaseMockedTest;
import ru.obukhov.trader.bot.interfaces.Bot;
import ru.obukhov.trader.bot.model.Decision;
import ru.obukhov.trader.bot.model.DecisionData;
import ru.obukhov.trader.bot.strategy.Strategy;
import ru.obukhov.trader.config.BotConfig;
import ru.obukhov.trader.config.TradingProperties;
import ru.obukhov.trader.market.impl.RealTinkoffService;
import ru.obukhov.trader.market.interfaces.MarketService;
import ru.obukhov.trader.market.interfaces.OperationsService;
import ru.obukhov.trader.market.interfaces.OrdersService;
import ru.obukhov.trader.market.interfaces.PortfolioService;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

class ScheduledBotFactoryUnitTest extends BaseMockedTest {

    @Mock
    private TradingProperties tradingProperties;
    @Mock
    private MarketService marketService;
    @Mock
    private RealTinkoffService tinkoffService;
    @Mock
    private OperationsService operationsService;
    @Mock
    private OrdersService ordersService;
    @Mock
    private PortfolioService portfolioService;
    @Mock
    private BotConfig botConfig;

    @Test
    void createBots() {

        try (MockedStatic<ScheduledBot> ScheduledBotStaticMock =
                     Mockito.mockStatic(ScheduledBot.class, Mockito.CALLS_REAL_METHODS)) {

            Strategy strategy1 = new TestStrategy();
            Strategy strategy2 = new TestStrategy();
            Strategy strategy3 = new TestStrategy();
            Collection<Strategy> strategies = Arrays.asList(strategy1, strategy2, strategy3);

            ScheduledBotFactory factory = new ScheduledBotFactory(
                    tradingProperties,
                    marketService,
                    tinkoffService,
                    strategies,
                    operationsService,
                    ordersService,
                    portfolioService,
                    botConfig
            );

            Set<Bot> bots = factory.createBots();

            Assertions.assertEquals(strategies.size(), bots.size());
            for (Bot bot : bots) {
                Assertions.assertEquals(ScheduledBot.class, bot.getClass());
            }

            verifyScheduledBotCreated(ScheduledBotStaticMock, strategy1);
            verifyScheduledBotCreated(ScheduledBotStaticMock, strategy2);
            verifyScheduledBotCreated(ScheduledBotStaticMock, strategy3);
        }
    }

    private void verifyScheduledBotCreated(MockedStatic<ScheduledBot> scheduledBotStaticMock, Strategy strategy) {
        scheduledBotStaticMock.verify(
                () -> ScheduledBot.create(
                        strategy,
                        marketService,
                        operationsService,
                        ordersService,
                        portfolioService,
                        botConfig,
                        tradingProperties
                )
        );
    }

    private static class TestStrategy implements Strategy {
        @Override
        public Decision decide(DecisionData data) {
            throw new NotImplementedException();
        }
    }

}