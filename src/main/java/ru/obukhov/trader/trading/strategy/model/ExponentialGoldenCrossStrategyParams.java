package ru.obukhov.trader.trading.strategy.model;

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
@PredicateConstraint(
        message = "slowWeightDecrease must lower than fastWeightDecrease",
        predicate = ExponentialGoldenCrossStrategyParams.ExponentialGoldenCrossStrategyParamsWeightDecreasesPredicate.class
)
public class ExponentialGoldenCrossStrategyParams extends GoldenCrossStrategyParams {

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

    public ExponentialGoldenCrossStrategyParams(
            Float indexCoefficient,
            Boolean greedy,
            Double fastWeightDecrease,
            Double slowWeightDecrease
    ) {
        super(indexCoefficient, greedy);
        this.fastWeightDecrease = fastWeightDecrease;
        this.slowWeightDecrease = slowWeightDecrease;
    }

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