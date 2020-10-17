package ru.obukhov.investor.bot.impl;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.NotImplementedException;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import ru.obukhov.investor.bot.interfaces.MarketMock;
import ru.obukhov.investor.service.interfaces.OrdersService;
import ru.tinkoff.invest.openapi.models.orders.Operation;
import ru.tinkoff.invest.openapi.models.orders.Order;
import ru.tinkoff.invest.openapi.models.orders.PlacedOrder;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

/**
 * Bean for making mocked orders, kept in memory
 */
@Service("ordersServiceMock")
@RequiredArgsConstructor
public class OrdersServiceMock implements OrdersService {

    private final MarketMock marketMock;

    @Override
    public List<Order> getOrders(String ticker) {
        return Collections.emptyList();
    }

    @Override
    public List<Order> getOrders() {
        return Collections.emptyList();
    }

    @Override
    public PlacedOrder placeOrder(@NotNull String ticker, int lots, @NotNull Operation operation, BigDecimal price) {
        marketMock.performOperation(ticker, lots, operation, price);

        return null;
    }


    @Override
    public void cancelOrder(@NotNull String orderId) {
        throw new NotImplementedException();
    }

}