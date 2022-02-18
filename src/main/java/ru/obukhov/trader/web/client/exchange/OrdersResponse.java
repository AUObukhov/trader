package ru.obukhov.trader.web.client.exchange;

import lombok.Data;
import ru.obukhov.trader.market.model.Order;

import java.util.List;

@Data
public class OrdersResponse {

    private String trackingId;

    private String status;

    private List<Order> payload;

}