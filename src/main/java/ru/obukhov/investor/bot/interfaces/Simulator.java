package ru.obukhov.investor.bot.interfaces;

import ru.obukhov.investor.common.model.Interval;
import ru.obukhov.investor.web.model.pojo.SimulationResult;
import ru.obukhov.investor.web.model.pojo.SimulationUnit;

import java.util.List;
import java.util.Map;

public interface Simulator {

    Map<String, List<SimulationResult>> simulate(List<SimulationUnit> simulationUnits, Interval interval, boolean saveToFiles);
}