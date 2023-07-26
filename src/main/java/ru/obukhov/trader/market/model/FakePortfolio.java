package ru.obukhov.trader.market.model;

import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.obukhov.trader.trading.model.BackTestOperation;

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
    private final String accountId;

    @NotNull
    private final Map<String, FakeBalance> balances = new HashMap<>();

    @NotNull
    private final Map<String, PortfolioPosition> figiesToPositions = new HashMap<>();

    @NotNull
    private final Set<BackTestOperation> operations = new HashSet<>();

}