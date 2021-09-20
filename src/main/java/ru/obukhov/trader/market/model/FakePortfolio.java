package ru.obukhov.trader.market.model;

import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.obukhov.trader.web.model.BackTestOperation;
import ru.tinkoff.invest.openapi.model.rest.Currency;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * POJO keeping portfolio data for back testing purposes
 */
@Data
public class FakePortfolio {

    @Nullable
    private final String brokerAccountId;

    @NotNull
    private final EnumMap<Currency, FakeBalance> balances = new EnumMap<>(Currency.class);

    @NotNull
    private final Map<String, PortfolioPosition> tickersToPositions = new HashMap<>();

    @NotNull
    private final Set<BackTestOperation> operations = new HashSet<>();

}