package ru.obukhov.trader.market.model.transform;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import ru.tinkoff.piapi.contract.v1.Bond;

import java.io.IOException;

public class BondSerializer extends JsonSerializer<Bond> {

    @Override
    public Class<Bond> handledType() {
        return Bond.class;
    }

    @Override
    public void serialize(final Bond value, final JsonGenerator jgen, final SerializerProvider provider)
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
        jgen.writeNumberField("couponQuantityPerYear", value.getCouponQuantityPerYear());
        jgen.writeObjectField("maturityDate", value.getMaturityDate());
        jgen.writeObjectField("nominal", value.getNominal());
        jgen.writeObjectField("initialNominal", value.getInitialNominal());
        jgen.writeObjectField("stateRegDate", value.getStateRegDate());
        jgen.writeObjectField("placementDate", value.getPlacementDate());
        jgen.writeObjectField("placementPrice", value.getPlacementPrice());
        jgen.writeObjectField("aciValue", value.getAciValue());
        jgen.writeStringField("countryOfRisk", value.getCountryOfRisk());
        jgen.writeStringField("countryOfRiskName", value.getCountryOfRiskName());
        jgen.writeStringField("sector", value.getSector());
        jgen.writeStringField("issueKind", value.getIssueKind());
        jgen.writeNumberField("issueSize", value.getIssueSize());
        jgen.writeNumberField("issueSizePlan", value.getIssueSizePlan());
        jgen.writeObjectField("tradingStatus", value.getTradingStatus());
        jgen.writeObjectField("otcFlag", value.getOtcFlag());
        jgen.writeBooleanField("buyAvailableFlag", value.getBuyAvailableFlag());
        jgen.writeBooleanField("sellAvailableFlag", value.getSellAvailableFlag());
        jgen.writeBooleanField("floatingCouponFlag", value.getFloatingCouponFlag());
        jgen.writeBooleanField("perpetualFlag", value.getPerpetualFlag());
        jgen.writeBooleanField("amortizationFlag", value.getAmortizationFlag());
        jgen.writeObjectField("minPriceIncrement", value.getMinPriceIncrement());
        jgen.writeBooleanField("apiTradeAvailableFlag", value.getApiTradeAvailableFlag());
        jgen.writeStringField("uid", value.getUid());
        jgen.writeObjectField("realExchange", value.getRealExchange());
        jgen.writeStringField("positionUid", value.getPositionUid());
        jgen.writeBooleanField("forIisFlag", value.getForIisFlag());
        jgen.writeBooleanField("forQualInvestorFlag", value.getForQualInvestorFlag());
        jgen.writeBooleanField("weekendFlag", value.getWeekendFlag());
        jgen.writeBooleanField("blockedTcaFlag", value.getBlockedTcaFlag());
        jgen.writeBooleanField("subordinatedFlag", value.getSubordinatedFlag());
        jgen.writeObjectField("first1MinCandleDate", value.getFirst1MinCandleDate());
        jgen.writeObjectField("first1DayCandleDate", value.getFirst1DayCandleDate());
        jgen.writeObjectField("riskLevel", value.getRiskLevel());

        jgen.writeEndObject();
    }

}