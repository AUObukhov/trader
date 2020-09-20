package ru.obukhov.investor.web.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.tinkoff.invest.openapi.models.orders.PlacedOrder;

@Data
@AllArgsConstructor
public class PlaceOrderResponse {
    private PlacedOrder placedOrder;
}