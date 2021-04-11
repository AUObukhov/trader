package ru.obukhov.trader.common.service.interfaces;

import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.market.model.ExtendedCandle;
import ru.obukhov.trader.web.model.pojo.SimulationResult;

import java.io.File;
import java.util.Collection;
import java.util.List;

public interface ExcelService {

    File saveSimulationResults(String ticker, Collection<SimulationResult> results);

    File saveCandles(String ticker, Interval interval, List<ExtendedCandle> candles);

}