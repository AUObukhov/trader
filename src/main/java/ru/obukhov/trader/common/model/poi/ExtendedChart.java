package ru.obukhov.trader.common.model.poi;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ooxml.POIXMLFactory;
import org.apache.poi.ooxml.POIXMLRelation;
import org.apache.poi.xddf.usermodel.chart.AxisPosition;
import org.apache.poi.xddf.usermodel.chart.ChartTypes;
import org.apache.poi.xddf.usermodel.chart.XDDFCategoryAxis;
import org.apache.poi.xddf.usermodel.chart.XDDFChart;
import org.apache.poi.xddf.usermodel.chart.XDDFChartData;
import org.apache.poi.xddf.usermodel.chart.XDDFValueAxis;
import org.apache.poi.xssf.usermodel.XSSFChart;

@Getter
@RequiredArgsConstructor
public class ExtendedChart extends XDDFChart {

    private final XSSFChart delegate;

    // region additional methods

    public ExtendedChartData createChartData(
            final AxisPosition categoryAxisPosition,
            final AxisPosition valueAxisPosition,
            final ChartTypes chartType
    ) {
        final XDDFCategoryAxis categoryAxis = delegate.createCategoryAxis(categoryAxisPosition);
        final XDDFValueAxis valueAxis = delegate.createValueAxis(valueAxisPosition);
        final XDDFChartData xddfChartData = delegate.createData(chartType, categoryAxis, valueAxis);
        return new ExtendedChartData(xddfChartData, this);
    }

    // endregion

    // region XSSFChart delegations

    public void plot(final ExtendedChartData chartData) {
        delegate.plot(chartData.getDelegate());
    }

    // endregion

    // region XDDFChart implementation

    @Override
    public void deleteLegend() {
        delegate.deleteLegend();
    }

    @Override
    protected POIXMLRelation getChartRelation() {
        throw new UnsupportedOperationException();
    }

    @Override
    protected POIXMLRelation getChartWorkbookRelation() {
        throw new UnsupportedOperationException();
    }

    @Override
    protected POIXMLFactory getChartFactory() {
        throw new UnsupportedOperationException();
    }

    // endregion

}