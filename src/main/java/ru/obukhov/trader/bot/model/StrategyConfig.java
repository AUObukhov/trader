package ru.obukhov.trader.bot.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.validation.constraints.NotNull;
import java.util.Map;

@Data
@NoArgsConstructor
@ConfigurationProperties("strategy-config")
public class StrategyConfig {

    @NotNull(message = "type in StrategyConfig is mandatory")
    private StrategyType type;

    private Map<String, Object> params;

    public StrategyConfig(StrategyType strategyType) {
        this.type = strategyType;
    }

}
