package ru.obukhov.trader.market.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Objects;

public class MarketOrderRequest {
    @JsonProperty("lots")
    private Integer lots = null;

    @JsonProperty("operation")
    private OperationType operation = null;

    public MarketOrderRequest lots(Integer lots) {
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

    public MarketOrderRequest operation(OperationType operation) {
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


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MarketOrderRequest marketOrderRequest = (MarketOrderRequest) o;
        return Objects.equals(this.lots, marketOrderRequest.lots) &&
                Objects.equals(this.operation, marketOrderRequest.operation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lots, operation);
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class MarketOrderRequest {\n");

        sb.append("    lots: ").append(toIndentedString(lots)).append("\n");
        sb.append("    operation: ").append(toIndentedString(operation)).append("\n");
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
