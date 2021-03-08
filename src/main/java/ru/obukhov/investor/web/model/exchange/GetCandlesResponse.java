package ru.obukhov.investor.web.model.exchange;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.obukhov.investor.market.model.Candle;

import java.util.List;

@Data
@AllArgsConstructor
public class GetCandlesResponse {
    private List<Candle> candles;
}