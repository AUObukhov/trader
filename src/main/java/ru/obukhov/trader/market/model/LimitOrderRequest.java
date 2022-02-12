package ru.obukhov.trader.market.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Objects;

public class LimitOrderRequest {
    @JsonProperty("lots")
    private Integer lots = null;

    @JsonProperty("operation")
    private OperationType operation = null;

    @JsonProperty("price")
    private java.math.BigDecimal price = null;

    public LimitOrderRequest lots(Integer lots) {
        this.lots = lots;
        return this;
    }

    /**
     * Get lots
     *
     * @return lots
     **/
    @Schema(required = true, description = "")
    public Integer getLots() {
        return lots;
    }

    public void setLots(Integer lots) {
        this.lots = lots;
    }

    public LimitOrderRequest operation(OperationType operation) {
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

    public LimitOrderRequest price(java.math.BigDecimal price) {
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
        LimitOrderRequest limitOrderRequest = (LimitOrderRequest) o;
        return Objects.equals(this.lots, limitOrderRequest.lots) &&
                Objects.equals(this.operation, limitOrderRequest.operation) &&
                Objects.equals(this.price, limitOrderRequest.price);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lots, operation, price);
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class LimitOrderRequest {\n");

        sb.append("    lots: ").append(toIndentedString(lots)).append("\n");
        sb.append("    operation: ").append(toIndentedString(operation)).append("\n");
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
