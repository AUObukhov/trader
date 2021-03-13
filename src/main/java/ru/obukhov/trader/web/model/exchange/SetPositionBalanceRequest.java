package ru.obukhov.trader.web.model.exchange;

import lombok.Data;
import org.jetbrains.annotations.Nullable;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
@Valid
public class SetPositionBalanceRequest {

    @NotNull
    private String ticker;

    @NotNull
    private BigDecimal balance;

    @Nullable
    private String brokerAccountId;
}