package ru.obukhov.trader.web.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.quartz.CronExpression;
import ru.obukhov.trader.common.util.DecimalUtils;
import ru.obukhov.trader.web.model.validation.constraint.NullabilityConsistent;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Objects;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Valid
@NullabilityConsistent(
        fields = {"balanceIncrement", "balanceIncrementCron"},
        message = "balanceIncrement and balanceIncrementCron must be both null or not null"
)
public class BalanceConfig {

    @NotNull(message = "initial balance is mandatory")
    private BigDecimal initialBalance;

    @Min(value = 1, message = "balanceIncrement must be positive")
    private BigDecimal balanceIncrement;

    private CronExpression balanceIncrementCron;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        BalanceConfig that = (BalanceConfig) o;
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