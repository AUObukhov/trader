package ru.obukhov.trader.web.model.exchange;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.obukhov.trader.market.model.Order;

import java.util.List;

@Data
@AllArgsConstructor
public class GetOrdersResponse {

    private List<Order> orders;

}