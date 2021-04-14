package ru.obukhov.trader.web.model.exchange;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.obukhov.trader.common.model.Point;
import ru.obukhov.trader.market.model.Candle;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
public class GetCandlesResponse {

    private List<Candle> candles;

    private List<BigDecimal> averages;

    private List<Point> localMinimums;

    private List<Point> localMaximums;

    private List<List<Point>> supportLines;

    private List<List<Point>> resistanceLines;

}