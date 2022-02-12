package ru.obukhov.trader.market.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Objects;

public class Order {
    @JsonProperty("orderId")
    private String orderId = null;

    @JsonProperty("figi")
    private String figi = null;

    @JsonProperty("operation")
    private OperationType operation = null;

    @JsonProperty("status")
    private OrderStatus status = null;

    @JsonProperty("requestedLots")
    private Integer requestedLots = null;

    @JsonProperty("executedLots")
    private Integer executedLots = null;

    @JsonProperty("type")
    private OrderType type = null;

    @JsonProperty("price")
    private java.math.BigDecimal price = null;

    public Order orderId(String orderId) {
        this.orderId = orderId;
        return this;
    }

    /**
     * Get orderId
     *
     * @return orderId
     **/
    @Schema(required = true, description = "")
    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public Order figi(String figi) {
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

    public Order operation(OperationType operation) {
        this.operation = operation;
        return this;
    }

    /**
     * Get operation
     *
     * @return operation
     **/
    @Schema(required = true, description = "")
    public OperationType getOperation() {
        return operation;
    }

    public void setOperation(OperationType operation) {
        this.operation = operation;
    }

    public Order status(OrderStatus status) {
        this.status = status;
        return this;
    }

    /**
     * Get status
     *
     * @return status
     **/
    @Schema(required = true, description = "")
    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public Order requestedLots(Integer requestedLots) {
        this.requestedLots = requestedLots;
        return this;
    }

    /**
     * Get requestedLots
     *
     * @return requestedLots
     **/
    @Schema(required = true, description = "")
    public Integer getRequestedLots() {
        return requestedLots;
    }

    public void setRequestedLots(Integer requestedLots) {
        this.requestedLots = requestedLots;
    }

    public Order executedLots(Integer executedLots) {
        this.executedLots = executedLots;
        return this;
    }

    /**
     * Get executedLots
     *
     * @return executedLots
     **/
    @Schema(required = true, description = "")
    public Integer getExecutedLots() {
        return executedLots;
    }

    public void setExecutedLots(Integer executedLots) {
        this.executedLots = executedLots;
    }

    public Order type(OrderType type) {
        this.type = type;
        return this;
    }

    /**
     * Get type
     *
     * @return type
     **/
    @Schema(required = true, description = "")
    public OrderType getType() {
        return type;
    }

    public void setType(OrderType type) {
        this.type = type;
    }

    public Order price(java.math.BigDecimal price) {
        this.price = price;
        return this;
    }

    /**
     * Get price
     *
     * @return price
     **/
    @Schema(required = true, description = "")
    public java.math.BigDecimal getPrice() {
        return price;
    }

    public void setPrice(java.math.BigDecimal price) {
        this.price = price;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Order order = (Order) o;
        return Objects.equals(this.orderId, order.orderId) &&
                Objects.equals(this.figi, order.figi) &&
                Objects.equals(this.operation, order.operation) &&
                Objects.equals(this.status, order.status) &&
                Objects.equals(this.requestedLots, order.requestedLots) &&
                Objects.equals(this.executedLots, order.executedLots) &&
                Objects.equals(this.type, order.type) &&
                Objects.equals(this.price, order.price);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderId, figi, operation, status, requestedLots, executedLots, type, price);
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class Order {\n");

        sb.append("    orderId: ").append(toIndentedString(orderId)).append("\n");
        sb.append("    figi: ").append(toIndentedString(figi)).append("\n");
        sb.append("    operation: ").append(toIndentedString(operation)).append("\n");
        sb.append("    status: ").append(toIndentedString(status)).append("\n");
        sb.append("    requestedLots: ").append(toIndentedString(requestedLots)).append("\n");
        sb.append("    executedLots: ").append(toIndentedString(executedLots)).append("\n");
        sb.append("    type: ").append(toIndentedString(type)).append("\n");
        sb.append("    price: ").append(toIndentedString(price)).append("\n");
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
