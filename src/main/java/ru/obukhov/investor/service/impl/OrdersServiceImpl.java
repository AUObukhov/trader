package ru.obukhov.investor.service.impl;

import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import ru.obukhov.investor.service.interfaces.MarketService;
import ru.obukhov.investor.service.interfaces.OrdersService;
import ru.tinkoff.invest.openapi.OrdersContext;
import ru.tinkoff.invest.openapi.models.orders.LimitOrder;
import ru.tinkoff.invest.openapi.models.orders.MarketOrder;
import ru.tinkoff.invest.openapi.models.orders.Operation;
import ru.tinkoff.invest.openapi.models.orders.Order;
import ru.tinkoff.invest.openapi.models.orders.PlacedOrder;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service("ordersServiceImpl")
@AllArgsConstructor
public class OrdersServiceImpl implements OrdersService {

    private final MarketService marketService;
    private final OrdersContext ordersContext;

    @Override
    public List<Order> getOrders(String ticker) {
        String figi = marketService.getFigi(ticker);
        List<Order> orders = ordersContext.getOrders(null).join();
        return orders.stream()
                .filter(order -> figi.equals(order.figi))
                .collect(Collectors.toList());
    }

    @Override
    public List<Order> getOrders() {
        return ordersContext.getOrders(null).join();
    }

    @Override
    public PlacedOrder placeOrder(@NotNull String ticker,
                                  int lots,
                                  @NotNull Operation operation,
                                  @Nullable BigDecimal price) {
        String figi = marketService.getFigi(ticker);
        if (price == null) {
            MarketOrder marketOrder = new MarketOrder(lots, operation);
            return ordersContext.placeMarketOrder(figi, marketOrder, null).join();
        } else {
            LimitOrder limitOrder = new LimitOrder(lots, operation, price);
            return ordersContext.placeLimitOrder(figi, limitOrder, null).join();
        }
    }

    @Override
    public void cancelOrder(@NotNull String orderId) {
        ordersContext.cancelOrder(orderId, null).join();
    }
}