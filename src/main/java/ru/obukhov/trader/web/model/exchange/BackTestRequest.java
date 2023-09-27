package ru.obukhov.trader.web.model.exchange;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import ru.obukhov.trader.web.model.BalanceConfig;
import ru.obukhov.trader.web.model.BotConfig;

import java.time.OffsetDateTime;
import java.util.List;

@Data
public class BackTestRequest {

    @NotNull(message = "from is mandatory")
    private OffsetDateTime from;

    private OffsetDateTime to;

    @NotNull(message = "balanceConfig is mandatory")
    private BalanceConfig balanceConfig;

    private Boolean saveToFiles;

    @Valid
    @NotEmpty(message = "botConfigs is mandatory")
    private List<BotConfig> botConfigs;

}