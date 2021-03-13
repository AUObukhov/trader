package ru.obukhov.trader.bot.interfaces;

import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.web.model.pojo.SimulationResult;
import ru.obukhov.trader.web.model.pojo.SimulationUnit;

import java.util.List;
import java.util.Map;

public interface Simulator {

    Map<String, List<SimulationResult>> simulate(List<SimulationUnit> simulationUnits, Interval interval, boolean saveToFiles);
}