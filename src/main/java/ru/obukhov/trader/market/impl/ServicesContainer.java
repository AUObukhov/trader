package ru.obukhov.trader.market.impl;

import org.springframework.stereotype.Component;
import ru.obukhov.trader.market.interfaces.ExtInstrumentsService;
import ru.obukhov.trader.market.interfaces.ExtOperationsService;
import ru.obukhov.trader.market.interfaces.ExtOrdersService;

@Component
public record ServicesContainer(
        ExtMarketDataService extMarketDataService,
        ExtInstrumentsService extInstrumentsService,
        ExtOperationsService extOperationsService,
        ExtOrdersService extOrdersService,
        ExtUsersService extUsersService
) {
}