package ru.obukhov.investor.web.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.obukhov.investor.service.interfaces.OrdersService;
import ru.obukhov.investor.web.model.GetOrdersResponse;
import ru.obukhov.investor.web.model.PlaceOrderRequest;
import ru.obukhov.investor.web.model.PlaceOrderResponse;
import ru.tinkoff.invest.openapi.models.orders.Order;
import ru.tinkoff.invest.openapi.models.orders.PlacedOrder;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/investor/orders")
@RequiredArgsConstructor
public class OrdersController {

    private final OrdersService ordersService;

    @GetMapping("/get")
    public GetOrdersResponse getOrders() {

        List<Order> orders = ordersService.getOrders();

        return new GetOrdersResponse(orders);

    }

    @GetMapping("/place")
    public PlaceOrderResponse placeOrder(@Valid @RequestBody PlaceOrderRequest request) {

        PlacedOrder placedOrder = ordersService.placeOrder(request.getTicker(),
                request.getLots(),
                request.getOperation(),
                request.getPrice());

        return new PlaceOrderResponse(placedOrder);

    }

}