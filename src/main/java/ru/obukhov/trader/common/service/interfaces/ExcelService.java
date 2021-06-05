package ru.obukhov.trader.common.service.interfaces;

import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.web.model.exchange.GetCandlesResponse;
import ru.obukhov.trader.web.model.pojo.SimulationResult;

import java.util.Collection;

public interface ExcelService {

    void saveSimulationResults(final String ticker, final Collection<SimulationResult> results);

    void saveCandles(final String ticker, final Interval interval, final GetCandlesResponse response);

}