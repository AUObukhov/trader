package ru.obukhov.trader.web.client.exchange;

import lombok.Data;
import ru.obukhov.trader.market.model.PlacedLimitOrder;

@Data
public class LimitOrderResponse {

    private String trackingId;

    private String status;

    private PlacedLimitOrder payload;

}