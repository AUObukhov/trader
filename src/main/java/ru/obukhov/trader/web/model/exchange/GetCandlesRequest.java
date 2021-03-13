package ru.obukhov.trader.web.model.exchange;

import lombok.Builder;
import lombok.Data;
import ru.tinkoff.invest.openapi.models.market.CandleInterval;

import java.time.OffsetDateTime;

@Data
@Builder
public class GetCandlesRequest {
    private String ticker;
    private OffsetDateTime from;
    private OffsetDateTime to;
    private CandleInterval candleInterval;

    // due to some lombok internal error
    public CandleInterval getCandleInterval() {
        return this.candleInterval;
    }
}