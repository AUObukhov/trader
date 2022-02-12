package ru.obukhov.trader.market.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Orderbook {
    @JsonProperty("figi")
    private String figi = null;

    @JsonProperty("depth")
    private Integer depth = null;

    @JsonProperty("bids")
    private List<OrderResponse> bids = new ArrayList<>();

    @JsonProperty("asks")
    private List<OrderResponse> asks = new ArrayList<>();

    @JsonProperty("tradeStatus")
    private TradeStatus tradeStatus = null;

    @JsonProperty("minPriceIncrement")
    private java.math.BigDecimal minPriceIncrement = null;

    @JsonProperty("faceValue")
    private java.math.BigDecimal faceValue = null;

    @JsonProperty("lastPrice")
    private java.math.BigDecimal lastPrice = null;

    @JsonProperty("closePrice")
    private java.math.BigDecimal closePrice = null;

    @JsonProperty("limitUp")
    private java.math.BigDecimal limitUp = null;

    @JsonProperty("limitDown")
    private java.math.BigDecimal limitDown = null;

    public Orderbook figi(String figi) {
        this.figi = figi;
        return this;
    }

    /**
     * Get figi
     *
     * @return figi
     **/
    @Schema(required = true, description = "")
    public String getFigi() {
        return figi;
    }

    public void setFigi(String figi) {
        this.figi = figi;
    }

    public Orderbook depth(Integer depth) {
        this.depth = depth;
        return this;
    }

    /**
     * Get depth
     *
     * @return depth
     **/
    @Schema(required = true, description = "")
    public Integer getDepth() {
        return depth;
    }

    public void setDepth(Integer depth) {
        this.depth = depth;
    }

    public Orderbook bids(List<OrderResponse> bids) {
        this.bids = bids;
        return this;
    }

    public Orderbook addBidsItem(OrderResponse bidsItem) {
        this.bids.add(bidsItem);
        return this;
    }

    /**
     * Get bids
     *
     * @return bids
     **/
    @Schema(required = true, description = "")
    public List<OrderResponse> getBids() {
        return bids;
    }

    public void setBids(List<OrderResponse> bids) {
        this.bids = bids;
    }

    public Orderbook asks(List<OrderResponse> asks) {
        this.asks = asks;
        return this;
    }

    public Orderbook addAsksItem(OrderResponse asksItem) {
        this.asks.add(asksItem);
        return this;
    }

    /**
     * Get asks
     *
     * @return asks
     **/
    @Schema(required = true, description = "")
    public List<OrderResponse> getAsks() {
        return asks;
    }

    public void setAsks(List<OrderResponse> asks) {
        this.asks = asks;
    }

    public Orderbook tradeStatus(TradeStatus tradeStatus) {
        this.tradeStatus = tradeStatus;
        return this;
    }

    /**
     * Get tradeStatus
     *
     * @return tradeStatus
     **/
    @Schema(required = true, description = "")
    public TradeStatus getTradeStatus() {
        return tradeStatus;
    }

    public void setTradeStatus(TradeStatus tradeStatus) {
        this.tradeStatus = tradeStatus;
    }

    public Orderbook minPriceIncrement(java.math.BigDecimal minPriceIncrement) {
        this.minPriceIncrement = minPriceIncrement;
        return this;
    }

    /**
     * Шаг цены
     *
     * @return minPriceIncrement
     **/
    @Schema(required = true, description = "Шаг цены")
    public java.math.BigDecimal getMinPriceIncrement() {
        return minPriceIncrement;
    }

    public void setMinPriceIncrement(java.math.BigDecimal minPriceIncrement) {
        this.minPriceIncrement = minPriceIncrement;
    }

    public Orderbook faceValue(java.math.BigDecimal faceValue) {
        this.faceValue = faceValue;
        return this;
    }

    /**
     * Номинал для облигаций
     *
     * @return faceValue
     **/
    @Schema(description = "Номинал для облигаций")
    public java.math.BigDecimal getFaceValue() {
        return faceValue;
    }

    public void setFaceValue(java.math.BigDecimal faceValue) {
        this.faceValue = faceValue;
    }

    public Orderbook lastPrice(java.math.BigDecimal lastPrice) {
        this.lastPrice = lastPrice;
        return this;
    }

    /**
     * Get lastPrice
     *
     * @return lastPrice
     **/
    @Schema(description = "")
    public java.math.BigDecimal getLastPrice() {
        return lastPrice;
    }

    public void setLastPrice(java.math.BigDecimal lastPrice) {
        this.lastPrice = lastPrice;
    }

    public Orderbook closePrice(java.math.BigDecimal closePrice) {
        this.closePrice = closePrice;
        return this;
    }

    /**
     * Get closePrice
     *
     * @return closePrice
     **/
    @Schema(description = "")
    public java.math.BigDecimal getClosePrice() {
        return closePrice;
    }

    public void setClosePrice(java.math.BigDecimal closePrice) {
        this.closePrice = closePrice;
    }

    public Orderbook limitUp(java.math.BigDecimal limitUp) {
        this.limitUp = limitUp;
        return this;
    }

    /**
     * Верхняя граница цены
     *
     * @return limitUp
     **/
    @Schema(description = "Верхняя граница цены")
    public java.math.BigDecimal getLimitUp() {
        return limitUp;
    }

    public void setLimitUp(java.math.BigDecimal limitUp) {
        this.limitUp = limitUp;
    }

    public Orderbook limitDown(java.math.BigDecimal limitDown) {
        this.limitDown = limitDown;
        return this;
    }

    /**
     * Нижняя граница цены
     *
     * @return limitDown
     **/
    @Schema(description = "Нижняя граница цены")
    public java.math.BigDecimal getLimitDown() {
        return limitDown;
    }

    public void setLimitDown(java.math.BigDecimal limitDown) {
        this.limitDown = limitDown;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Orderbook orderbook = (Orderbook) o;
        return Objects.equals(this.figi, orderbook.figi) &&
                Objects.equals(this.depth, orderbook.depth) &&
                Objects.equals(this.bids, orderbook.bids) &&
                Objects.equals(this.asks, orderbook.asks) &&
                Objects.equals(this.tradeStatus, orderbook.tradeStatus) &&
                Objects.equals(this.minPriceIncrement, orderbook.minPriceIncrement) &&
                Objects.equals(this.faceValue, orderbook.faceValue) &&
                Objects.equals(this.lastPrice, orderbook.lastPrice) &&
                Objects.equals(this.closePrice, orderbook.closePrice) &&
                Objects.equals(this.limitUp, orderbook.limitUp) &&
                Objects.equals(this.limitDown, orderbook.limitDown);
    }

    @Override
    public int hashCode() {
        return Objects.hash(figi, depth, bids, asks, tradeStatus, minPriceIncrement, faceValue, lastPrice, closePrice, limitUp, limitDown);
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class Orderbook {\n");

        sb.append("    figi: ").append(toIndentedString(figi)).append("\n");
        sb.append("    depth: ").append(toIndentedString(depth)).append("\n");
        sb.append("    bids: ").append(toIndentedString(bids)).append("\n");
        sb.append("    asks: ").append(toIndentedString(asks)).append("\n");
        sb.append("    tradeStatus: ").append(toIndentedString(tradeStatus)).append("\n");
        sb.append("    minPriceIncrement: ").append(toIndentedString(minPriceIncrement)).append("\n");
        sb.append("    faceValue: ").append(toIndentedString(faceValue)).append("\n");
        sb.append("    lastPrice: ").append(toIndentedString(lastPrice)).append("\n");
        sb.append("    closePrice: ").append(toIndentedString(closePrice)).append("\n");
        sb.append("    limitUp: ").append(toIndentedString(limitUp)).append("\n");
        sb.append("    limitDown: ").append(toIndentedString(limitDown)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }

}
