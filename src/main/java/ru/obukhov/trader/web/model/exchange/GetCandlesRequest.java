package ru.obukhov.trader.web.model.exchange;

import lombok.Builder;
import lombok.Data;
import ru.tinkoff.invest.openapi.model.rest.CandleResolution;

import java.time.OffsetDateTime;

@Data
@Builder
public class GetCandlesRequest {
    private String ticker;
    private OffsetDateTime from;
    private OffsetDateTime to;
    private CandleResolution candleInterval;
    private boolean saveToFile;

    // due to some lombok internal error
    public CandleResolution getCandleInterval() {
        return this.candleInterval;
    }
}