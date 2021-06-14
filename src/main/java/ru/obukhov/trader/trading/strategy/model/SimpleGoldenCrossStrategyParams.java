package ru.obukhov.trader.trading.strategy.model;

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
        message = "smallWindow must lower than bigWindow",
        predicate = SimpleGoldenCrossStrategyParams.GoldenCrossStrategyParamsWindowsPredicate.class
)
public class SimpleGoldenCrossStrategyParams {

    /**
     * window of short-term moving average
     */
    @NotNull(message = "smallWindow is mandatory")
    @Min(value = 1, message = "smallWindow min value is 1")
    private Integer smallWindow;

    /**
     * window of long-term moving average
     */
    @NotNull(message = "bigWindow is mandatory")
    private Integer bigWindow;

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

    protected static class GoldenCrossStrategyParamsWindowsPredicate implements Predicate<SimpleGoldenCrossStrategyParams> {
        @Override
        public boolean test(SimpleGoldenCrossStrategyParams params) {
            return params.getSmallWindow() == null
                    || params.getBigWindow() == null
                    || params.getSmallWindow() < params.getBigWindow();
        }
    }
}