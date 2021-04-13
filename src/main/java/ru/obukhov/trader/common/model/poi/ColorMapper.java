package ru.obukhov.trader.common.model.poi;

import org.apache.poi.xddf.usermodel.XDDFColor;
import org.mapstruct.Mapper;

import java.awt.Color;

/**
 * Maps {@link Color} to {@link XDDFColor}
 */
@Mapper
public interface ColorMapper {

    default byte[] mapToBytes(Color source) {
        return new byte[]{(byte) source.getRed(), (byte) source.getGreen(), (byte) source.getBlue()};
    }

    default XDDFColor mapToXDDFColor(Color source) {
        return XDDFColor.from(mapToBytes(source));
    }

}