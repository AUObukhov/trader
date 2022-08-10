package ru.obukhov.trader.market.impl;

public record TinkoffServices(
        MarketService marketService,
        MarketInstrumentsService marketInstrumentsService,
        MarketOperationsService operationsService,
        MarketOrdersService ordersService,
        PortfolioService portfolioService,
        RealTinkoffService realTinkoffService
) {
}