package ru.obukhov.trader.web.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.obukhov.trader.market.interfaces.OrdersService;
import ru.obukhov.trader.web.model.exchange.GetOrdersResponse;
import ru.tinkoff.invest.openapi.model.rest.Order;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/trader/orders")
@SuppressWarnings("unused")
public class OrdersController {

    private final OrdersService ordersService;

    public OrdersController(final OrdersService ordersService) {
        this.ordersService = ordersService;
    }

    @GetMapping("/get")
    public GetOrdersResponse getOrders() {
        final List<Order> orders = ordersService.getOrders();

        return new GetOrdersResponse(orders);
    }

}