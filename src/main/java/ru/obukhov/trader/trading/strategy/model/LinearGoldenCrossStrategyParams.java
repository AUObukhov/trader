package ru.obukhov.trader.trading.strategy.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import ru.obukhov.trader.common.model.validation.constraint.PredicateConstraint;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.function.Predicate;

@Valid
@Getter
@NoArgsConstructor
@PredicateConstraint(
        message = "smallWindow must lower than bigWindow",
        predicate = LinearGoldenCrossStrategyParams.LinearGoldenCrossStrategyParamsWindowsPredicate.class
)
public class LinearGoldenCrossStrategyParams extends GoldenCrossStrategyParams {

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

    public LinearGoldenCrossStrategyParams(
            Float indexCoefficient,
            Boolean greedy,
            Integer smallWindow,
            Integer bigWindow
    ) {
        super(indexCoefficient, greedy);

        this.smallWindow = smallWindow;
        this.bigWindow = bigWindow;
    }

    protected static class LinearGoldenCrossStrategyParamsWindowsPredicate
            implements Predicate<LinearGoldenCrossStrategyParams> {
        @Override
        public boolean test(LinearGoldenCrossStrategyParams params) {
            return params.getSmallWindow() == null
                    || params.getBigWindow() == null
                    || params.getSmallWindow() < params.getBigWindow();
        }
    }

}