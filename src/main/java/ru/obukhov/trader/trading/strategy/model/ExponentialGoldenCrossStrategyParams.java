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
            final Float minimumProfit,
            final Integer order,
            final Float indexCoefficient,
            final Boolean greedy,
            final Double fastWeightDecrease,
            final Double slowWeightDecrease
    ) {
        super(minimumProfit, order, indexCoefficient, greedy);

        this.fastWeightDecrease = fastWeightDecrease;
        this.slowWeightDecrease = slowWeightDecrease;
    }

    @Override
    public String toString() {
        return "[" +
                "minimumProfit=" + minimumProfit +
                ", indexCoefficient=" + indexCoefficient +
                ", greedy=" + greedy +
                ", fastWeightDecrease=" + fastWeightDecrease +
                ", slowWeightDecrease=" + slowWeightDecrease +
                ']';
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