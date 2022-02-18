package ru.obukhov.trader.web.client.exchange;

import lombok.Data;
import ru.obukhov.trader.market.model.MarketInstrumentList;

@Data
public class MarketInstrumentListResponse {

    private String trackingId;

    private String status;

    private MarketInstrumentList payload;

}