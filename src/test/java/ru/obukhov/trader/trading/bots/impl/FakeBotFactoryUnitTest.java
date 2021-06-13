package ru.obukhov.trader.trading.bots.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.obukhov.trader.config.TradingProperties;
import ru.obukhov.trader.market.impl.RealTinkoffService;
import ru.obukhov.trader.market.interfaces.MarketService;
import ru.obukhov.trader.trading.bots.interfaces.Bot;
import ru.obukhov.trader.trading.bots.interfaces.FakeBot;
import ru.obukhov.trader.trading.strategy.impl.ConservativeStrategy;
import ru.obukhov.trader.trading.strategy.interfaces.TradingStrategy;

@ExtendWith(MockitoExtension.class)
class FakeBotFactoryUnitTest {

    @Mock
    private TradingProperties tradingProperties;
    @Mock
    private MarketService marketService;
    @Mock
    private RealTinkoffService tinkoffService;

    @InjectMocks
    private FakeBotFactory factory;

    @Test
    void createBot_createsFakeBot() {
        final TradingStrategy strategy = new ConservativeStrategy(0.1f, tradingProperties);

        final Bot bot = factory.createBot(strategy);

        Assertions.assertTrue(bot instanceof FakeBot);
    }

}