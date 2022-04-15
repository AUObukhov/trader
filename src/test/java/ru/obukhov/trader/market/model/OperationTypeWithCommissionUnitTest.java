package ru.obukhov.trader.market.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ru.obukhov.trader.test.utils.TestUtils;

import java.util.stream.Stream;

class OperationTypeWithCommissionUnitTest {

    @SuppressWarnings("unused")
    static Stream<Arguments> valuesAndOperationTypesWithCommission() {
        return Stream.of(
                Arguments.of("Buy", OperationTypeWithCommission.BUY),
                Arguments.of("BuyCard", OperationTypeWithCommission.BUYCARD),
                Arguments.of("Sell", OperationTypeWithCommission.SELL),
                Arguments.of("BrokerCommission", OperationTypeWithCommission.BROKERCOMMISSION),
                Arguments.of("ExchangeCommission", OperationTypeWithCommission.EXCHANGECOMMISSION),
                Arguments.of("ServiceCommission", OperationTypeWithCommission.SERVICECOMMISSION),
                Arguments.of("MarginCommission", OperationTypeWithCommission.MARGINCOMMISSION),
                Arguments.of("OtherCommission", OperationTypeWithCommission.OTHERCOMMISSION),
                Arguments.of("PayIn", OperationTypeWithCommission.PAYIN),
                Arguments.of("PayOut", OperationTypeWithCommission.PAYOUT),
                Arguments.of("Tax", OperationTypeWithCommission.TAX),
                Arguments.of("TaxLucre", OperationTypeWithCommission.TAXLUCRE),
                Arguments.of("TaxDividend", OperationTypeWithCommission.TAXDIVIDEND),
                Arguments.of("TaxCoupon", OperationTypeWithCommission.TAXCOUPON),
                Arguments.of("TaxBack", OperationTypeWithCommission.TAXBACK),
                Arguments.of("Repayment", OperationTypeWithCommission.REPAYMENT),
                Arguments.of("PartRepayment", OperationTypeWithCommission.PARTREPAYMENT),
                Arguments.of("Coupon", OperationTypeWithCommission.COUPON),
                Arguments.of("Dividend", OperationTypeWithCommission.DIVIDEND),
                Arguments.of("SecurityIn", OperationTypeWithCommission.SECURITYIN),
                Arguments.of("SecurityOut", OperationTypeWithCommission.SECURITYOUT)
        );
    }

    @ParameterizedTest
    @MethodSource("valuesAndOperationTypesWithCommission")
    void toString_returnsValue(final String expectedValue, final OperationTypeWithCommission operationTypeWithCommission) {
        Assertions.assertEquals(expectedValue, operationTypeWithCommission.toString());
    }

    @ParameterizedTest
    @MethodSource("valuesAndOperationTypesWithCommission")
    void fromValue_returnProperEnum(final String value, final OperationTypeWithCommission expectedOperationTypeWithCommission) {
        Assertions.assertEquals(expectedOperationTypeWithCommission, OperationTypeWithCommission.fromValue(value));
    }

    @ParameterizedTest
    @MethodSource("valuesAndOperationTypesWithCommission")
    void jsonMapping_mapsValue(final String value, final OperationTypeWithCommission operationTypeWithCommission) throws JsonProcessingException {
        Assertions.assertEquals('"' + value + '"', TestUtils.OBJECT_MAPPER.writeValueAsString(operationTypeWithCommission));
    }

    @ParameterizedTest
    @MethodSource("valuesAndOperationTypesWithCommission")
    void jsonMapping_createsFromValue(final String value, final OperationTypeWithCommission operationTypeWithCommission)
            throws JsonProcessingException {
        final String content = '"' + value + '"';
        Assertions.assertEquals(operationTypeWithCommission, TestUtils.OBJECT_MAPPER.readValue(content, OperationTypeWithCommission.class));
    }

}