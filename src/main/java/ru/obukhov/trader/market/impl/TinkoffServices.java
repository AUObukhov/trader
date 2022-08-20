package ru.obukhov.trader.market.impl;

import ru.obukhov.trader.market.interfaces.ExtOperationsService;

public record TinkoffServices(
        ExtMarketDataService extMarketDataService,
        ExtInstrumentsService extInstrumentsService,
        ExtOperationsService operationsService,
        ExtOrdersService ordersService,
        RealTinkoffService realTinkoffService
) {
}