package ru.obukhov.investor.service.impl;

import org.jetbrains.annotations.NotNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import ru.obukhov.investor.service.TinkoffContextsAware;
import ru.obukhov.investor.service.interfaces.ConnectionService;
import ru.obukhov.investor.service.interfaces.MarketService;
import ru.obukhov.investor.service.interfaces.OrdersService;
import ru.tinkoff.invest.openapi.models.orders.LimitOrder;
import ru.tinkoff.invest.openapi.models.orders.MarketOrder;
import ru.tinkoff.invest.openapi.models.orders.Operation;
import ru.tinkoff.invest.openapi.models.orders.Order;
import ru.tinkoff.invest.openapi.models.orders.PlacedOrder;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service("ordersServiceImpl")
public class OrdersServiceImpl extends TinkoffContextsAware implements OrdersService {

    private final MarketService marketService;

    public OrdersServiceImpl(ConnectionService connectionService, MarketService marketService) {
        super(connectionService);
        this.marketService = marketService;
    }

    @Override
    public List<Order> getOrders(String ticker) {
        String figi = marketService.getFigi(ticker);
        List<Order> orders = getOrdersContext().getOrders(null).join();
        return orders.stream()
                .filter(order -> figi.equals(order.figi))
                .collect(Collectors.toList());
    }

    @Override
    public List<Order> getOrders() {
        return getOrdersContext().getOrders(null).join();
    }

    @Override
    public PlacedOrder placeOrder(@NotNull String ticker,
                                  int lots,
                                  @NotNull Operation operation,
                                  @Nullable BigDecimal price) {
        String figi = marketService.getFigi(ticker);
        if (price == null) {
            MarketOrder marketOrder = new MarketOrder(lots, operation);
            return getOrdersContext().placeMarketOrder(figi, marketOrder, null).join();
        } else {
            LimitOrder limitOrder = new LimitOrder(lots, operation, price);
            return getOrdersContext().placeLimitOrder(figi, limitOrder, null).join();
        }
    }

    @Override
    public void cancelOrder(@NotNull String orderId) {
        getOrdersContext().cancelOrder(orderId, null).join();
    }
}