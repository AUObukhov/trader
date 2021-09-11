package ru.obukhov.trader.trading.simulation.interfaces;

import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.web.model.BalanceConfig;
import ru.obukhov.trader.web.model.SimulationResult;
import ru.obukhov.trader.web.model.TradingConfig;

import java.util.List;

public interface Simulator {

    List<SimulationResult> simulate(
            final List<TradingConfig> tradingConfigs,
            final BalanceConfig balanceConfig,
            final Interval interval,
            final boolean saveToFiles
    );

}