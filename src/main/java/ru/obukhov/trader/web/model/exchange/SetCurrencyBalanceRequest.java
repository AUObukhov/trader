package ru.obukhov.trader.web.model.exchange;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;

@Data
public class SetCurrencyBalanceRequest {

    @Nullable
    private String accountId;

    @NotNull(message = "currency is mandatory")
    private String currency;

    @NotNull(message = "balance is mandatory")
    private BigDecimal balance;

}