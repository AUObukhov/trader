package ru.obukhov.trader.trading.strategy.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TradingStrategyParams {

    /**
     * minimum value of profit in percent, which allows to sell papers
     */
    @NotNull(message = "minimumProfit is mandatory")
    @Min(value = 0, message = "minimumProfit min value is 0")
    protected Float minimumProfit;

    @Override
    public String toString() {
        return "[" +
                "minimumProfit=" + minimumProfit +
                ']';
    }
}