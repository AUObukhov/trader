package ru.obukhov.trader.web.model.exchange;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.obukhov.trader.market.model.Candle;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
public class GetCandlesResponse {

    private List<Candle> candles;

    private List<BigDecimal> averages;

}