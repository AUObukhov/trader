package ru.obukhov.trader.web.model.exchange;

import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;

@Data
public class SetPositionBalanceRequest {

    @NotEmpty(message = "ticker is mandatory")
    @ApiModelProperty(example = "FXIT", required = true, position = 1)
    private String ticker;

    @NotNull(message = "balance is mandatory")
    @ApiModelProperty(example = "10000", required = true, position = 2)
    private BigDecimal balance;

    @Nullable
    @ApiModelProperty(example = "2000124699", position = 3)
    private String accountId;
}