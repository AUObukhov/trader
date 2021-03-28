package ru.obukhov.trader.web.model.exchange;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.obukhov.trader.market.model.ExtendedCandle;

import java.util.List;

@Data
@AllArgsConstructor
public class GetCandlesResponse {
    private List<ExtendedCandle> candles;
}