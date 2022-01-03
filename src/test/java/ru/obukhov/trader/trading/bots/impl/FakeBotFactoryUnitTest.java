package ru.obukhov.trader.trading.bots.impl;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.obukhov.trader.config.properties.MarketProperties;
import ru.obukhov.trader.market.impl.FakeTinkoffService;
import ru.obukhov.trader.market.impl.RealTinkoffService;
import ru.obukhov.trader.market.interfaces.MarketService;
import ru.obukhov.trader.trading.strategy.impl.ConservativeStrategy;
import ru.obukhov.trader.trading.strategy.impl.TradingStrategyFactory;
import ru.obukhov.trader.web.model.BotConfig;

@ExtendWith(MockitoExtension.class)
class FakeBotFactoryUnitTest {

    private static final ConservativeStrategy CONSERVATIVE_STRATEGY = new ConservativeStrategy(StringUtils.EMPTY);

    @Mock
    private TradingStrategyFactory strategyFactory;
    @Mock
    private MarketProperties marketProperties;
    @Mock
    private MarketService marketService;
    @Mock
    private RealTinkoffService tinkoffService;

    @InjectMocks
    private FakeBotFactory factory;

    @Test
    void createBot_returnsNotNull() {
        final BotConfig botConfig = new BotConfig();
        final FakeTinkoffService fakeTinkoffService = new FakeTinkoffService(marketProperties, 0.0, marketService, tinkoffService);
        Mockito.when(strategyFactory.createStrategy(botConfig)).thenReturn(CONSERVATIVE_STRATEGY);

        final FakeBot bot = factory.createBot(botConfig, fakeTinkoffService);

        Assertions.assertNotNull(bot);
    }

}