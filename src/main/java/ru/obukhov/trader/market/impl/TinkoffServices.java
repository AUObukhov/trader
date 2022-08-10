package ru.obukhov.trader.market.impl;

public record TinkoffServices(
        ExtMarketDataService extMarketDataService,
        MarketInstrumentsService marketInstrumentsService,
        MarketOperationsService operationsService,
        MarketOrdersService ordersService,
        PortfolioService portfolioService,
        RealTinkoffService realTinkoffService
) {
}