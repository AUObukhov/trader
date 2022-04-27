package ru.obukhov.trader.market.impl;

import org.springframework.stereotype.Service;

@Service
public record TinkoffServices(MarketService marketService,
                              MarketOperationsService operationsService,
                              MarketOrdersService ordersService,
                              PortfolioService portfolioService,
                              RealTinkoffService realTinkoffService) {
}