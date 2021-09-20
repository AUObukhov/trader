package ru.obukhov.trader.trading.backtest.interfaces;

import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.web.model.BackTestResult;
import ru.obukhov.trader.web.model.BalanceConfig;
import ru.obukhov.trader.web.model.TradingConfig;

import java.util.List;

public interface BackTester {

    List<BackTestResult> test(
            final List<TradingConfig> tradingConfigs,
            final BalanceConfig balanceConfig,
            final Interval interval,
            final boolean saveToFiles
    );

}