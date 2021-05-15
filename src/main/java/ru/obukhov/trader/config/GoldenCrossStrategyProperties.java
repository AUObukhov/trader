package ru.obukhov.trader.config;

import lombok.Data;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.validation.annotation.Validated;
import ru.obukhov.trader.bot.strategy.impl.GoldenCrossStrategy;
import ru.obukhov.trader.common.model.validation.constraint.PredicateConstraint;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Positive;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Container of configurations for {@link GoldenCrossStrategy}
 */
@Data
@Validated
@ConstructorBinding
@ConfigurationProperties(prefix = "golden-cross-strategy")
public class GoldenCrossStrategyProperties {

    @Getter
    @NotEmpty(message = "configs must not be empty")
    private final Set<StrategyConfig> configs;

    public GoldenCrossStrategyProperties(Set<StrategyConfig> configs) {
        this.configs = Set.copyOf(configs);
    }

    @Data
    @PredicateConstraint(
            message = "smallWindow must not be greater than bigWindow",
            predicate = StrategyConfigWindowsPredicate.class
    )
    public static class StrategyConfig {

        @Min(value = 1, message = "smallWindow must not be lower than 1")
        private final int smallWindow;

        private final int bigWindow;

        @Positive(message = "indexCoefficient must be positive")
        @Max(value = 1, message = "indexCoefficient must not be greater than 1")
        private final float indexCoefficient;

    }

    private static class StrategyConfigWindowsPredicate implements Predicate<StrategyConfig> {
        @Override
        public boolean test(StrategyConfig config) {
            return config.getSmallWindow() <= config.getBigWindow();
        }
    }

}