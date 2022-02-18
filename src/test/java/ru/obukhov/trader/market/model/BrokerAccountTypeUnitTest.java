package ru.obukhov.trader.market.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

class BrokerAccountTypeUnitTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @SuppressWarnings("unused")
    static Stream<Arguments> valuesAndBrokerAccountTypes() {
        return Stream.of(
                Arguments.of("Tinkoff", BrokerAccountType.TINKOFF),
                Arguments.of("TinkoffIis", BrokerAccountType.TINKOFF_IIS)
        );
    }

    @ParameterizedTest
    @MethodSource("valuesAndBrokerAccountTypes")
    void toString_returnsProperEnum(final String expectedValue, final BrokerAccountType brokerAccountType) {
        Assertions.assertEquals(expectedValue, brokerAccountType.toString());
    }

    @ParameterizedTest
    @MethodSource("valuesAndBrokerAccountTypes")
    void fromValue_returnsProperEnum(final String value, final BrokerAccountType expectedBrokerAccountType) {
        Assertions.assertEquals(expectedBrokerAccountType, BrokerAccountType.fromValue(value));
    }

    @ParameterizedTest
    @MethodSource("valuesAndBrokerAccountTypes")
    void jsonMapping_mapsValue(final String value, final BrokerAccountType brokerAccountType) throws JsonProcessingException {
        Assertions.assertEquals('"' + value + '"', objectMapper.writeValueAsString(brokerAccountType));
    }

    @ParameterizedTest
    @MethodSource("valuesAndBrokerAccountTypes")
    void jsonMapping_createsFromValue(final String value, final BrokerAccountType expectedBrokerAccountType) throws JsonProcessingException {
        Assertions.assertEquals(expectedBrokerAccountType, objectMapper.readValue('"' + value + '"', BrokerAccountType.class));
    }

}