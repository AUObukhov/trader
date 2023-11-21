package ru.obukhov.trader.web.model;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.quartz.CronExpression;
import ru.obukhov.trader.web.model.validation.constraint.NullabilityConsistent;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@NullabilityConsistent(
        fields = {"balanceIncrements", "balanceIncrementCron"},
        message = "balanceIncrements and balanceIncrementCron must be both null or not null"
)
public class BalanceConfig {

    @NotEmpty(message = "initial balances are mandatory")
    private Map<String, BigDecimal> initialBalances;

    @Nullable
    private Map<String, BigDecimal> balanceIncrements;

    @Nullable
    private CronExpression balanceIncrementCron;

    public BalanceConfig(
            final Map<String, BigDecimal> initialBalances,
            @Nullable final Map<String, BigDecimal> balanceIncrements,
            @Nullable final CronExpression balanceIncrementCron
    ) {
        this.initialBalances = new HashMap<>(initialBalances);
        this.balanceIncrements = balanceIncrements == null ? null : new HashMap<>(balanceIncrements);
        this.balanceIncrementCron = balanceIncrementCron;
    }

}