package ru.obukhov.investor.bot.interfaces;

import ru.obukhov.investor.common.model.Interval;
import ru.obukhov.investor.web.model.pojo.SimulationResult;

import java.math.BigDecimal;
import java.util.List;

public interface Simulator {

    List<SimulationResult> simulate(String ticker, BigDecimal balance, Interval interval, boolean saveToFile);

}