package ru.obukhov.trader.market.interfaces;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.tinkoff.invest.openapi.model.rest.SandboxCurrency;

import java.math.BigDecimal;

public interface SandboxService {

    void setCurrencyBalance(
            @NotNull final SandboxCurrency currency,
            @NotNull final BigDecimal balance,
            @Nullable final String brokerAccountId
    );

    void setPositionBalance(
            @NotNull final String ticker,
            @NotNull final BigDecimal balance,
            @Nullable final String brokerAccountId
    );

    void clearAll(@Nullable final String brokerAccountId);

}