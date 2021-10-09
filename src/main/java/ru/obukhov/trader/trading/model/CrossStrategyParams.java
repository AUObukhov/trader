package ru.obukhov.trader.trading.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import ru.obukhov.trader.common.model.validation.constraint.PredicateConstraint;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.function.Predicate;

@Getter
@NoArgsConstructor
@PredicateConstraint(message = "smallWindow must lower than bigWindow", predicate = CrossStrategyParams.CrossStrategyParamsWindowsPredicate.class)
public class CrossStrategyParams implements TradingStrategyParams {

    /**
     * Minimum value of profit in percent, which allows selling securities.
     * Negative value means never sell.
     */
    @NotNull(message = "minimumProfit is mandatory")
    protected Float minimumProfit;

    @NotNull(message = "order is mandatory")
    @Min(value = 1, message = "order min value is 1")
    protected Integer order;

    /**
     * relation of index of expected moving averages crossover to prices count. Must be in range [0..1]
     */
    @NotNull(message = "indexCoefficient is mandatory")
    @Min(value = 0, message = "indexCoefficient min value is 0")
    @Max(value = 1, message = "indexCoefficient max value is 1")
    protected Float indexCoefficient;

    /**
     * flag allowing to buy securities even when short-term moving average crosses a long-term moving average from above
     * and selling is not profitable enough
     */
    @NotNull(message = "greedy is mandatory")
    protected Boolean greedy;

    /**
     * window of short-term moving average
     */
    @NotNull(message = "smallWindow is mandatory")
    @Min(value = 1, message = "smallWindow must be positive")
    private Integer smallWindow;

    /**
     * window of long-term moving average
     */
    @NotNull(message = "bigWindow is mandatory")
    private Integer bigWindow;

    public CrossStrategyParams(
            final Float minimumProfit,
            final Integer order,
            final Float indexCoefficient,
            final Boolean greedy,
            final Integer smallWindow,
            final Integer bigWindow
    ) {
        this.minimumProfit = minimumProfit;
        this.order = order;
        this.indexCoefficient = indexCoefficient;
        this.greedy = greedy;
        this.smallWindow = smallWindow;
        this.bigWindow = bigWindow;
    }

    @Override
    public String toString() {
        return "[" +
                "minimumProfit=" + minimumProfit +
                ", order=" + order +
                ", indexCoefficient=" + indexCoefficient +
                ", greedy=" + greedy +
                ", smallWindow=" + smallWindow +
                ", bigWindow=" + bigWindow +
                ']';
    }

    protected static class CrossStrategyParamsWindowsPredicate implements Predicate<CrossStrategyParams> {
        @Override
        public boolean test(CrossStrategyParams params) {
            return params.getSmallWindow() == null
                    || params.getBigWindow() == null
                    || params.getSmallWindow() < params.getBigWindow();
        }
    }

}
