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
import ru.obukhov.trader.market.impl.RealExtOrdersService;
import ru.tinkoff.piapi.contract.v1.OrderState;

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
    @ApiOperation("Get active orders at default broker account")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 400, message = "Bad Request"),
            @ApiResponse(code = 500, message = "Internal Server Error")
    })
    public List<OrderState> getOrders(@RequestParam @ApiParam(example = "2008941383") final String accountId) {
        return ordersService.getOrders(accountId);
    }

}