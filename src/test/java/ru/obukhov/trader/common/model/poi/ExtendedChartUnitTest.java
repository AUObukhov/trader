package ru.obukhov.trader.common.model.poi;

import org.apache.poi.xddf.usermodel.chart.AxisPosition;
import org.apache.poi.xddf.usermodel.chart.ChartTypes;
import org.apache.poi.xddf.usermodel.chart.XDDFLineChartData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ExtendedChartUnitTest {

    @Test
    void createChartData() {
        ExtendedChart chart = ExcelTestDataHelper.createExtendedChart();
        AxisPosition categoryAxisPosition = AxisPosition.BOTTOM;
        AxisPosition valueAxisPosition = AxisPosition.LEFT;

        ExtendedChartData chartData = chart.createChartData(categoryAxisPosition, valueAxisPosition, ChartTypes.LINE);

        Assertions.assertNotNull(chartData);
        Assertions.assertNotNull(chartData.getDelegate());
        Assertions.assertEquals(categoryAxisPosition, chartData.getCategoryAxis().getPosition());
        Assertions.assertEquals(1, chartData.getValueAxes().size());
        Assertions.assertEquals(valueAxisPosition, chartData.getValueAxes().get(0).getPosition());
        Assertions.assertEquals(XDDFLineChartData.class, chartData.getDelegate().getClass());

    }

}