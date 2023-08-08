package ru.obukhov.trader.market.model.transform;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import ru.tinkoff.piapi.contract.v1.Share;

import java.io.IOException;

public class ShareSerializer extends JsonSerializer<Share> {

    @Override
    public Class<Share> handledType() {
        return Share.class;
    }

    @Override
    public void serialize(final Share value, final JsonGenerator jgen, final SerializerProvider provider)
            throws IOException {

        jgen.writeStartObject();

        jgen.writeStringField("figi", value.getFigi());
        jgen.writeStringField("ticker", value.getTicker());
        jgen.writeStringField("classCode", value.getClassCode());
        jgen.writeStringField("isin", value.getIsin());
        jgen.writeNumberField("lot", value.getLot());
        jgen.writeStringField("currency", value.getCurrency());
        jgen.writeObjectField("klong", value.getKlong());
        jgen.writeObjectField("kshort", value.getKshort());
        jgen.writeObjectField("dlong", value.getDlong());
        jgen.writeObjectField("dshort", value.getDshort());
        jgen.writeObjectField("dlongMin", value.getDlongMin());
        jgen.writeObjectField("dshortMin", value.getDshortMin());
        jgen.writeBooleanField("shortEnabledFlag", value.getShortEnabledFlag());
        jgen.writeStringField("name", value.getName());
        jgen.writeStringField("exchange", value.getExchange());
        jgen.writeObjectField("ipoDate", value.getIpoDate());
        jgen.writeNumberField("issueSize", value.getIssueSize());
        jgen.writeStringField("countryOfRisk", value.getCountryOfRisk());
        jgen.writeStringField("countryOfRiskName", value.getCountryOfRiskName());
        jgen.writeStringField("sector", value.getSector());
        jgen.writeNumberField("issueSizePlan", value.getIssueSizePlan());
        jgen.writeObjectField("nominal", value.getNominal());
        jgen.writeObjectField("tradingStatus", value.getTradingStatus());
        jgen.writeObjectField("otcFlag", value.getOtcFlag());
        jgen.writeBooleanField("buyAvailableFlag", value.getBuyAvailableFlag());
        jgen.writeBooleanField("sellAvailableFlag", value.getSellAvailableFlag());
        jgen.writeBooleanField("divYieldFlag", value.getDivYieldFlag());
        jgen.writeObjectField("shareType", value.getShareType());
        jgen.writeObjectField("minPriceIncrement", value.getMinPriceIncrement());
        jgen.writeBooleanField("apiTradeAvailableFlag", value.getApiTradeAvailableFlag());
        jgen.writeStringField("uid", value.getUid());
        jgen.writeObjectField("realExchange", value.getRealExchange());
        jgen.writeStringField("positionUid", value.getPositionUid());
        jgen.writeBooleanField("forIisFlag", value.getForIisFlag());
        jgen.writeBooleanField("forQualInvestorFlag", value.getForQualInvestorFlag());
        jgen.writeBooleanField("weekendFlag", value.getWeekendFlag());
        jgen.writeBooleanField("blockedTcaFlag", value.getBlockedTcaFlag());
        jgen.writeObjectField("first1MinCandleDate", value.getFirst1MinCandleDate());
        jgen.writeObjectField("first1DayCandleDate", value.getFirst1DayCandleDate());

        jgen.writeEndObject();
    }

}