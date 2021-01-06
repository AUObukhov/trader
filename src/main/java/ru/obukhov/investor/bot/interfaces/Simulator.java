package ru.obukhov.investor.bot.interfaces;

import ru.obukhov.investor.model.Interval;
import ru.obukhov.investor.web.model.SimulationResult;

import java.math.BigDecimal;

public interface Simulator {

    SimulationResult simulate(String ticker, BigDecimal balance, Interval interval);

}