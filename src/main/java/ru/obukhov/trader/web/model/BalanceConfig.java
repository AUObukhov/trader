package ru.obukhov.trader.web.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;
import org.springframework.scheduling.support.CronExpression;
import ru.obukhov.trader.web.model.validation.constraint.NullabilityConsistent;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
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

    @JsonCreator
    public BalanceConfig(
            @JsonProperty("initialBalances") final Map<String, BigDecimal> initialBalances,
            @JsonProperty("balanceIncrements") @Nullable final Map<String, BigDecimal> balanceIncrements,
            @JsonProperty("balanceIncrementCron") @Nullable final String balanceIncrementCron
    ) {
        this.initialBalances = new HashMap<>(initialBalances);
        this.balanceIncrements = balanceIncrements == null ? null : new HashMap<>(balanceIncrements);
        this.balanceIncrementCron = balanceIncrementCron == null ? null : CronExpression.parse(balanceIncrementCron);
    }

}