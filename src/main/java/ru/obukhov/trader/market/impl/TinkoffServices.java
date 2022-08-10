package ru.obukhov.trader.market.impl;

public record TinkoffServices(
        ExtMarketDataService extMarketDataService,
        ExtInstrumentsService extInstrumentsService,
        MarketOperationsService operationsService,
        MarketOrdersService ordersService,
        PortfolioService portfolioService,
        RealTinkoffService realTinkoffService
) {
}