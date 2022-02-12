package ru.obukhov.trader.market.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Objects;

public class CurrencyPosition {

    @JsonProperty("currency")
    private Currency currency = null;

    @JsonProperty("balance")
    private java.math.BigDecimal balance = null;

    @JsonProperty("blocked")
    private java.math.BigDecimal blocked = null;

    public CurrencyPosition currency(Currency currency) {
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

    public CurrencyPosition balance(java.math.BigDecimal balance) {
        this.balance = balance;
        return this;
    }

    /**
     * Get balance
     *
     * @return balance
     **/
    @Schema(required = true, description = "")
    public java.math.BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(java.math.BigDecimal balance) {
        this.balance = balance;
    }

    public CurrencyPosition blocked(java.math.BigDecimal blocked) {
        this.blocked = blocked;
        return this;
    }

    /**
     * Get blocked
     *
     * @return blocked
     **/
    @Schema(description = "")
    public java.math.BigDecimal getBlocked() {
        return blocked;
    }

    public void setBlocked(java.math.BigDecimal blocked) {
        this.blocked = blocked;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CurrencyPosition currencyPosition = (CurrencyPosition) o;
        return Objects.equals(this.currency, currencyPosition.currency) &&
                Objects.equals(this.balance, currencyPosition.balance) &&
                Objects.equals(this.blocked, currencyPosition.blocked);
    }

    @Override
    public int hashCode() {
        return Objects.hash(currency, balance, blocked);
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class CurrencyPosition {\n");

        sb.append("    currency: ").append(toIndentedString(currency)).append("\n");
        sb.append("    balance: ").append(toIndentedString(balance)).append("\n");
        sb.append("    blocked: ").append(toIndentedString(blocked)).append("\n");
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
