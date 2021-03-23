package ru.obukhov.trader.web.model.exchange;

import lombok.Data;
import org.jetbrains.annotations.Nullable;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
@Valid
public class SetPositionBalanceRequest {

    @NotEmpty(message = "ticker is mandatory")
    private String ticker;

    @NotNull(message = "balance is mandatory")
    private BigDecimal balance;

    @Nullable
    private String brokerAccountId;
}