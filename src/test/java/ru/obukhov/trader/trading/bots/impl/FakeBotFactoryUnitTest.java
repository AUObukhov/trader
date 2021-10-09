package ru.obukhov.trader.trading.bots.impl;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.obukhov.trader.market.impl.RealTinkoffService;
import ru.obukhov.trader.market.interfaces.MarketService;
import ru.obukhov.trader.trading.bots.interfaces.Bot;
import ru.obukhov.trader.trading.bots.interfaces.FakeBot;
import ru.obukhov.trader.trading.strategy.impl.AbstractTradingStrategy;
import ru.obukhov.trader.trading.strategy.impl.ConservativeStrategy;
import ru.tinkoff.invest.openapi.model.rest.CandleResolution;

@ExtendWith(MockitoExtension.class)
class FakeBotFactoryUnitTest {

    @Mock
    private MarketService marketService;
    @Mock
    private RealTinkoffService tinkoffService;

    @InjectMocks
    private FakeBotFactory factory;

    @Test
    void createBot_createsFakeBot() {
        final AbstractTradingStrategy strategy = new ConservativeStrategy(StringUtils.EMPTY, 0.0);

        final Bot bot = factory.createBot(strategy, CandleResolution._1MIN);

        Assertions.assertInstanceOf(FakeBot.class, bot);
    }

}