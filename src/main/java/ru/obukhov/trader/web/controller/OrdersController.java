package ru.obukhov.trader.web.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.obukhov.trader.market.impl.OrdersService;
import ru.obukhov.trader.market.model.Order;
import ru.obukhov.trader.web.model.exchange.GetOrdersResponse;

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
    @ApiOperation("Get active orders at default broker account")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 500, message = "Internal Server Error")
    })
    public GetOrdersResponse getOrders(
            @RequestParam(required = false)
            @ApiParam(name = "brokerAccountId. When null then default account used", example = "2008941383") final String brokerAccountId
    ) {
        final List<Order> orders = ordersService.getOrders(brokerAccountId);

        return new GetOrdersResponse(orders);
    }

}