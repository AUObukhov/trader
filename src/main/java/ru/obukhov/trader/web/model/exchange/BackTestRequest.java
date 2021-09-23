package ru.obukhov.trader.web.model.exchange;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import ru.obukhov.trader.web.model.BalanceConfig;
import ru.obukhov.trader.web.model.BotConfig;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.List;

@Data
public class BackTestRequest {

    @NotNull(message = "from is mandatory")
    @ApiModelProperty(
            value = "The back test start date time",
            required = true,
            position = 2,
            example = "2019-01-01T00:00:00+03:00")
    private OffsetDateTime from;

    @ApiModelProperty(
            value = "The back test end date time. Default value is current date time",
            position = 3,
            example = "2020-01-01T00:00:00+03:00"
    )
    private OffsetDateTime to;

    @NotNull(message = "balanceConfig is mandatory")
    @ApiModelProperty(value = "configuration of balance in the back test", required = true, position = 4)
    private BalanceConfig balanceConfig;

    @ApiModelProperty(
            value = "Flag indicating to save the back test result to a file. Default value is false",
            position = 4
    )
    private Boolean saveToFiles;

    @Valid
    @NotEmpty(message = "botConfigs is mandatory")
    @ApiModelProperty(
            value = "List of bot configurations. Separate back test created for each config",
            required = true,
            position = 5
    )
    private List<BotConfig> botConfigs;

}