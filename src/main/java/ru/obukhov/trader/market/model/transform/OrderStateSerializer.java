package ru.obukhov.trader.market.model.transform;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import ru.tinkoff.piapi.contract.v1.OrderState;

import java.io.IOException;

public class OrderStateSerializer extends JsonSerializer<OrderState> {

    @Override
    public Class<OrderState> handledType() {
        return OrderState.class;
    }

    @Override
    public void serialize(final OrderState value, final JsonGenerator jgen, final SerializerProvider provider)
            throws IOException {

        jgen.writeStartObject();

        jgen.writeStringField("orderId", value.getOrderId());
        jgen.writeObjectField("executionReportStatus", value.getExecutionReportStatus());
        jgen.writeNumberField("lotsRequested", value.getLotsRequested());
        jgen.writeNumberField("lotsExecuted", value.getLotsExecuted());
        jgen.writeObjectField("initialOrderPrice", value.getInitialOrderPrice());
        jgen.writeObjectField("executedOrderPrice", value.getExecutedOrderPrice());
        jgen.writeObjectField("totalOrderAmount", value.getTotalOrderAmount());
        jgen.writeObjectField("averagePositionPrice", value.getAveragePositionPrice());
        jgen.writeObjectField("initialCommission", value.getInitialCommission());
        jgen.writeObjectField("executedCommission", value.getExecutedCommission());
        jgen.writeStringField("figi", value.getFigi());
        jgen.writeObjectField("direction", value.getDirection());
        jgen.writeObjectField("initialSecurityPrice", value.getInitialSecurityPrice());
        jgen.writeObjectField("stages", value.getStagesList());
        jgen.writeObjectField("serviceCommission", value.getServiceCommission());
        jgen.writeStringField("currency", value.getCurrency());
        jgen.writeObjectField("orderType", value.getOrderType());
        jgen.writeObjectField("orderDate", value.getOrderDate());
        jgen.writeObjectField("instrumentUid", value.getInstrumentUid());

        jgen.writeEndObject();
    }

}