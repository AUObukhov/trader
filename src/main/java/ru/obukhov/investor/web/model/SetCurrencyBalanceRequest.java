package ru.obukhov.investor.web.model;

import lombok.Data;
import org.jetbrains.annotations.Nullable;
import ru.tinkoff.invest.openapi.models.Currency;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
@Valid
public class SetCurrencyBalanceRequest {

    @NotNull
    private Currency currency;

    @NotNull
    private BigDecimal balance;

    @Nullable
    private String brokerAccountId;

}