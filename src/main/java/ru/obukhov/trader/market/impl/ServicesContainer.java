package ru.obukhov.trader.market.impl;

import ru.obukhov.trader.market.interfaces.ExtInstrumentsService;
import ru.obukhov.trader.market.interfaces.ExtOperationsService;
import ru.obukhov.trader.market.interfaces.ExtOrdersService;

public record ServicesContainer(
        ExtMarketDataService extMarketDataService,
        ExtInstrumentsService extInstrumentsService,
        ExtOperationsService extOperationsService,
        ExtOrdersService extOrdersService
) {
}