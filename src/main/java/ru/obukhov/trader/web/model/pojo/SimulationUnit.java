package ru.obukhov.trader.web.model.pojo;

import lombok.Data;
import org.quartz.CronExpression;
import ru.obukhov.trader.web.model.validation.constraint.NullabilityConsistent;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
@Valid
@NullabilityConsistent(
        fields = {"balanceIncrement", "balanceIncrementCron"},
        message = "balanceIncrement and balanceIncrementCron must be both null or not null"
)
public class SimulationUnit {

    @NotBlank(message = "ticker in simulation unit is mandatory")
    private String ticker;

    @NotNull(message = "initial balance in simulation unit is mandatory")
    private BigDecimal initialBalance;

    private BigDecimal balanceIncrement;

    private CronExpression balanceIncrementCron;

    public boolean isBalanceIncremented() {
        return balanceIncrement != null && balanceIncrementCron != null;
    }

}