package ru.obukhov.trader.trading.bots.impl;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.obukhov.trader.config.properties.MarketProperties;
import ru.obukhov.trader.market.impl.FakeTinkoffService;
import ru.obukhov.trader.market.impl.RealTinkoffService;
import ru.obukhov.trader.market.interfaces.MarketService;

@Service
@AllArgsConstructor
public class FakeTinkoffServiceFactory {

    private final MarketProperties marketProperties;
    private final MarketService realMarketService;
    private final RealTinkoffService realTinkoffService;

    public FakeTinkoffService createService(final Double commission) {
        return new FakeTinkoffService(marketProperties, commission, realMarketService, realTinkoffService);
    }

}