package ru.obukhov.investor.service.interfaces;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.tinkoff.invest.openapi.models.Currency;

import java.math.BigDecimal;

public interface SandboxService {

    void setCurrencyBalance(@NotNull Currency currency, @NotNull BigDecimal balance, @Nullable String brokerAccountId);

    void setPositionBalance(@NotNull String ticker, @NotNull BigDecimal balance, @Nullable String brokerAccountId);

    void clearAll(@Nullable String brokerAccountId);

}