package ru.obukhov.investor.common.model.poi;

import org.apache.poi.xddf.usermodel.chart.AxisPosition;
import org.apache.poi.xddf.usermodel.chart.ChartTypes;
import org.apache.poi.xddf.usermodel.chart.XDDFLineChartData;
import org.junit.Assert;
import org.junit.Test;

public class ExtendedChartTest {

    @Test
    public void createChartData() {
        ExtendedChart chart = ExcelTestDataHelper.createExtendedChart();
        AxisPosition categoryAxisPosition = AxisPosition.BOTTOM;
        AxisPosition valueAxisPosition = AxisPosition.LEFT;

        ExtendedChartData chartData = chart.createChartData(categoryAxisPosition, valueAxisPosition, ChartTypes.LINE);

        Assert.assertNotNull(chartData);
        Assert.assertNotNull(chartData.getDelegate());
        Assert.assertEquals(categoryAxisPosition, chartData.getCategoryAxis().getPosition());
        Assert.assertEquals(1, chartData.getValueAxes().size());
        Assert.assertEquals(valueAxisPosition, chartData.getValueAxes().get(0).getPosition());
        Assert.assertEquals(XDDFLineChartData.class, chartData.getDelegate().getClass());

    }

}