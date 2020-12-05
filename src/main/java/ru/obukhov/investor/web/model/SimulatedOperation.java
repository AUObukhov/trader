package ru.obukhov.investor.web.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Data;
import ru.tinkoff.invest.openapi.models.operations.OperationType;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@Builder
public class SimulatedOperation {

    @JsonIgnore
    private String ticker;

    private OffsetDateTime dateTime;

    private OperationType operationType;

    private BigDecimal price;

    private Integer quantity;

    private BigDecimal commission;

}