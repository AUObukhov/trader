package ru.obukhov.trader.common.model.poi;

import lombok.Getter;
import org.apache.poi.xddf.usermodel.chart.MarkerStyle;
import org.apache.poi.xddf.usermodel.chart.XDDFCategoryAxis;
import org.apache.poi.xddf.usermodel.chart.XDDFCategoryDataSource;
import org.apache.poi.xddf.usermodel.chart.XDDFChartData;
import org.apache.poi.xddf.usermodel.chart.XDDFLineChartData;
import org.apache.poi.xddf.usermodel.chart.XDDFNumericalDataSource;
import org.apache.poi.xddf.usermodel.chart.XDDFValueAxis;
import ru.obukhov.trader.common.util.MathUtils;

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
    private final XDDFChartData delegate;

    public ExtendedChartData(XDDFChartData delegate) {
        this.delegate = delegate;
    }

    // region additional methods

    public void addSeries(XDDFCategoryDataSource categoryDataSource,
                          XDDFNumericalDataSource<Number> numericalDataSource,
                          short markerSize,
                          MarkerStyle markerStyle) {
        XDDFLineChartData.Series series =
                (XDDFLineChartData.Series) delegate.addSeries(categoryDataSource, numericalDataSource);
        series.setMarkerSize(markerSize);
        series.setMarkerStyle(markerStyle);
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
        return delegate.getSeries();
    }

    public void removeSeries(int n) {
        delegate.removeSeries(n);
    }

    // endregion

}