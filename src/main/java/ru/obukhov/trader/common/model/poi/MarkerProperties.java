package ru.obukhov.trader.common.model.poi;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.poi.xddf.usermodel.chart.MarkerStyle;

import java.awt.Color;

@Data
@AllArgsConstructor
public class MarkerProperties {
    public static final MarkerProperties NO_MARKER = new MarkerProperties((short) 2, MarkerStyle.NONE, null);

    private short size;
    private MarkerStyle style;
    private Color color;

}