package ru.obukhov.trader.trading.strategy.impl;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import ru.obukhov.trader.common.model.validation.constraint.PredicateConstraint;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.function.Predicate;

@Valid
@Getter
@NoArgsConstructor
@AllArgsConstructor
@PredicateConstraint(
        message = "slowWeightDecrease must lower than fastWeightDecrease",
        predicate = ExponentialGoldenCrossStrategyParams.ExponentialGoldenCrossStrategyParamsWeightDecreasesPredicate.class
)
public class ExponentialGoldenCrossStrategyParams {

    /**
     * coefficient, representing speed of weight decrease. Must be in range [0..1]
     */
    @NotNull(message = "fastWeightDecrease is mandatory")
    @Min(value = 0, message = "fastWeightDecrease min value is 0")
    @Max(value = 1, message = "fastWeightDecrease max value is 1")
    private Double fastWeightDecrease;

    /**
     * coefficient, representing speed of weight decrease. Must be in range [0..1]
     */
    @NotNull(message = "slowWeightDecrease is mandatory")
    @Min(value = 0, message = "slowWeightDecrease min value is 0")
    @Max(value = 1, message = "slowWeightDecrease max value is 1")
    private Double slowWeightDecrease;

    /**
     * relation of index of expected moving averages crossover to prices count. Must be in range [0..1]
     */
    @NotNull(message = "indexCoefficient is mandatory")
    @Min(value = 0, message = "indexCoefficient min value is 0")
    @Max(value = 1, message = "indexCoefficient max value is 1")
    private Float indexCoefficient;

    /**
     * flag allowing to buy papers even when short-term moving average crosses a long-term moving average from above
     * and selling is not profitable enough
     */
    @NotNull(message = "greedy is mandatory")
    private Boolean greedy;

    protected static class ExponentialGoldenCrossStrategyParamsWeightDecreasesPredicate
            implements Predicate<ExponentialGoldenCrossStrategyParams> {
        @Override
        public boolean test(ExponentialGoldenCrossStrategyParams params) {
            return params.getFastWeightDecrease() == null
                    || params.getSlowWeightDecrease() == null
                    || params.getSlowWeightDecrease() < params.getFastWeightDecrease();
        }
    }

}