package ru.obukhov.trader.market.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.Objects;

public class SandboxSetCurrencyBalanceRequest {
  @JsonProperty("currency")
  private Currency currency = null;

  @JsonProperty("balance")
  private java.math.BigDecimal balance = null;

  public SandboxSetCurrencyBalanceRequest currency(Currency currency) {
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

  public SandboxSetCurrencyBalanceRequest balance(BigDecimal balance) {
    this.balance = balance;
    return this;
  }

  /**
   * Get balance
   *
   * @return balance
   **/
  @Schema(required = true, description = "")
  public BigDecimal getBalance() {
    return balance;
  }

  public void setBalance(BigDecimal balance) {
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
    SandboxSetCurrencyBalanceRequest sandboxSetCurrencyBalanceRequest = (SandboxSetCurrencyBalanceRequest) o;
    return Objects.equals(this.currency, sandboxSetCurrencyBalanceRequest.currency) &&
            Objects.equals(this.balance, sandboxSetCurrencyBalanceRequest.balance);
  }

  @Override
  public int hashCode() {
    return Objects.hash(currency, balance);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SandboxSetCurrencyBalanceRequest {\n");

    sb.append("    currency: ").append(toIndentedString(currency)).append("\n");
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
