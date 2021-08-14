package ru.obukhov.trader.web.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.quartz.CronExpression;
import ru.obukhov.trader.common.util.DecimalUtils;
import ru.obukhov.trader.web.model.validation.constraint.NullabilityConsistent;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Objects;

@Data
@NoArgsConstructor
@AllArgsConstructor
@NullabilityConsistent(
        fields = {"balanceIncrement", "balanceIncrementCron"},
        message = "balanceIncrement and balanceIncrementCron must be both null or not null"
)
public class BalanceConfig {

    @NotNull(message = "initial balance is mandatory")
    @ApiModelProperty(value = "Initial balance before the simulation in ticker currency", example = "100000", required = true, position = 1)
    private BigDecimal initialBalance;

    @Min(value = 1, message = "balanceIncrement must be positive")
    @ApiModelProperty(value = "Sum to add to balance", example = "1000", allowableValues = "range[0, infinity]", position = 2)
    private BigDecimal balanceIncrement;

    @ApiModelProperty(value = "Cron expression describing schedule of balance increments", example = "0 0 0 1 * ?", position = 3)
    private CronExpression balanceIncrementCron;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final BalanceConfig that = (BalanceConfig) o;
        return DecimalUtils.numbersEqual(initialBalance, that.initialBalance)
                && DecimalUtils.numbersEqual(balanceIncrement, that.balanceIncrement)
                && (String.valueOf(balanceIncrementCron).equals(String.valueOf(that.balanceIncrementCron))
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(initialBalance, balanceIncrement, balanceIncrementCron.toString());
    }

}