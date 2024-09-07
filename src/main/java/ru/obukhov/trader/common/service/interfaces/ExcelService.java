package ru.obukhov.trader.common.service.interfaces;

import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.trading.model.BackTestResult;
import ru.obukhov.trader.web.model.exchange.GetCandlesResponse;
import ru.obukhov.trader.web.model.exchange.WeightedShare;

import java.util.Collection;
import java.util.List;

public interface ExcelService {

    void saveBackTestResults(final Collection<BackTestResult> results);

    void saveCandles(final String figi, final Interval interval, final GetCandlesResponse response);

    void saveWeightedShares(final List<WeightedShare> weightedShares);

}