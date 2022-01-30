package ru.obukhov.trader.market.impl;

import org.springframework.stereotype.Service;

@Service
public record TinkoffServices(MarketService marketService,
                              OperationsService operationsService,
                              OrdersService ordersService,
                              PortfolioService portfolioService,
                              RealTinkoffService realTinkoffService) {
}