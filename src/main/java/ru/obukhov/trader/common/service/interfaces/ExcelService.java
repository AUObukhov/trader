package ru.obukhov.trader.common.service.interfaces;

import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.web.model.SimulationResult;
import ru.obukhov.trader.web.model.exchange.GetCandlesResponse;

import java.util.Collection;

public interface ExcelService {

    void saveSimulationResults(Collection<SimulationResult> results);

    void saveCandles(final String ticker, final Interval interval, final GetCandlesResponse response);

}