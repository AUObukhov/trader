package ru.obukhov.trader.web.client.exchange;

import lombok.Data;
import ru.obukhov.trader.market.model.Operations;

@Data
public class OperationsResponse {

    private String trackingId;

    private String status;

    private Operations payload;

}