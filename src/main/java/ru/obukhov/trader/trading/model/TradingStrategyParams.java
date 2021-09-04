package ru.obukhov.trader.trading.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TradingStrategyParams {

    /**
     * Minimum value of profit in percent, which allows selling papers.
     * Negative value means never sell.
     */
    @NotNull(message = "minimumProfit is mandatory")
    protected Float minimumProfit;

    @Override
    public String toString() {
        return "[minimumProfit=" + minimumProfit + ']';
    }
}