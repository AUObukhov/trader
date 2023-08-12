package ru.obukhov.trader.market.model.transform;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import ru.tinkoff.piapi.contract.v1.Account;

import java.io.IOException;

public class AccountSerializer extends JsonSerializer<Account> {

    @Override
    public Class<Account> handledType() {
        return Account.class;
    }

    @Override
    public void serialize(final Account value, final JsonGenerator jgen, final SerializerProvider provider)
            throws IOException {

        jgen.writeStartObject();

        jgen.writeStringField("id", value.getId());
        jgen.writeObjectField("type", value.getType());
        jgen.writeStringField("name", value.getName());
        jgen.writeObjectField("status", value.getStatus());
        jgen.writeObjectField("openedDate", value.getOpenedDate());
        jgen.writeObjectField("closedDate", value.getClosedDate());
        jgen.writeObjectField("accessLevel", value.getAccessLevel());

        jgen.writeEndObject();
    }

}