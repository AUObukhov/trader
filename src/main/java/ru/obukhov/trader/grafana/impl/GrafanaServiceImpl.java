package ru.obukhov.trader.grafana.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.common.util.MapUtils;
import ru.obukhov.trader.grafana.interfaces.GrafanaService;
import ru.obukhov.trader.grafana.model.Column;
import ru.obukhov.trader.grafana.model.ColumnType;
import ru.obukhov.trader.grafana.model.GetDataRequest;
import ru.obukhov.trader.grafana.model.Metric;
import ru.obukhov.trader.grafana.model.QueryResult;
import ru.obukhov.trader.grafana.model.QueryTableResult;
import ru.obukhov.trader.grafana.model.Target;
import ru.obukhov.trader.market.impl.MarketService;
import ru.obukhov.trader.market.impl.StatisticsService;
import ru.obukhov.trader.market.model.Candle;
import ru.obukhov.trader.market.model.MovingAverageType;
import ru.obukhov.trader.web.model.exchange.GetCandlesResponse;
import ru.tinkoff.invest.openapi.model.rest.CandleResolution;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GrafanaServiceImpl implements GrafanaService {

    private static final List<Column> CANDLES_COLUMNS = List.of(
            new Column("time", ColumnType.TIME),
            new Column("open price", ColumnType.NUMBER)
    );

    private final MarketService marketService;
    private final StatisticsService statisticsService;

    /**
     * @param request params of data retrieval
     * @return list of single {@link QueryResult} containing data
     */
    @Override
    public List<QueryResult> getData(GetDataRequest request) {
        Metric metric = getRequiredMetric(request);
        return switch (metric) {
            case CANDLES -> getCandles(request);
            case EXTENDED_CANDLES -> getExtendedCandles(request);
        };
    }

    private Metric getRequiredMetric(GetDataRequest request) {
        List<Target> targets = request.getTargets();
        Assert.isTrue(targets.size() == 1, "Expected single target");
        return targets.get(0).getMetric();
    }

    /**
     * @param request params of candles retrieval.
     *                Must contain single values in targets and keys "ticker" and "candleResolution" in targets[0].data
     * @return list of single {@link QueryResult} containing candles times and open prices
     */
    private List<QueryResult> getCandles(GetDataRequest request) {
        final Map<String, Object> data = getRequiredTargetData(request);
        final String ticker = MapUtils.getNotBlankString(data, "ticker");
        final CandleResolution candleResolution = getRequiredCandleResolution(data);

        final QueryResult queryResult = getCandles(ticker, request.getInterval(), candleResolution);

        return List.of(queryResult);
    }

    /**
     * @param request params of candles retrieval.
     *                Must contain single values in targets and keys "ticker" and "candleResolution" in targets[0].data
     * @return list of single {@link QueryResult} containing candles times, open prices and moving average values
     */
    private List<QueryResult> getExtendedCandles(GetDataRequest request) {
        final Map<String, Object> data = getRequiredTargetData(request);
        final String ticker = MapUtils.getNotBlankString(data, "ticker");
        final Interval interval = request.getInterval();
        final CandleResolution candleResolution = getRequiredCandleResolution(data);
        final MovingAverageType movingAverageType = getRequiredMovingAverageType(data);
        final Integer window1 = MapUtils.getRequiredInteger(data, "window1");
        final Integer window2 = MapUtils.getRequiredInteger(data, "window2");

        return getExtendedCandles(
                ticker, interval, candleResolution, movingAverageType, window1, window2
        );
    }

    private Map<String, Object> getRequiredTargetData(GetDataRequest request) {
        final Map<String, Object> data = (Map<String, Object>) request.getTargets().get(0).getData();
        Assert.isTrue(data != null, "data is mandatory");
        return data;
    }

    private CandleResolution getRequiredCandleResolution(Map<String, Object> data) {
        final String candleResolution = MapUtils.getRequiredString(data, "candleResolution");
        return CandleResolution.fromValue(candleResolution);
    }

    private MovingAverageType getRequiredMovingAverageType(Map<String, Object> data) {
        final String movingAverageType = MapUtils.getRequiredString(data, "movingAverageType");
        return MovingAverageType.from(movingAverageType);
    }

    private QueryResult getCandles(final String ticker, final Interval interval, final CandleResolution candleResolution) {
        final QueryTableResult queryResult = new QueryTableResult();
        queryResult.setColumns(CANDLES_COLUMNS);

        final List<List<Object>> rows = new ArrayList<>();
        final List<Candle> candles = marketService.getCandles(ticker, interval, candleResolution);
        for (Candle candle : candles) {
            rows.add(List.of(candle.getTime(), candle.getOpenPrice()));
        }
        queryResult.setRows(rows);

        return queryResult;
    }

    private List<QueryResult> getExtendedCandles(
            final String ticker,
            final Interval interval,
            final CandleResolution candleResolution,
            final MovingAverageType movingAverageType,
            final Integer window1,
            final Integer window2
    ) {
        final GetCandlesResponse candlesResponse = statisticsService.getExtendedCandles(
                ticker,
                interval,
                candleResolution,
                movingAverageType,
                window1,
                window2
        );

        final QueryTableResult valuesAndAveragesResult = new QueryTableResult();
        valuesAndAveragesResult.setColumns(getExtendedCandlesColumns(movingAverageType, window1, window2));

        final List<List<Object>> valuesAndAveragesRows = new ArrayList<>();
        for (int i = 0; i < candlesResponse.getCandles().size(); i++) {
            final Candle candle = candlesResponse.getCandles().get(i);
            final BigDecimal average1 = candlesResponse.getAverages1().get(i);
            final BigDecimal average2 = candlesResponse.getAverages2().get(i);
            valuesAndAveragesRows.add(List.of(candle.getTime(), candle.getOpenPrice(), average1, average2));
        }
        valuesAndAveragesResult.setRows(valuesAndAveragesRows);

        return List.of(valuesAndAveragesResult);
    }

    private List<Column> getExtendedCandlesColumns(final MovingAverageType movingAverageType, final Integer window1, final Integer window2) {
        return List.of(
                new Column("time", ColumnType.TIME),
                new Column("open price", ColumnType.NUMBER),
                new Column(movingAverageType.getValue() + "(" + window1 + ")", ColumnType.NUMBER),
                new Column(movingAverageType.getValue() + "(" + window2 + ")", ColumnType.NUMBER)
        );
    }

}