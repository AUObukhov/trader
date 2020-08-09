package ru.obukhov.investor.web.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.tinkoff.invest.openapi.models.market.Candle;

import java.util.List;

@Data
@AllArgsConstructor
public class GetCandlesResponse {
    private List<Candle> candles;
}