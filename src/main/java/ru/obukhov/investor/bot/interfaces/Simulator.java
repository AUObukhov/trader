package ru.obukhov.investor.bot.interfaces;

import ru.obukhov.investor.model.Interval;
import ru.obukhov.investor.web.model.SimulationResult;

import java.util.List;

public interface Simulator {

    List<SimulationResult> simulate(String ticker, Interval interval);

}