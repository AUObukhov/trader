package ru.obukhov.trader.market.model;

import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.tinkoff.piapi.contract.v1.Operation;
import ru.tinkoff.piapi.core.models.Position;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Data
public class FakePortfolio {

    @Nullable
    private final String accountId;

    @NotNull
    private final Map<String, FakeBalance> balances = new HashMap<>();

    @NotNull
    private final Map<String, Position> figiesToPositions = new HashMap<>();

    @NotNull
    private final Set<Operation> operations = new HashSet<>();

}