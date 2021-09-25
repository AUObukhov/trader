package ru.obukhov.trader.trading.backtest.interfaces;

import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.trading.model.BackTestResult;
import ru.obukhov.trader.web.model.BalanceConfig;
import ru.obukhov.trader.web.model.BotConfig;

import java.util.List;

public interface BackTester {

    List<BackTestResult> test(
            final List<BotConfig> botConfigs,
            final BalanceConfig balanceConfig,
            final Interval interval,
            final boolean saveToFiles
    );

}