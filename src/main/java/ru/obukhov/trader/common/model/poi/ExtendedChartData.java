package ru.obukhov.trader.common.model.poi;

import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.poi.xddf.usermodel.XDDFFillProperties;
import org.apache.poi.xddf.usermodel.XDDFLineProperties;
import org.apache.poi.xddf.usermodel.XDDFShapeProperties;
import org.apache.poi.xddf.usermodel.XDDFSolidFillProperties;
import org.apache.poi.xddf.usermodel.chart.XDDFCategoryAxis;
import org.apache.poi.xddf.usermodel.chart.XDDFCategoryDataSource;
import org.apache.poi.xddf.usermodel.chart.XDDFChartData;
import org.apache.poi.xddf.usermodel.chart.XDDFLineChartData;
import org.apache.poi.xddf.usermodel.chart.XDDFNumericalDataSource;
import org.apache.poi.xddf.usermodel.chart.XDDFValueAxis;
import org.mapstruct.factory.Mappers;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTMarker;
import ru.obukhov.trader.common.util.MathUtils;

import java.awt.Color;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Decorator of {@link XDDFChartData} with additional handy methods
 */
public class ExtendedChartData {

    @Getter
    private final ExtendedChart chart;
    @Getter
    private final XDDFChartData delegate;

    private final ColorMapper colorMapper = Mappers.getMapper(ColorMapper.class);

    public ExtendedChartData(final XDDFChartData delegate, final ExtendedChart chart) {
        this.delegate = delegate;
        this.chart = chart;
    }

    // region additional methods

    public void addSeries(
            final XDDFCategoryDataSource categoryDataSource,
            final XDDFNumericalDataSource<Number> numericalDataSource,
            final MarkerProperties markerProperties,
            final Color seriesColor
    ) {
        final XDDFLineChartData.Series series = (XDDFLineChartData.Series) delegate.addSeries(categoryDataSource, numericalDataSource);
        setMarkerProperties(series, markerProperties);
        setSeriesColor(series, seriesColor);
    }

    public void addSeries(
            final XDDFCategoryDataSource categoryDataSource,
            final XDDFNumericalDataSource<Number> numericalDataSource,
            final MarkerProperties markerProperties
    ) {
        addSeries(categoryDataSource, numericalDataSource, markerProperties, null);
    }

    private void setMarkerProperties(final XDDFLineChartData.Series series, final MarkerProperties markerProperties) {
        series.setMarkerSize(markerProperties.getSize());
        series.setMarkerStyle(markerProperties.getStyle());
        setMarkerColor(series, markerProperties.getColor());
    }


    private void setMarkerColor(final XDDFLineChartData.Series series, final Color color) {
        if (color != null) {
            final XDDFFillProperties fillProperties = new XDDFSolidFillProperties(colorMapper.mapToXDDFColor(color));
            final XDDFLineProperties lineProperties = new XDDFLineProperties();
            lineProperties.setFillProperties(fillProperties);

            final XDDFShapeProperties shapeProperties = new XDDFShapeProperties();
            shapeProperties.setFillProperties(fillProperties);
            shapeProperties.setLineProperties(lineProperties);

            final CTMarker marker = getMarker(series);
            if (marker.isSetSpPr()) {
                marker.getSpPr().set(shapeProperties.getXmlObject());
            } else {
                marker.setSpPr(shapeProperties.getXmlObject());
            }
        }
    }

    @SneakyThrows
    private CTMarker getMarker(final XDDFLineChartData.Series series) {
        final Method getMarkerMethod = series.getClass().getDeclaredMethod("getMarker");
        getMarkerMethod.trySetAccessible();
        return (CTMarker) getMarkerMethod.invoke(series);
    }

    private void setSeriesColor(final XDDFChartData.Series series, final Color color) {
        if (color != null) {
            final XDDFFillProperties fillProperties = new XDDFSolidFillProperties(colorMapper.mapToXDDFColor(color));
            final XDDFLineProperties lineProperties = new XDDFLineProperties(fillProperties);

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
        final List<Double> values = getSeries().stream()
                .map(this::getSeriesValues)
                .flatMap(Collection::stream)
                .filter(Objects::nonNull)
                .map(Number::doubleValue)
                .toList();

        if (!values.isEmpty()) {
            final Double minimum = MathUtils.min(values);
            final Double maximum = MathUtils.max(values);
            getValueAxes().forEach(axis -> {
                axis.setMinimum(minimum);
                axis.setMaximum(maximum);
            });
        }
    }

    private List<Number> getSeriesValues(final XDDFChartData.Series series) {
        final List<Number> values = new ArrayList<>();
        final XDDFNumericalDataSource<? extends Number> valuesData = series.getValuesData();
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
        final int count = delegate.getSeriesCount();
        final List<XDDFChartData.Series> series = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            series.add(delegate.getSeries(i));
        }
        return series;
    }

    // endregion

}