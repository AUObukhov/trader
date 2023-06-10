package ru.obukhov.trader.common.service.interfaces;

import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.trading.model.BackTestResult;
import ru.obukhov.trader.web.model.exchange.GetCandlesResponse;

import java.util.Collection;

public interface ExcelService {

    void saveBackTestResults(final Collection<BackTestResult> results);

    void saveCandles(final String figi, final Interval interval, final GetCandlesResponse response);

}