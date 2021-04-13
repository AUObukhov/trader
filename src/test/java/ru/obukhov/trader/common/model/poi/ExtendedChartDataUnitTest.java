package ru.obukhov.trader.common.model.poi;

import org.apache.poi.xddf.usermodel.XDDFColorRgbBinary;
import org.apache.poi.xddf.usermodel.XDDFSolidFillProperties;
import org.apache.poi.xddf.usermodel.chart.MarkerStyle;
import org.apache.poi.xddf.usermodel.chart.XDDFCategoryDataSource;
import org.apache.poi.xddf.usermodel.chart.XDDFChartData;
import org.apache.poi.xddf.usermodel.chart.XDDFDataSourcesFactory;
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
    void addSeries() {
        ExtendedChartData chartData = ExcelTestDataHelper.createExtendedChartData();

        String[] category = new String[]{"s1", "s2", "s3"};
        Integer[] values = new Integer[]{1, 2, 3};
        XDDFCategoryDataSource categoryDataSource = XDDFDataSourcesFactory.fromArray(category);
        XDDFNumericalDataSource<Number> numericalDataSource = XDDFDataSourcesFactory.fromArray(values);

        Color color = Color.RED;

        chartData.addSeries(categoryDataSource, numericalDataSource, (short) 2, MarkerStyle.CIRCLE, color);

        List<XDDFChartData.Series> seriesList = chartData.getSeries();
        Assertions.assertEquals(1, seriesList.size());
        XDDFChartData.Series series = seriesList.get(0);
        Assertions.assertSame(categoryDataSource, series.getCategoryData());
        Assertions.assertSame(numericalDataSource, series.getValuesData());

        XDDFSolidFillProperties fillProperties =
                (XDDFSolidFillProperties) series.getShapeProperties().getFillProperties();
        XDDFColorRgbBinary xddfColor = (XDDFColorRgbBinary) fillProperties.getColor();
        AssertUtils.assertEquals(colorMapper.mapToBytes(color), xddfColor.getValue());
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