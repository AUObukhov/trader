package ru.obukhov.trader.common.model.poi;

import org.apache.poi.xddf.usermodel.chart.MarkerStyle;
import org.apache.poi.xddf.usermodel.chart.XDDFCategoryDataSource;
import org.apache.poi.xddf.usermodel.chart.XDDFChartData;
import org.apache.poi.xddf.usermodel.chart.XDDFDataSourcesFactory;
import org.apache.poi.xddf.usermodel.chart.XDDFLineChartData;
import org.apache.poi.xddf.usermodel.chart.XDDFNumericalDataSource;
import org.apache.poi.xddf.usermodel.chart.XDDFValueAxis;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.test.utils.model.PoiTestData;

import java.awt.Color;
import java.io.IOException;
import java.util.List;

class ExtendedChartDataUnitTest {

    private ExtendedChartData chartData;

    @BeforeEach
    void setUp() throws IOException {
        chartData = PoiTestData.createExtendedChartData();
    }

    @Test
    void addSeries_withColor_andGetSeries() {
        final String[] category1 = new String[]{"s1", "s2", "s3"};
        final Integer[] values1 = new Integer[]{1, 2, 3};
        final MarkerProperties markerProperties1 = new MarkerProperties((short) 2, MarkerStyle.CIRCLE, Color.GREEN);
        final Color seriesColor1 = Color.RED;

        final XDDFCategoryDataSource categoryDataSource1 = XDDFDataSourcesFactory.fromArray(category1);
        final XDDFNumericalDataSource<Number> numericalDataSource1 = XDDFDataSourcesFactory.fromArray(values1);
        chartData.addSeries(categoryDataSource1, numericalDataSource1, markerProperties1, seriesColor1);

        final String[] category2 = new String[]{"s4", "s5", "s6"};
        final Integer[] values2 = new Integer[]{4, 5, 6};
        final MarkerProperties markerProperties2 = new MarkerProperties((short) 3, MarkerStyle.SQUARE, Color.CYAN);
        final Color seriesColor2 = Color.BLUE;

        final XDDFCategoryDataSource categoryDataSource2 = XDDFDataSourcesFactory.fromArray(category2);
        final XDDFNumericalDataSource<Number> numericalDataSource2 = XDDFDataSourcesFactory.fromArray(values2);
        chartData.addSeries(categoryDataSource2, numericalDataSource2, markerProperties2, seriesColor2);

        final List<XDDFChartData.Series> seriesList = chartData.getSeries();
        Assertions.assertEquals(2, seriesList.size());

        final XDDFLineChartData.Series series1 = (XDDFLineChartData.Series) seriesList.get(0);
        Assertions.assertSame(categoryDataSource1, series1.getCategoryData());
        Assertions.assertSame(numericalDataSource1, series1.getValuesData());
        AssertUtils.assertSeriesColor(series1, seriesColor1);
        AssertUtils.assertSeriesMarkerColor(series1, markerProperties1.getColor());

        final XDDFLineChartData.Series series2 = (XDDFLineChartData.Series) seriesList.get(1);
        Assertions.assertSame(categoryDataSource2, series2.getCategoryData());
        Assertions.assertSame(numericalDataSource2, series2.getValuesData());
        AssertUtils.assertSeriesColor(series2, seriesColor2);
        AssertUtils.assertSeriesMarkerColor(series2, markerProperties2.getColor());
    }

    @Test
    void addSeries_withoutColors_andGetSeries() {
        final String[] category1 = new String[]{"s1", "s2", "s3"};
        final Integer[] values1 = new Integer[]{1, 2, 3};
        final MarkerProperties markerProperties1 = new MarkerProperties((short) 2, MarkerStyle.CIRCLE, null);

        final XDDFCategoryDataSource categoryDataSource1 = XDDFDataSourcesFactory.fromArray(category1);
        final XDDFNumericalDataSource<Number> numericalDataSource1 = XDDFDataSourcesFactory.fromArray(values1);
        chartData.addSeries(categoryDataSource1, numericalDataSource1, markerProperties1);

        final String[] category2 = new String[]{"s4", "s5", "s6"};
        final Integer[] values2 = new Integer[]{4, 5, 6};
        final MarkerProperties markerProperties2 = new MarkerProperties((short) 3, MarkerStyle.SQUARE, null);

        final XDDFCategoryDataSource categoryDataSource2 = XDDFDataSourcesFactory.fromArray(category2);
        final XDDFNumericalDataSource<Number> numericalDataSource2 = XDDFDataSourcesFactory.fromArray(values2);
        chartData.addSeries(categoryDataSource2, numericalDataSource2, markerProperties2);

        final List<XDDFChartData.Series> seriesList = chartData.getSeries();
        Assertions.assertEquals(2, seriesList.size());

        final XDDFLineChartData.Series series1 = (XDDFLineChartData.Series) seriesList.get(0);
        Assertions.assertSame(categoryDataSource1, series1.getCategoryData());
        Assertions.assertSame(numericalDataSource1, series1.getValuesData());
        Assertions.assertNull(series1.getShapeProperties());

        final XDDFLineChartData.Series series2 = (XDDFLineChartData.Series) seriesList.get(1);
        Assertions.assertSame(categoryDataSource2, series2.getCategoryData());
        Assertions.assertSame(numericalDataSource2, series2.getValuesData());
        Assertions.assertNull(series2.getShapeProperties());
    }

    // region stretchChart tests

    @Test
    void stretchChart_doesNothing_whenNoValues() {
        final String[] category = new String[0];
        final Integer[] values = new Integer[0];
        final XDDFCategoryDataSource categoryDataSource = XDDFDataSourcesFactory.fromArray(category);
        final XDDFNumericalDataSource<Number> numericalDataSource = XDDFDataSourcesFactory.fromArray(values);
        final MarkerProperties markerProperties = new MarkerProperties((short) 2, MarkerStyle.CIRCLE, Color.BLUE);

        chartData.addSeries(categoryDataSource, numericalDataSource, markerProperties, Color.RED);

        chartData.stretchChart();

        final XDDFValueAxis valueAxis = chartData.getValueAxes().get(0);
        AssertUtils.assertEquals(Double.NaN, valueAxis.getMinimum());
        AssertUtils.assertEquals(Double.NaN, valueAxis.getMaximum());
    }

    @Test
    void stretchChart_doesNothing_whenAllValuesAreNull() {
        final String[] category = new String[]{"s1", "s2", "s3"};
        final Integer[] values = new Integer[]{null, null, null};
        final XDDFCategoryDataSource categoryDataSource = XDDFDataSourcesFactory.fromArray(category);
        final XDDFNumericalDataSource<Number> numericalDataSource = XDDFDataSourcesFactory.fromArray(values);
        final MarkerProperties markerProperties = new MarkerProperties((short) 2, MarkerStyle.CIRCLE, Color.BLUE);

        chartData.addSeries(categoryDataSource, numericalDataSource, markerProperties, Color.RED);

        chartData.stretchChart();

        final XDDFValueAxis valueAxis = chartData.getValueAxes().get(0);
        AssertUtils.assertEquals(Double.NaN, valueAxis.getMinimum());
        AssertUtils.assertEquals(Double.NaN, valueAxis.getMaximum());
    }

    @Test
    void stretchChart_stretchesChart_whenValuesAreNotEmpty() {
        final String[] category = new String[]{"s1", "s2", "s3", "s4", "s5"};
        final Integer[] values = new Integer[]{1, null, 3, 2, 0};
        final XDDFCategoryDataSource categoryDataSource = XDDFDataSourcesFactory.fromArray(category);
        final XDDFNumericalDataSource<Number> numericalDataSource = XDDFDataSourcesFactory.fromArray(values);
        final MarkerProperties markerProperties = new MarkerProperties((short) 2, MarkerStyle.CIRCLE, Color.BLUE);

        chartData.addSeries(categoryDataSource, numericalDataSource, markerProperties, Color.RED);

        chartData.stretchChart();

        final XDDFValueAxis valueAxis = chartData.getValueAxes().get(0);
        AssertUtils.assertEquals(0, valueAxis.getMinimum());
        AssertUtils.assertEquals(3, valueAxis.getMaximum());
    }

    // endregion

}