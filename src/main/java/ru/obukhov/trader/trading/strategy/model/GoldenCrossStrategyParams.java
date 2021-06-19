package ru.obukhov.trader.trading.strategy.model;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Valid
@Getter
@NoArgsConstructor
public abstract class GoldenCrossStrategyParams extends TradingStrategyParams {

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
     * flag allowing to buy papers even when short-term moving average crosses a long-term moving average from above
     * and selling is not profitable enough
     */
    @NotNull(message = "greedy is mandatory")
    protected Boolean greedy;

    protected GoldenCrossStrategyParams(
            final Float minimumProfit,
            final Integer order,
            final Float indexCoefficient,
            final Boolean greedy
    ) {
        super(minimumProfit);

        this.order = order;
        this.indexCoefficient = indexCoefficient;
        this.greedy = greedy;
    }

}
