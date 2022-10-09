package ru.obukhov.trader.market.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ru.obukhov.trader.test.utils.TestUtils;

import java.util.stream.Stream;

class SectorUnitTest {

    @SuppressWarnings("unused")
    static Stream<Arguments> valuesAndSectors() {
        return Stream.of(
                Arguments.of("IT", Sector.IT),
                Arguments.of("CONSUMER", Sector.CONSUMER),
                Arguments.of("HEALTH_CARE", Sector.HEALTH_CARE),
                Arguments.of("FINANCIAL", Sector.FINANCIAL),
                Arguments.of("INDUSTRIALS", Sector.INDUSTRIALS),
                Arguments.of("MATERIALS", Sector.MATERIALS),
                Arguments.of("ECOMATERIALS", Sector.ECOMATERIALS),
                Arguments.of("ENERGY", Sector.ENERGY),
                Arguments.of("GREEN_ENERGY", Sector.GREEN_ENERGY),
                Arguments.of("REAL_ESTATE", Sector.REAL_ESTATE),
                Arguments.of("GREEN_BUILDINGS", Sector.GREEN_BUILDINGS),
                Arguments.of("UTILITIES", Sector.UTILITIES),
                Arguments.of("TELECOM", Sector.TELECOM),
                Arguments.of("ELECTROCARS", Sector.ELECTROCARS),
                Arguments.of("GOVERNMENT", Sector.GOVERNMENT),
                Arguments.of("MUNICIPAL", Sector.MUNICIPAL),
                Arguments.of("OTHER", Sector.OTHER),
                Arguments.of("UNKNOWN", Sector.UNKNOWN)
        );
    }

    @ParameterizedTest
    @MethodSource("valuesAndSectors")
    void jsonMapping_mapsValue(final String value, final Sector sector) throws JsonProcessingException {
        Assertions.assertEquals('"' + value + '"', TestUtils.OBJECT_MAPPER.writeValueAsString(sector));
    }

    @ParameterizedTest
    @MethodSource("valuesAndSectors")
    void jsonMapping_createsFromValue(final String value, final Sector sector) throws JsonProcessingException {
        Assertions.assertEquals(sector, TestUtils.OBJECT_MAPPER.readValue('"' + value + '"', Sector.class));
    }

    @ParameterizedTest
    @MethodSource("valuesAndSectors")
    void valueOfIgnoreCase(final String value, final Sector sector) {
        Assertions.assertEquals(sector, Sector.valueOfIgnoreCase(value.toLowerCase()));
        Assertions.assertEquals(sector, Sector.valueOfIgnoreCase(value));
    }

    @Test
    void valueOfIgnoreCase_whenValueIsNull() {
        Assertions.assertEquals(Sector.UNKNOWN, Sector.valueOfIgnoreCase(null));
    }

    @Test
    void valueOfIgnoreCase_whenValueIsEmpty() {
        Assertions.assertEquals(Sector.UNKNOWN, Sector.valueOfIgnoreCase(StringUtils.EMPTY));
    }

}