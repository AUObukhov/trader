package ru.obukhov.trader.market.model.transform;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mapstruct.factory.Mappers;
import ru.obukhov.trader.market.model.Sector;

import java.util.stream.Stream;

class SectorMapperUnitTest {

    private final SectorMapper sectorMapper = Mappers.getMapper(SectorMapper.class);

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
                Arguments.of("UNKNOWN", Sector.UNKNOWN),
                Arguments.of(StringUtils.EMPTY, Sector.UNKNOWN)
        );
    }

    @ParameterizedTest
    @MethodSource("valuesAndSectors")
    void map(final String value, final Sector sector) {
        Assertions.assertEquals(sector, sectorMapper.map(value.toLowerCase()));
        Assertions.assertEquals(sector, sectorMapper.map(value));
    }

    @Test
    void map_whenValueIsNull() {
        Assertions.assertEquals(Sector.UNKNOWN, sectorMapper.map(null));
    }

}