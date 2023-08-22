package ru.obukhov.trader.web.model;

import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.quartz.CronExpression;
import ru.obukhov.trader.web.model.validation.constraint.NullabilityConsistent;
import ru.obukhov.trader.web.model.validation.constraint.QuotationPositive;
import ru.tinkoff.piapi.contract.v1.Quotation;

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
    @ApiModelProperty(value = "Initial balance before the back test", required = true, position = 1, example = "100000")
    private Quotation initialBalance;

    @QuotationPositive(message = "balanceIncrement must be positive")
    @ApiModelProperty(value = "Sum to add to balance", allowableValues = "range[0, infinity]", position = 2, example = "1000")
    private Quotation balanceIncrement;

    @ApiModelProperty(value = "Cron expression describing schedule of balance increments", position = 3, example = "0 0 0 1 * ?")
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
        return Objects.equals(initialBalance, that.initialBalance)
                && Objects.equals(balanceIncrement, that.balanceIncrement)
                && (String.valueOf(balanceIncrementCron).equals(String.valueOf(that.balanceIncrementCron))
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(initialBalance, balanceIncrement, balanceIncrementCron.toString());
    }

}