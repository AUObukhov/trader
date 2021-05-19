package ru.obukhov.trader.web.model.exchange;

import lombok.Data;
import org.quartz.CronExpression;
import ru.obukhov.trader.web.model.validation.constraint.NullabilityConsistent;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@Valid
@NullabilityConsistent(
        fields = {"balanceIncrement", "balanceIncrementCron"},
        message = "balanceIncrement and balanceIncrementCron must be both null or not null"
)
public class SimulateRequest {

    @NotBlank(message = "ticker is mandatory")
    private String ticker;

    @NotNull(message = "initial balance is mandatory")
    private BigDecimal initialBalance;

    private BigDecimal balanceIncrement;

    private CronExpression balanceIncrementCron;

    @NotNull(message = "from is mandatory")
    private OffsetDateTime from;

    private OffsetDateTime to;

    private Boolean saveToFiles;

}