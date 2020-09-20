package ru.obukhov.investor.web.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.tinkoff.invest.openapi.models.orders.Order;

import java.util.List;

@Data
@AllArgsConstructor
public class GetOrdersResponse {

    private List<Order> orders;

}