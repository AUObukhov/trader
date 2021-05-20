package ru.obukhov.trader.bot.interfaces;

import org.quartz.CronExpression;
import ru.obukhov.trader.bot.model.StrategyConfig;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.web.model.pojo.SimulationResult;

import java.math.BigDecimal;
import java.util.List;

public interface Simulator {
    List<SimulationResult> simulate(
            String ticker,
            BigDecimal initialBalance,
            BigDecimal balanceIncrement,
            CronExpression balanceIncrementCron,
            List<StrategyConfig> strategiesConfigs,
            Interval interval,
            boolean saveToFiles
    );
}