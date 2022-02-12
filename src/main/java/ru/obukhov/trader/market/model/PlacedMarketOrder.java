
package ru.obukhov.trader.market.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Objects;

public class PlacedMarketOrder {
    @JsonProperty("orderId")
    private String orderId = null;

    @JsonProperty("operation")
    private OperationType operation = null;

    @JsonProperty("status")
    private OrderStatus status = null;

    @JsonProperty("rejectReason")
    private String rejectReason = null;

    @JsonProperty("message")
    private String message = null;

    @JsonProperty("requestedLots")
    private Integer requestedLots = null;

    @JsonProperty("executedLots")
    private Integer executedLots = null;

    @JsonProperty("commission")
    private MoneyAmount commission = null;

    public PlacedMarketOrder orderId(String orderId) {
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

    public PlacedMarketOrder operation(OperationType operation) {
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

    public PlacedMarketOrder status(OrderStatus status) {
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

    public PlacedMarketOrder rejectReason(String rejectReason) {
        this.rejectReason = rejectReason;
        return this;
    }

    /**
     * Get rejectReason
     *
     * @return rejectReason
     **/
    @Schema(description = "")
    public String getRejectReason() {
        return rejectReason;
    }

    public void setRejectReason(String rejectReason) {
        this.rejectReason = rejectReason;
    }

    public PlacedMarketOrder message(String message) {
        this.message = message;
        return this;
    }

    /**
     * Сообщение об ошибке
     *
     * @return message
     **/
    @Schema(description = "Сообщение об ошибке")
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public PlacedMarketOrder requestedLots(Integer requestedLots) {
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

    public PlacedMarketOrder executedLots(Integer executedLots) {
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

    public PlacedMarketOrder commission(MoneyAmount commission) {
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


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PlacedMarketOrder placedMarketOrder = (PlacedMarketOrder) o;
        return Objects.equals(this.orderId, placedMarketOrder.orderId) &&
                Objects.equals(this.operation, placedMarketOrder.operation) &&
                Objects.equals(this.status, placedMarketOrder.status) &&
                Objects.equals(this.rejectReason, placedMarketOrder.rejectReason) &&
                Objects.equals(this.message, placedMarketOrder.message) &&
                Objects.equals(this.requestedLots, placedMarketOrder.requestedLots) &&
                Objects.equals(this.executedLots, placedMarketOrder.executedLots) &&
                Objects.equals(this.commission, placedMarketOrder.commission);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderId, operation, status, rejectReason, message, requestedLots, executedLots, commission);
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class PlacedMarketOrder {\n");

        sb.append("    orderId: ").append(toIndentedString(orderId)).append("\n");
        sb.append("    operation: ").append(toIndentedString(operation)).append("\n");
        sb.append("    status: ").append(toIndentedString(status)).append("\n");
        sb.append("    rejectReason: ").append(toIndentedString(rejectReason)).append("\n");
        sb.append("    message: ").append(toIndentedString(message)).append("\n");
        sb.append("    requestedLots: ").append(toIndentedString(requestedLots)).append("\n");
        sb.append("    executedLots: ").append(toIndentedString(executedLots)).append("\n");
        sb.append("    commission: ").append(toIndentedString(commission)).append("\n");
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
