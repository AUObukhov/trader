package ru.obukhov.trader.web.model.exchange;

import lombok.Data;
import org.jetbrains.annotations.Nullable;
import ru.tinkoff.invest.openapi.model.rest.SandboxCurrency;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
@Valid
public class SetCurrencyBalanceRequest {

    @NotNull(message = "currency is mandatory")
    private SandboxCurrency currency;

    @NotNull(message = "balance is mandatory")
    private BigDecimal balance;

    @Nullable
    private String brokerAccountId;

}