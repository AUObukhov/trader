package ru.obukhov.trader.web.client.exchange;

import lombok.Data;
import ru.obukhov.trader.market.model.SearchMarketInstrument;

@Data
public class SearchMarketInstrumentResponse {

    private String trackingId;

    private String status;

    private SearchMarketInstrument payload;

}