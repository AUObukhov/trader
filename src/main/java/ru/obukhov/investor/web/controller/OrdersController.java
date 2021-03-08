package ru.obukhov.investor.web.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.obukhov.investor.market.interfaces.OrdersService;
import ru.obukhov.investor.web.model.exchange.GetOrdersResponse;
import ru.tinkoff.invest.openapi.models.orders.Order;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/investor/orders")
public class OrdersController {

    private final OrdersService ordersService;

    public OrdersController(@Qualifier("realOrdersService") OrdersService ordersService) {
        this.ordersService = ordersService;
    }

    @GetMapping("/get")
    public GetOrdersResponse getOrders() {

        List<Order> orders = ordersService.getOrders();

        return new GetOrdersResponse(orders);

    }

}