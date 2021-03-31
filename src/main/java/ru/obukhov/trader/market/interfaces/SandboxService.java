package ru.obukhov.trader.market.interfaces;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.tinkoff.invest.openapi.model.rest.SandboxCurrency;

import java.math.BigDecimal;

public interface SandboxService {

    void setCurrencyBalance(
            @NotNull SandboxCurrency currency,
            @NotNull BigDecimal balance,
            @Nullable String brokerAccountId
    );

    void setPositionBalance(@NotNull String ticker, @NotNull BigDecimal balance, @Nullable String brokerAccountId);

    void clearAll(@Nullable String brokerAccountId);

}