package ru.obukhov.trader.web.client.exchange;

import lombok.Data;
import ru.obukhov.trader.market.model.Candles;

@Data
public class CandlesResponse {

    private String trackingId;

    private String status;

    private Candles payload;

}