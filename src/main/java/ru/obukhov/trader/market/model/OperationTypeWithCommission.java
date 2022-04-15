package ru.obukhov.trader.market.model;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor
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

  private static final Map<String, OperationTypeWithCommission> LOOKUP = Stream.of(OperationTypeWithCommission.values())
          .collect(Collectors.toMap(OperationTypeWithCommission::getValue, operationTypeWithCommission -> operationTypeWithCommission));

  @Getter
  @JsonValue
  private final String value;

  @Override
  public String toString() {
    return value;
  }

  public static OperationTypeWithCommission fromValue(String text) {
    return LOOKUP.get(text);
  }

}