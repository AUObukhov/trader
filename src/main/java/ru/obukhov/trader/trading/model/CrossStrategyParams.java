package ru.obukhov.trader.trading.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import ru.obukhov.trader.common.model.validation.PredicateConstraint;

import java.util.function.Predicate;

@Getter
@NoArgsConstructor
@PredicateConstraint(message = "smallWindow must lower than bigWindow", predicate = CrossStrategyParams.CrossStrategyParamsWindowsPredicate.class)
public class CrossStrategyParams implements TradingStrategyParams {

    @NotNull(message = "minimumProfit is mandatory")
    protected Float minimumProfit;

    @NotNull(message = "order is mandatory")
    @Min(value = 1, message = "order min value is 1")
    protected Integer order;

    @NotNull(message = "indexCoefficient is mandatory")
    @Min(value = 0, message = "indexCoefficient min value is 0")
    @Max(value = 1, message = "indexCoefficient max value is 1")
    protected Float indexCoefficient;

    @NotNull(message = "greedy is mandatory")
    protected Boolean greedy;

    @NotNull(message = "smallWindow is mandatory")
    @Min(value = 1, message = "smallWindow must be positive")
    private Integer smallWindow;

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
