package ru.obukhov.trader.trading.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import ru.tinkoff.invest.openapi.model.rest.OperationType;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@Builder
@AllArgsConstructor
public class BackTestOperation {

    @JsonIgnore
    private String ticker;

    private OffsetDateTime dateTime;

    private OperationType operationType;

    private BigDecimal price;

    private Integer quantity;

    private BigDecimal commission;

}