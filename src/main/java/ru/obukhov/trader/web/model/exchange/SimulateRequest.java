package ru.obukhov.trader.web.model.exchange;

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
@Valid

public class SimulateRequest {

    @NotBlank(message = "ticker is mandatory")
    private String ticker;

    @NotNull(message = "balanceConfig is mandatory")
    private BalanceConfig balanceConfig;

    @NotNull(message = "from is mandatory")
    private OffsetDateTime from;

    private OffsetDateTime to;

    @Valid
    @NotEmpty(message = "tradingConfigs is mandatory")
    private List<TradingConfig> tradingConfigs;

    private Boolean saveToFiles;

}