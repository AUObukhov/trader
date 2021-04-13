package ru.obukhov.trader.common.model.poi;

import org.apache.poi.xddf.usermodel.XDDFColorRgbBinary;
import org.apache.poi.xddf.usermodel.XDDFSolidFillProperties;
import org.apache.poi.xddf.usermodel.chart.MarkerStyle;
import org.apache.poi.xddf.usermodel.chart.XDDFCategoryDataSource;
import org.apache.poi.xddf.usermodel.chart.XDDFChartData;
import org.apache.poi.xddf.usermodel.chart.XDDFDataSourcesFactory;
import org.apache.poi.xddf.usermodel.chart.XDDFLineChartData;
import org.apache.poi.xddf.usermodel.chart.XDDFNumericalDataSource;
import org.apache.poi.xddf.usermodel.chart.XDDFValueAxis;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.obukhov.trader.test.utils.AssertUtils;

import java.awt.Color;
import java.util.List;

class ExtendedChartDataUnitTest {

    private final ColorMapper colorMapper = Mappers.getMapper(ColorMapper.class);

    @Test
    void addSeries_andGetSeries() {
        ExtendedChartData chartData = ExcelTestDataHelper.createExtendedChartData();

        final String[] category1 = new String[]{"s1", "s2", "s3"};
        final Integer[] values1 = new Integer[]{1, 2, 3};
        final short markerSize1 = (short) 2;
        final MarkerStyle marketStyle1 = MarkerStyle.CIRCLE;
        final Color color1 = Color.RED;

        final XDDFCategoryDataSource categoryDataSource1 = XDDFDataSourcesFactory.fromArray(category1);
        final XDDFNumericalDataSource<Number> numericalDataSource1 = XDDFDataSourcesFactory.fromArray(values1);
        chartData.addSeries(categoryDataSource1, numericalDataSource1, markerSize1, marketStyle1, color1);

        final String[] category2 = new String[]{"s4", "s5", "s6"};
        final Integer[] values2 = new Integer[]{4, 5, 6};
        final short markerSize2 = (short) 3;
        final MarkerStyle marketStyle2 = MarkerStyle.SQUARE;
        final Color color2 = Color.BLUE;

        final XDDFCategoryDataSource categoryDataSource2 = XDDFDataSourcesFactory.fromArray(category2);
        final XDDFNumericalDataSource<Number> numericalDataSource2 = XDDFDataSourcesFactory.fromArray(values2);
        chartData.addSeries(categoryDataSource2, numericalDataSource2, markerSize2, marketStyle2, color2);

        List<XDDFChartData.Series> seriesList = chartData.getSeries();
        Assertions.assertEquals(2, seriesList.size());

        XDDFLineChartData.Series series1 = (XDDFLineChartData.Series) seriesList.get(0);
        Assertions.assertSame(categoryDataSource1, series1.getCategoryData());
        Assertions.assertSame(numericalDataSource1, series1.getValuesData());

        XDDFSolidFillProperties fillProperties1 =
                (XDDFSolidFillProperties) series1.getShapeProperties().getFillProperties();
        XDDFColorRgbBinary xddfColor1 = (XDDFColorRgbBinary) fillProperties1.getColor();
        AssertUtils.assertEquals(colorMapper.mapToBytes(color1), xddfColor1.getValue());

        XDDFLineChartData.Series series2 = (XDDFLineChartData.Series) seriesList.get(1);
        Assertions.assertSame(categoryDataSource2, series2.getCategoryData());
        Assertions.assertSame(numericalDataSource2, series2.getValuesData());

        XDDFSolidFillProperties fillProperties2 =
                (XDDFSolidFillProperties) series2.getShapeProperties().getFillProperties();
        XDDFColorRgbBinary xddfColor2 = (XDDFColorRgbBinary) fillProperties2.getColor();
        AssertUtils.assertEquals(colorMapper.mapToBytes(color2), xddfColor2.getValue());
    }

    // region stretchChart tests

    @Test
    void stretchChart_doesNothing_whenNoValues() {
        ExtendedChartData chartData = ExcelTestDataHelper.createExtendedChartData();

        String[] category = new String[0];
        Integer[] values = new Integer[0];
        XDDFCategoryDataSource categoryDataSource = XDDFDataSourcesFactory.fromArray(category);
        XDDFNumericalDataSource<Number> numericalDataSource = XDDFDataSourcesFactory.fromArray(values);

        chartData.addSeries(categoryDataSource, numericalDataSource, (short) 2, MarkerStyle.CIRCLE, Color.RED);

        chartData.stretchChart();

        XDDFValueAxis valueAxis = chartData.getValueAxes().get(0);
        AssertUtils.assertEquals(Double.NaN, valueAxis.getMinimum());
        AssertUtils.assertEquals(Double.NaN, valueAxis.getMaximum());

    }

    @Test
    void stretchChart_doesNothing_whenAllValuesAreNull() {
        ExtendedChartData chartData = ExcelTestDataHelper.createExtendedChartData();

        String[] category = new String[]{"s1", "s2", "s3"};
        Integer[] values = new Integer[]{null, null, null};
        XDDFCategoryDataSource categoryDataSource = XDDFDataSourcesFactory.fromArray(category);
        XDDFNumericalDataSource<Number> numericalDataSource = XDDFDataSourcesFactory.fromArray(values);

        chartData.addSeries(categoryDataSource, numericalDataSource, (short) 2, MarkerStyle.CIRCLE, Color.RED);

        chartData.stretchChart();

        XDDFValueAxis valueAxis = chartData.getValueAxes().get(0);
        AssertUtils.assertEquals(Double.NaN, valueAxis.getMinimum());
        AssertUtils.assertEquals(Double.NaN, valueAxis.getMaximum());

    }

    @Test
    void stretchChart_stretchesChart_whenValuesAreNotEmpty() {
        ExtendedChartData chartData = ExcelTestDataHelper.createExtendedChartData();

        String[] category = new String[]{"s1", "s2", "s3", "s4", "s5"};
        Integer[] values = new Integer[]{1, null, 3, 2, 0};
        XDDFCategoryDataSource categoryDataSource = XDDFDataSourcesFactory.fromArray(category);
        XDDFNumericalDataSource<Number> numericalDataSource = XDDFDataSourcesFactory.fromArray(values);

        chartData.addSeries(categoryDataSource, numericalDataSource, (short) 2, MarkerStyle.CIRCLE, Color.RED);

        chartData.stretchChart();

        XDDFValueAxis valueAxis = chartData.getValueAxes().get(0);
        AssertUtils.assertEquals(0, valueAxis.getMinimum());
        AssertUtils.assertEquals(3, valueAxis.getMaximum());

    }

    // endregion

}