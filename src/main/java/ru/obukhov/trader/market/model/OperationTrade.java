package ru.obukhov.trader.market.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;
import java.util.Objects;

public class OperationTrade {
    @JsonProperty("tradeId")
    private String tradeId = null;

    @JsonProperty("date")
    private OffsetDateTime date = null;

    @JsonProperty("price")
    private java.math.BigDecimal price = null;

    @JsonProperty("quantity")
    private Integer quantity = null;

    public OperationTrade tradeId(String tradeId) {
        this.tradeId = tradeId;
        return this;
    }

    /**
     * Get tradeId
     *
     * @return tradeId
     **/
    @Schema(required = true, description = "")
    public String getTradeId() {
        return tradeId;
    }

    public void setTradeId(String tradeId) {
        this.tradeId = tradeId;
    }

    public OperationTrade date(OffsetDateTime date) {
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

    public OperationTrade price(java.math.BigDecimal price) {
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

    public OperationTrade quantity(Integer quantity) {
        this.quantity = quantity;
        return this;
    }

    /**
     * Get quantity
     *
     * @return quantity
     **/
    @Schema(required = true, description = "")
    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        OperationTrade operationTrade = (OperationTrade) o;
        return Objects.equals(this.tradeId, operationTrade.tradeId) &&
                Objects.equals(this.date, operationTrade.date) &&
                Objects.equals(this.price, operationTrade.price) &&
                Objects.equals(this.quantity, operationTrade.quantity);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tradeId, date, price, quantity);
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class OperationTrade {\n");

        sb.append("    tradeId: ").append(toIndentedString(tradeId)).append("\n");
        sb.append("    date: ").append(toIndentedString(date)).append("\n");
        sb.append("    price: ").append(toIndentedString(price)).append("\n");
        sb.append("    quantity: ").append(toIndentedString(quantity)).append("\n");
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
