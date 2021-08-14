package ru.obukhov.trader.web.model.exchange;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import ru.obukhov.trader.web.model.BalanceConfig;
import ru.obukhov.trader.web.model.TradingConfig;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.List;

@Data
public class SimulateRequest {

    @NotBlank(message = "ticker is mandatory")
    @ApiModelProperty(
            value = "Ticker on which the simulation will run",
            example = "FXIT",
            required = true,
            position = 1
    )
    private String ticker;

    @NotNull(message = "from is mandatory")
    @ApiModelProperty(
            value = "The simulation start date time",
            example = "2019-01-01T00:00:00+03:00",
            required = true,
            position = 2)
    private OffsetDateTime from;

    @ApiModelProperty(
            value = "The simulation end date time. Default value is current date time",
            example = "2020-01-01T00:00:00+03:00",
            position = 3
    )
    private OffsetDateTime to;

    @NotNull(message = "balanceConfig is mandatory")
    @ApiModelProperty(value = "configuration of balance in the simulation", required = true, position = 4)
    private BalanceConfig balanceConfig;

    @ApiModelProperty(
            value = "Flag indicating to save the simulation result to a file. Default value is false",
            position = 4
    )
    private Boolean saveToFiles;

    @Valid
    @NotEmpty(message = "tradingConfigs is mandatory")
    @ApiModelProperty(
            value = "List of trading strategies configurations. Separate simulation created for each tradingConfig",
            required = true,
            position = 5
    )
    private List<TradingConfig> tradingConfigs;

}