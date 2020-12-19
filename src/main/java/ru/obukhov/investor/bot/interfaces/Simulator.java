package ru.obukhov.investor.bot.interfaces;

import ru.obukhov.investor.model.Interval;
import ru.obukhov.investor.web.model.SimulateResponse;

import java.math.BigDecimal;

public interface Simulator {

    SimulateResponse simulate(String ticker, BigDecimal balance, Interval interval);

}