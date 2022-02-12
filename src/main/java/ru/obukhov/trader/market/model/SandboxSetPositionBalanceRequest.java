package ru.obukhov.trader.market.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Objects;

public class SandboxSetPositionBalanceRequest {
    @JsonProperty("figi")
    private String figi = null;

    @JsonProperty("balance")
    private java.math.BigDecimal balance = null;

    public SandboxSetPositionBalanceRequest figi(String figi) {
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

    public SandboxSetPositionBalanceRequest balance(java.math.BigDecimal balance) {
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


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SandboxSetPositionBalanceRequest sandboxSetPositionBalanceRequest = (SandboxSetPositionBalanceRequest) o;
        return Objects.equals(this.figi, sandboxSetPositionBalanceRequest.figi) &&
                Objects.equals(this.balance, sandboxSetPositionBalanceRequest.balance);
    }

    @Override
    public int hashCode() {
        return Objects.hash(figi, balance);
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class SandboxSetPositionBalanceRequest {\n");

        sb.append("    figi: ").append(toIndentedString(figi)).append("\n");
        sb.append("    balance: ").append(toIndentedString(balance)).append("\n");
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
