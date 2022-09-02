package ru.obukhov.trader.market.impl;

import ru.obukhov.trader.market.interfaces.ExtOperationsService;
import ru.obukhov.trader.market.interfaces.ExtOrdersService;

public record TinkoffServices(
        ExtMarketDataService extMarketDataService,
        ExtInstrumentsService extInstrumentsService,
        ExtOperationsService operationsService,
        ExtOrdersService ordersService,
        RealContext realContext
) {
}