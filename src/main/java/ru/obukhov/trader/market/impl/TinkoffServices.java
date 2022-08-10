package ru.obukhov.trader.market.impl;

public record TinkoffServices(
        ExtMarketDataService extMarketDataService,
        ExtInstrumentsService extInstrumentsService,
        ExtOperationsService operationsService,
        ExtOrdersService ordersService,
        PortfolioService portfolioService,
        RealTinkoffService realTinkoffService
) {
}