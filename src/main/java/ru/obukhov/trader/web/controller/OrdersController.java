package ru.obukhov.trader.web.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.obukhov.trader.market.impl.RealExtOrdersService;
import ru.obukhov.trader.market.model.OrderState;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/trader/orders")
@SuppressWarnings("unused")
public class OrdersController {

    private final RealExtOrdersService ordersService;

    public OrdersController(final RealExtOrdersService ordersService) {
        this.ordersService = ordersService;
    }

    @GetMapping("/get")
    public List<OrderState> getOrders(@RequestParam final String accountId) {
        return ordersService.getOrders(accountId);
    }

}