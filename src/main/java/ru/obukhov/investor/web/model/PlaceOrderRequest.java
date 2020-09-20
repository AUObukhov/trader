package ru.obukhov.investor.web.model;

import lombok.Data;
import ru.tinkoff.invest.openapi.models.orders.Operation;

import javax.annotation.Nullable;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class PlaceOrderRequest {

    @NotBlank(message = "ticker is mandatory")
    private String ticker;

    private int lots;

    @NotNull(message = "Operation is mandatory")
    private Operation operation;

    @Nullable
    private BigDecimal price;
}