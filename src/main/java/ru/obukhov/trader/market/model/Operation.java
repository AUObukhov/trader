package ru.obukhov.trader.market.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Operation {

    @JsonProperty("id")
    private String id = null;

    @JsonProperty("status")
    private OperationStatus status = null;

    @JsonProperty("trades")
    private List<OperationTrade> trades = null;

    @JsonProperty("commission")
    private MoneyAmount commission = null;

    @JsonProperty("currency")
    private Currency currency = null;

    @JsonProperty("payment")
    private java.math.BigDecimal payment = null;

    @JsonProperty("price")
    private java.math.BigDecimal price = null;

    @JsonProperty("quantity")
    private Integer quantity = null;

    @JsonProperty("quantityExecuted")
    private Integer quantityExecuted = null;

    @JsonProperty("figi")
    private String figi = null;

    @JsonProperty("instrumentType")
    private InstrumentType instrumentType = null;

    @JsonProperty("isMarginCall")
    private Boolean isMarginCall = null;

    @JsonProperty("date")
    private OffsetDateTime date = null;

    @JsonProperty("operationType")
    private OperationTypeWithCommission operationType = null;

    public Operation id(String id) {
        this.id = id;
        return this;
    }

    /**
     * Get id
     *
     * @return id
     **/
    @Schema(required = true, description = "")
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Operation status(OperationStatus status) {
        this.status = status;
        return this;
    }

    /**
     * Get status
     *
     * @return status
     **/
    @Schema(required = true, description = "")
    public OperationStatus getStatus() {
        return status;
    }

    public void setStatus(OperationStatus status) {
        this.status = status;
    }

    public Operation trades(List<OperationTrade> trades) {
        this.trades = trades;
        return this;
    }

    public Operation addTradesItem(OperationTrade tradesItem) {
        if (this.trades == null) {
            this.trades = new ArrayList<>();
        }
        this.trades.add(tradesItem);
        return this;
    }

    /**
     * Get trades
     *
     * @return trades
     **/
    @Schema(description = "")
    public List<OperationTrade> getTrades() {
        return trades;
    }

    public void setTrades(List<OperationTrade> trades) {
        this.trades = trades;
    }

    public Operation commission(MoneyAmount commission) {
        this.commission = commission;
        return this;
    }

    /**
     * Get commission
     *
     * @return commission
     **/
    @Schema(description = "")
    public MoneyAmount getCommission() {
        return commission;
    }

    public void setCommission(MoneyAmount commission) {
        this.commission = commission;
    }

    public Operation currency(Currency currency) {
        this.currency = currency;
        return this;
    }

    /**
     * Get currency
     *
     * @return currency
     **/
    @Schema(required = true, description = "")
    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public Operation payment(java.math.BigDecimal payment) {
        this.payment = payment;
        return this;
    }

    /**
     * Get payment
     *
     * @return payment
     **/
    @Schema(required = true, description = "")
    public java.math.BigDecimal getPayment() {
        return payment;
    }

    public void setPayment(java.math.BigDecimal payment) {
        this.payment = payment;
    }

    public Operation price(java.math.BigDecimal price) {
        this.price = price;
        return this;
    }

    /**
     * Get price
     *
     * @return price
     **/
    @Schema(description = "")
    public java.math.BigDecimal getPrice() {
        return price;
    }

    public void setPrice(java.math.BigDecimal price) {
        this.price = price;
    }

    public Operation quantity(Integer quantity) {
        this.quantity = quantity;
        return this;
    }

    /**
     * Число инструментов в выставленной заявке
     *
     * @return quantity
     **/
    @Schema(description = "Число инструментов в выставленной заявке")
    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Operation quantityExecuted(Integer quantityExecuted) {
        this.quantityExecuted = quantityExecuted;
        return this;
    }

    /**
     * Число инструментов, исполненных в заявке
     *
     * @return quantityExecuted
     **/
    @Schema(description = "Число инструментов, исполненных в заявке")
    public Integer getQuantityExecuted() {
        return quantityExecuted;
    }

    public void setQuantityExecuted(Integer quantityExecuted) {
        this.quantityExecuted = quantityExecuted;
    }

    public Operation figi(String figi) {
        this.figi = figi;
        return this;
    }

    /**
     * Get figi
     *
     * @return figi
     **/
    @Schema(description = "")
    public String getFigi() {
        return figi;
    }

    public void setFigi(String figi) {
        this.figi = figi;
    }

    public Operation instrumentType(InstrumentType instrumentType) {
        this.instrumentType = instrumentType;
        return this;
    }

    /**
     * Get instrumentType
     *
     * @return instrumentType
     **/
    @Schema(description = "")
    public InstrumentType getInstrumentType() {
        return instrumentType;
    }

    public void setInstrumentType(InstrumentType instrumentType) {
        this.instrumentType = instrumentType;
    }

    public Operation isMarginCall(Boolean isMarginCall) {
        this.isMarginCall = isMarginCall;
        return this;
    }

    /**
     * Get isMarginCall
     *
     * @return isMarginCall
     **/
    @Schema(required = true, description = "")
    public Boolean isIsMarginCall() {
        return isMarginCall;
    }

    public void setIsMarginCall(Boolean isMarginCall) {
        this.isMarginCall = isMarginCall;
    }

    public Operation date(OffsetDateTime date) {
        this.date = date;
        return this;
    }

    /**
     * ISO8601
     *
     * @return date
     **/
    @Schema(example = "2019-08-19T18:38:33+03:00", required = true, description = "ISO8601")
    public OffsetDateTime getDate() {
        return date;
    }

    public void setDate(OffsetDateTime date) {
        this.date = date;
    }

    public Operation operationType(OperationTypeWithCommission operationType) {
        this.operationType = operationType;
        return this;
    }

    /**
     * Get operationType
     *
     * @return operationType
     **/
    @Schema(description = "")
    public OperationTypeWithCommission getOperationType() {
        return operationType;
    }

    public void setOperationType(OperationTypeWithCommission operationType) {
        this.operationType = operationType;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Operation operation = (Operation) o;
        return Objects.equals(this.id, operation.id) &&
                Objects.equals(this.status, operation.status) &&
                Objects.equals(this.trades, operation.trades) &&
                Objects.equals(this.commission, operation.commission) &&
                Objects.equals(this.currency, operation.currency) &&
                Objects.equals(this.payment, operation.payment) &&
                Objects.equals(this.price, operation.price) &&
                Objects.equals(this.quantity, operation.quantity) &&
                Objects.equals(this.quantityExecuted, operation.quantityExecuted) &&
                Objects.equals(this.figi, operation.figi) &&
                Objects.equals(this.instrumentType, operation.instrumentType) &&
                Objects.equals(this.isMarginCall, operation.isMarginCall) &&
                Objects.equals(this.date, operation.date) &&
                Objects.equals(this.operationType, operation.operationType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, status, trades, commission, currency, payment, price, quantity, quantityExecuted, figi, instrumentType, isMarginCall, date, operationType);
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class Operation {\n");

        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    status: ").append(toIndentedString(status)).append("\n");
        sb.append("    trades: ").append(toIndentedString(trades)).append("\n");
        sb.append("    commission: ").append(toIndentedString(commission)).append("\n");
        sb.append("    currency: ").append(toIndentedString(currency)).append("\n");
        sb.append("    payment: ").append(toIndentedString(payment)).append("\n");
        sb.append("    price: ").append(toIndentedString(price)).append("\n");
        sb.append("    quantity: ").append(toIndentedString(quantity)).append("\n");
        sb.append("    quantityExecuted: ").append(toIndentedString(quantityExecuted)).append("\n");
        sb.append("    figi: ").append(toIndentedString(figi)).append("\n");
        sb.append("    instrumentType: ").append(toIndentedString(instrumentType)).append("\n");
        sb.append("    isMarginCall: ").append(toIndentedString(isMarginCall)).append("\n");
        sb.append("    date: ").append(toIndentedString(date)).append("\n");
        sb.append("    operationType: ").append(toIndentedString(operationType)).append("\n");
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
