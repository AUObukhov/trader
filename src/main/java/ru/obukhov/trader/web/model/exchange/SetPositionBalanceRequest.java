package ru.obukhov.trader.web.model.exchange;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;

@Data
public class SetPositionBalanceRequest {

    @NotEmpty(message = "figi is mandatory")
    private String figi;

    @NotNull(message = "balance is mandatory")
    private BigDecimal balance;

    @Nullable
    private String accountId;
}