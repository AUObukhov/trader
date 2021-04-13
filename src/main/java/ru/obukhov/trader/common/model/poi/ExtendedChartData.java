package ru.obukhov.trader.common.model.poi;

import lombok.Getter;
import org.apache.poi.xddf.usermodel.XDDFFillProperties;
import org.apache.poi.xddf.usermodel.XDDFLineProperties;
import org.apache.poi.xddf.usermodel.XDDFShapeProperties;
import org.apache.poi.xddf.usermodel.XDDFSolidFillProperties;
import org.apache.poi.xddf.usermodel.chart.MarkerStyle;
import org.apache.poi.xddf.usermodel.chart.XDDFCategoryAxis;
import org.apache.poi.xddf.usermodel.chart.XDDFCategoryDataSource;
import org.apache.poi.xddf.usermodel.chart.XDDFChartData;
import org.apache.poi.xddf.usermodel.chart.XDDFLineChartData;
import org.apache.poi.xddf.usermodel.chart.XDDFNumericalDataSource;
import org.apache.poi.xddf.usermodel.chart.XDDFValueAxis;
import org.mapstruct.factory.Mappers;
import ru.obukhov.trader.common.util.MathUtils;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Decorator of {@link XDDFChartData} with additional handy methods
 */
public class ExtendedChartData {

    @Getter
    private final ExtendedChart chart;
    @Getter
    private final XDDFChartData delegate;

    private final ColorMapper colorMapper = Mappers.getMapper(ColorMapper.class);

    public ExtendedChartData(XDDFChartData delegate, ExtendedChart chart) {
        this.delegate = delegate;
        this.chart = chart;
    }

    // region additional methods

    public void addSeries(
            XDDFCategoryDataSource categoryDataSource,
            XDDFNumericalDataSource<Number> numericalDataSource,
            short markerSize,
            MarkerStyle markerStyle,
            Color color
    ) {
        XDDFLineChartData.Series series =
                (XDDFLineChartData.Series) delegate.addSeries(categoryDataSource, numericalDataSource);
        series.setMarkerSize(markerSize);
        series.setMarkerStyle(markerStyle);
        setSeriesColor(series, color);
    }

    public void addSeries(
            XDDFCategoryDataSource categoryDataSource,
            XDDFNumericalDataSource<Number> numericalDataSource,
            short markerSize,
            MarkerStyle markerStyle
    ) {
        addSeries(categoryDataSource, numericalDataSource, markerSize, markerStyle, null);
    }

    private void setSeriesColor(XDDFChartData.Series series, Color color) {
        if (color != null) {
            XDDFFillProperties fillProperties = new XDDFSolidFillProperties(colorMapper.mapToXDDFColor(color));
            XDDFLineProperties lineProperties = new XDDFLineProperties(fillProperties);

            XDDFShapeProperties shapeProperties = series.getShapeProperties();
            if (shapeProperties == null) {
                shapeProperties = new XDDFShapeProperties();
            }
            shapeProperties.setFillProperties(fillProperties);
            shapeProperties.setLineProperties(lineProperties);
            series.setShapeProperties(shapeProperties);
        }
    }

    /**
     * Stretches chart to vertical borders
     */
    @SuppressWarnings("ConstantConditions")
    public void stretchChart() {
        List<Double> values = getSeries().stream()
                .map(this::getSeriesValues)
                .flatMap(Collection::stream)
                .filter(Objects::nonNull)
                .map(Number::doubleValue)
                .collect(Collectors.toList());

        if (!values.isEmpty()) {
            Double minimum = MathUtils.min(values);
            Double maximum = MathUtils.max(values);
            getValueAxes().forEach(axis -> {
                axis.setMinimum(minimum);
                axis.setMaximum(maximum);
            });
        }
    }

    private List<Number> getSeriesValues(XDDFChartData.Series series) {
        List<Number> values = new ArrayList<>();
        XDDFNumericalDataSource<? extends Number> valuesData = series.getValuesData();
        for (int index = 0; index < valuesData.getPointCount(); index++) {
            values.add(valuesData.getPointAt(index));
        }
        return values;
    }

    // endregion

    // region XDDFChartData delegations

    public XDDFCategoryAxis getCategoryAxis() {
        return delegate.getCategoryAxis();
    }

    public List<XDDFValueAxis> getValueAxes() {
        return delegate.getValueAxes();
    }

    public List<XDDFChartData.Series> getSeries() {
        int count = delegate.getSeriesCount();
        List<XDDFChartData.Series> series = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            series.add(delegate.getSeries(i));
        }
        return series;
    }

    public void removeSeries(int n) {
        delegate.removeSeries(n);
    }

    // endregion

}