package ru.obukhov.investor.web.model;

import lombok.Data;
import ru.obukhov.investor.model.TickerType;
import ru.tinkoff.invest.openapi.models.market.CandleInterval;

import java.time.OffsetDateTime;

@Data
public class GetCandlesRequest {
    private String token;
    private String ticker;
    private TickerType tickerType;
    private OffsetDateTime from;
    private OffsetDateTime to;
    private CandleInterval candleInterval;

    // due to some lombok internal error
    public CandleInterval getCandleInterval() {
        return this.candleInterval;
    }
}