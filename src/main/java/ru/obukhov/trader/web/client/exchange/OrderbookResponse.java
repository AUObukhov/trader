package ru.obukhov.trader.web.client.exchange;

import lombok.Data;
import ru.obukhov.trader.market.model.Orderbook;

@Data
public class OrderbookResponse {

    private String trackingId;

    private String status;

    private Orderbook payload;

}