package ru.obukhov.trader.common.model.poi;

import org.apache.poi.xddf.usermodel.XDDFColor;
import org.mapstruct.Mapper;

import java.awt.*;

@Mapper
public interface ColorMapper {

    default byte[] mapToBytes(final Color source) {
        return new byte[]{(byte) source.getRed(), (byte) source.getGreen(), (byte) source.getBlue()};
    }

    default XDDFColor mapToXDDFColor(final Color source) {
        return XDDFColor.from(mapToBytes(source));
    }

}