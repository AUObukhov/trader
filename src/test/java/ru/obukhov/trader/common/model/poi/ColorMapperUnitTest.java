package ru.obukhov.trader.common.model.poi;

import org.apache.poi.xddf.usermodel.XDDFColorRgbBinary;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.obukhov.trader.test.utils.AssertUtils;

import java.awt.Color;

class ColorMapperUnitTest {

    private final ColorMapper colorMapper = Mappers.getMapper(ColorMapper.class);

    @Test
    void mapToBytes() {

        byte[] expectedBytes = {(byte) 255, 0, 0};

        byte[] bytes = colorMapper.mapToBytes(Color.RED);

        AssertUtils.assertEquals(expectedBytes, bytes);
    }

    @Test
    void mapToXDDFColor() {

        byte[] expectedBytes = {(byte) 255, 0, 0};

        XDDFColorRgbBinary xddfColor = (XDDFColorRgbBinary) colorMapper.mapToXDDFColor(Color.RED);

        AssertUtils.assertEquals(expectedBytes, xddfColor.getValue());
    }

}