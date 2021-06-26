package ru.obukhov.trader.trading.simulation.interfaces;

import org.quartz.CronExpression;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.web.model.SimulationResult;
import ru.obukhov.trader.web.model.TradingConfig;

import java.math.BigDecimal;
import java.util.List;

public interface Simulator {
    List<SimulationResult> simulate(
            final String ticker,
            final BigDecimal initialBalance,
            final BigDecimal balanceIncrement,
            final CronExpression balanceIncrementCron,
            final List<TradingConfig> tradingConfigs,
            final Interval interval,
            final boolean saveToFiles
    );
}