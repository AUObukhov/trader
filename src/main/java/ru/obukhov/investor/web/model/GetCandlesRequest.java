package ru.obukhov.investor.web.model;

import lombok.Builder;
import lombok.Data;
import ru.tinkoff.invest.openapi.models.market.CandleInterval;

import java.time.OffsetDateTime;

@Data
@Builder
public class GetCandlesRequest {
    private String token;
    private String ticker;
    private OffsetDateTime from;
    private OffsetDateTime to;
    private CandleInterval candleInterval;

    // due to some lombok internal error
    public CandleInterval getCandleInterval() {
        return this.candleInterval;
    }
}