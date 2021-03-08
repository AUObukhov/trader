package ru.obukhov.investor.common.model.poi;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Chart;
import org.apache.poi.ss.usermodel.charts.ChartAxis;
import org.apache.poi.ss.usermodel.charts.ChartAxisFactory;
import org.apache.poi.ss.usermodel.charts.ChartData;
import org.apache.poi.ss.usermodel.charts.ChartDataFactory;
import org.apache.poi.ss.usermodel.charts.ChartLegend;
import org.apache.poi.ss.usermodel.charts.ManualLayout;
import org.apache.poi.ss.usermodel.charts.ValueAxis;
import org.apache.poi.xddf.usermodel.chart.AxisPosition;
import org.apache.poi.xddf.usermodel.chart.ChartTypes;
import org.apache.poi.xddf.usermodel.chart.XDDFCategoryAxis;
import org.apache.poi.xddf.usermodel.chart.XDDFChartData;
import org.apache.poi.xddf.usermodel.chart.XDDFValueAxis;
import org.apache.poi.xssf.usermodel.XSSFChart;

import java.util.List;

/**
 * Decorator of {@link XSSFChart} with additional handy methods
 */
@RequiredArgsConstructor
public class ExtendedChart implements Chart, ChartAxisFactory {

    @Getter
    private final XSSFChart delegate;

    // region additional methods

    public ExtendedChartData createChartData(AxisPosition categoryAxisPosition,
                                             AxisPosition valueAxisPosition,
                                             ChartTypes chartType) {
        XDDFCategoryAxis categoryAxis = delegate.createCategoryAxis(categoryAxisPosition);
        XDDFValueAxis valueAxis = delegate.createValueAxis(valueAxisPosition);
        XDDFChartData xddfChartData = delegate.createData(chartType, categoryAxis, valueAxis);
        return new ExtendedChartData(xddfChartData);
    }

    // endregion

    // region XSSFChart delegations

    public void plot(ExtendedChartData chartData) {
        delegate.plot(chartData.getDelegate());
    }

    // endregion

    // region Chart implementation

    @Override
    public ChartDataFactory getChartDataFactory() {
        return delegate.getChartDataFactory();
    }

    @Override
    public ChartAxisFactory getChartAxisFactory() {
        return delegate.getChartAxisFactory();
    }

    @Override
    public ChartLegend getOrCreateLegend() {
        return delegate.getOrCreateLegend();
    }

    @Override
    public void deleteLegend() {
        delegate.deleteLegend();
    }

    @Override
    public List<? extends ChartAxis> getAxis() {
        return delegate.getAxis();
    }

    @Override
    public void plot(ChartData data, ChartAxis... axis) {
        delegate.plot(data, axis);
    }

    @Override
    public ManualLayout getManualLayout() {
        return delegate.getManualLayout();
    }

    // endregion

    // region ChartAxisFactory implementation

    @Override
    public ValueAxis createValueAxis(org.apache.poi.ss.usermodel.charts.AxisPosition pos) {
        return delegate.createValueAxis(pos);
    }

    @Override
    public ChartAxis createCategoryAxis(org.apache.poi.ss.usermodel.charts.AxisPosition pos) {
        return delegate.createCategoryAxis(pos);
    }

    @Override
    public ChartAxis createDateAxis(org.apache.poi.ss.usermodel.charts.AxisPosition pos) {
        return delegate.createDateAxis(pos);
    }

    // endregion

}