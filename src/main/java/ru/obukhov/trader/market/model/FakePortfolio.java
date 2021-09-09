package ru.obukhov.trader.market.model;

import lombok.Data;
import org.jetbrains.annotations.NotNull;
import ru.obukhov.trader.web.model.SimulatedOperation;
import ru.tinkoff.invest.openapi.model.rest.Currency;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * POJO keeping portfolio data for simulation purposes
 */
@Data
public class FakePortfolio {

    @NotNull
    private final EnumMap<Currency, FakeBalance> balances = new EnumMap<>(Currency.class);

    @NotNull
    private final Map<String, PortfolioPosition> tickersToPositions = new HashMap<>();

    @NotNull
    private final Set<SimulatedOperation> operations = new HashSet<>();

}