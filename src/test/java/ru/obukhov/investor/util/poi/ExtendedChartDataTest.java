package ru.obukhov.investor.util.poi;

import org.apache.poi.xddf.usermodel.chart.MarkerStyle;
import org.apache.poi.xddf.usermodel.chart.XDDFCategoryDataSource;
import org.apache.poi.xddf.usermodel.chart.XDDFChartData;
import org.apache.poi.xddf.usermodel.chart.XDDFDataSourcesFactory;
import org.apache.poi.xddf.usermodel.chart.XDDFNumericalDataSource;
import org.apache.poi.xddf.usermodel.chart.XDDFValueAxis;
import org.junit.Test;
import ru.obukhov.investor.test.utils.AssertUtils;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

public class ExtendedChartDataTest {

    @Test
    public void addSeries() {
        ExtendedChartData chartData = ExcelTestDataHelper.createExtendedChartData();

        String[] category = new String[]{"s1", "s2", "s3"};
        Integer[] values = new Integer[]{1, 2, 3};
        XDDFCategoryDataSource categoryDataSource = XDDFDataSourcesFactory.fromArray(category);
        XDDFNumericalDataSource<Number> numericalDataSource = XDDFDataSourcesFactory.fromArray(values);

        chartData.addSeries(categoryDataSource, numericalDataSource, (short) 2, MarkerStyle.CIRCLE);

        List<XDDFChartData.Series> seriesList = chartData.getSeries();
        assertEquals(1, seriesList.size());
        XDDFChartData.Series series = seriesList.get(0);
        assertSame(categoryDataSource, series.getCategoryData());
        assertSame(numericalDataSource, series.getValuesData());
    }

    // region stretchChart tests

    @Test
    public void stretchChart_doesNothing_whenNoValues() {
        ExtendedChartData chartData = ExcelTestDataHelper.createExtendedChartData();

        String[] category = new String[0];
        Integer[] values = new Integer[0];
        XDDFCategoryDataSource categoryDataSource = XDDFDataSourcesFactory.fromArray(category);
        XDDFNumericalDataSource<Number> numericalDataSource = XDDFDataSourcesFactory.fromArray(values);

        chartData.addSeries(categoryDataSource, numericalDataSource, (short) 2, MarkerStyle.CIRCLE);

        chartData.stretchChart();

        XDDFValueAxis valueAxis = chartData.getValueAxes().get(0);
        AssertUtils.assertEquals(Double.NaN, valueAxis.getMinimum());
        AssertUtils.assertEquals(Double.NaN, valueAxis.getMaximum());

    }

    @Test
    public void stretchChart_doesNothing_whenAllValuesAreNull() {
        ExtendedChartData chartData = ExcelTestDataHelper.createExtendedChartData();

        String[] category = new String[]{"s1", "s2", "s3"};
        Integer[] values = new Integer[]{null, null, null};
        XDDFCategoryDataSource categoryDataSource = XDDFDataSourcesFactory.fromArray(category);
        XDDFNumericalDataSource<Number> numericalDataSource = XDDFDataSourcesFactory.fromArray(values);

        chartData.addSeries(categoryDataSource, numericalDataSource, (short) 2, MarkerStyle.CIRCLE);

        chartData.stretchChart();

        XDDFValueAxis valueAxis = chartData.getValueAxes().get(0);
        AssertUtils.assertEquals(Double.NaN, valueAxis.getMinimum());
        AssertUtils.assertEquals(Double.NaN, valueAxis.getMaximum());

    }

    @Test
    public void stretchChart_stretchesChart_whenValuesAreNotEmpty() {
        ExtendedChartData chartData = ExcelTestDataHelper.createExtendedChartData();

        String[] category = new String[]{"s1", "s2", "s3", "s4", "s5"};
        Integer[] values = new Integer[]{1, null, 3, 2, 0};
        XDDFCategoryDataSource categoryDataSource = XDDFDataSourcesFactory.fromArray(category);
        XDDFNumericalDataSource<Number> numericalDataSource = XDDFDataSourcesFactory.fromArray(values);

        chartData.addSeries(categoryDataSource, numericalDataSource, (short) 2, MarkerStyle.CIRCLE);

        chartData.stretchChart();

        XDDFValueAxis valueAxis = chartData.getValueAxes().get(0);
        AssertUtils.assertEquals(0, valueAxis.getMinimum());
        AssertUtils.assertEquals(3, valueAxis.getMaximum());

    }

    // endregion

}