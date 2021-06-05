package ru.obukhov.trader.bot.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.obukhov.trader.bot.interfaces.Bot;
import ru.obukhov.trader.bot.interfaces.FakeBot;
import ru.obukhov.trader.bot.strategy.TradingStrategy;
import ru.obukhov.trader.bot.strategy.impl.ConservativeStrategy;
import ru.obukhov.trader.config.TradingProperties;
import ru.obukhov.trader.market.impl.RealTinkoffService;
import ru.obukhov.trader.market.interfaces.MarketService;

@ExtendWith(MockitoExtension.class)
class FakeBotFactoryUnitTest {

    @Mock
    private TradingProperties tradingProperties;
    @Mock
    private MarketService marketService;
    @Mock
    private RealTinkoffService tinkoffService;

    @Test
    void createBot_createsFakeBot() {
        TradingStrategy strategy = new ConservativeStrategy(tradingProperties);

        FakeBotFactory factory = new FakeBotFactory(tradingProperties, marketService, tinkoffService);

        Bot bot = factory.createBot(strategy);

        Assertions.assertTrue(bot instanceof FakeBot);
    }

}