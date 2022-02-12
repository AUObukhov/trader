package ru.obukhov.trader.market.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum OperationTypeWithCommission {
  BUY("Buy"),
  BUYCARD("BuyCard"),
  SELL("Sell"),
  BROKERCOMMISSION("BrokerCommission"),
  EXCHANGECOMMISSION("ExchangeCommission"),
  SERVICECOMMISSION("ServiceCommission"),
  MARGINCOMMISSION("MarginCommission"),
  OTHERCOMMISSION("OtherCommission"),
  PAYIN("PayIn"),
  PAYOUT("PayOut"),
  TAX("Tax"),
  TAXLUCRE("TaxLucre"),
  TAXDIVIDEND("TaxDividend"),
  TAXCOUPON("TaxCoupon"),
  TAXBACK("TaxBack"),
  REPAYMENT("Repayment"),
  PARTREPAYMENT("PartRepayment"),
  COUPON("Coupon"),
  DIVIDEND("Dividend"),
  SECURITYIN("SecurityIn"),
  SECURITYOUT("SecurityOut");

  private String value;

  OperationTypeWithCommission(String value) {
    this.value = value;
  }

  @JsonValue
  public String getValue() {
    return value;
  }

  @Override
  public String toString() {
    return String.valueOf(value);
  }

  @JsonCreator
  public static OperationTypeWithCommission fromValue(String text) {
    for (OperationTypeWithCommission b : OperationTypeWithCommission.values()) {
      if (String.valueOf(b.value).equals(text)) {
        return b;
      }
    }
    return null;
  }
}
