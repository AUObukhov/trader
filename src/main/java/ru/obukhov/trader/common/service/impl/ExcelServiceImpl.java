package ru.obukhov.trader.common.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.xddf.usermodel.chart.AxisPosition;
import org.apache.poi.xddf.usermodel.chart.ChartTypes;
import org.apache.poi.xddf.usermodel.chart.MarkerStyle;
import org.apache.poi.xddf.usermodel.chart.XDDFCategoryDataSource;
import org.apache.poi.xddf.usermodel.chart.XDDFDataSourcesFactory;
import org.apache.poi.xddf.usermodel.chart.XDDFNumericalDataSource;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.common.model.Point;
import ru.obukhov.trader.common.model.poi.ExtendedCell;
import ru.obukhov.trader.common.model.poi.ExtendedChart;
import ru.obukhov.trader.common.model.poi.ExtendedChartData;
import ru.obukhov.trader.common.model.poi.ExtendedRow;
import ru.obukhov.trader.common.model.poi.ExtendedSheet;
import ru.obukhov.trader.common.model.poi.ExtendedWorkbook;
import ru.obukhov.trader.common.model.poi.MarkerProperties;
import ru.obukhov.trader.common.service.interfaces.ExcelFileService;
import ru.obukhov.trader.common.service.interfaces.ExcelService;
import ru.obukhov.trader.common.util.CollectionsUtils;
import ru.obukhov.trader.common.util.DateUtils;
import ru.obukhov.trader.market.model.Candle;
import ru.obukhov.trader.trading.model.StrategyType;
import ru.obukhov.trader.web.model.SimulatedOperation;
import ru.obukhov.trader.web.model.SimulatedPosition;
import ru.obukhov.trader.web.model.SimulationResult;
import ru.obukhov.trader.web.model.TradingConfig;
import ru.obukhov.trader.web.model.exchange.GetCandlesResponse;
import ru.tinkoff.invest.openapi.model.rest.CandleResolution;
import ru.tinkoff.invest.openapi.model.rest.OperationType;

import java.awt.Color;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExcelServiceImpl implements ExcelService {

    private static final String PERCENT_FORMAT = "0.00%";

    private static final int CHART_WIDTH = 20;
    private static final int CHART_HEIGHT = 40;
    private static final short MARKER_SIZE = (short) 10;

    private static final MarkerProperties SELL_OPERATION_MARKER_PROPERTIES =
            new MarkerProperties(MARKER_SIZE, MarkerStyle.DIAMOND, Color.GREEN);

    private static final MarkerProperties BUY_OPERATION_MARKER_PROPERTIES =
            new MarkerProperties(MARKER_SIZE, MarkerStyle.TRIANGLE, Color.RED);

    private static final MarkerProperties MAXIMUMS_MARKER_PROPERTIES =
            new MarkerProperties(MARKER_SIZE, MarkerStyle.TRIANGLE, Color.GREEN);

    private static final MarkerProperties MINIMUMS_MARKER_PROPERTIES =
            new MarkerProperties(MARKER_SIZE, MarkerStyle.DIAMOND, Color.RED);

    private final ExcelFileService excelFileService;

    @Override
    public void saveSimulationResults(final String ticker, final Collection<SimulationResult> results) {
        for (final SimulationResult result : results) {
            final ExtendedWorkbook workBook = createWorkBook();
            createSheet(workBook, ticker, result);
            saveToFile(workBook, "SimulationResult for '" + ticker + "'");
        }
    }

    @Override
    public void saveCandles(final String ticker, final Interval interval, final GetCandlesResponse response) {
        final ExtendedWorkbook workBook = createWorkBook();
        createSheet(workBook, ticker, interval, response);

        saveToFile(workBook, "Candles for '" + ticker + "'");
    }

    private void saveToFile(final ExtendedWorkbook workBook, final String fileName) {
        final String nowString = LocalDateTime.now().format(DateUtils.FILE_NAME_DATE_TIME_FORMATTER);
        final String extendedFileName = fileName + " " + nowString + ".xlsx";
        try {
            log.info("Creating file \"{}\"", extendedFileName);
            excelFileService.saveToFile(workBook, extendedFileName);
            log.info("File \"{}\" created", extendedFileName);
        } catch (IOException ioException) {
            log.error("Failed to save file \"" + extendedFileName + "\"", ioException);
        }
    }

    @NotNull
    private ExtendedWorkbook createWorkBook() {
        final ExtendedWorkbook workbook = new ExtendedWorkbook(new XSSFWorkbook());

        createCellStyles(workbook);

        return workbook;
    }

    private void createCellStyles(final ExtendedWorkbook workbook) {
        workbook.createCellStyle(ExtendedWorkbook.CellStylesNames.STRING);
        workbook.createCellStyle(ExtendedWorkbook.CellStylesNames.NUMERIC);

        final CellStyle dateTimeCellStyle = workbook.createCellStyle(ExtendedWorkbook.CellStylesNames.DATE_TIME);
        dateTimeCellStyle.setDataFormat(workbook.createDataFormat().getFormat(DateUtils.DATE_TIME_FORMAT));

        final CellStyle percentCellStyle = workbook.createCellStyle(ExtendedWorkbook.CellStylesNames.PERCENT);
        percentCellStyle.setDataFormat(workbook.createDataFormat().getFormat(PERCENT_FORMAT));
    }

    private void createSheet(final ExtendedWorkbook workbook, final String ticker, final SimulationResult result) {
        final ExtendedSheet sheet = (ExtendedSheet) workbook.createSheet();

        putTradingConfig(sheet, result.getTradingConfig());
        sheet.addRow();

        putCommonStatistics(sheet, ticker, result);
        sheet.addRow();

        putPositions(sheet, result.getPositions());
        sheet.addRow();

        putOperations(sheet, result.getOperations());

        sheet.autoSizeColumns();

        putChartWithOperations(sheet, result.getCandles(), result.getOperations());
    }

    private void createSheet(
            final ExtendedWorkbook workbook,
            final String ticker,
            final Interval interval,
            final GetCandlesResponse response
    ) {
        final ExtendedSheet sheet = (ExtendedSheet) workbook.createSheet(ticker);

        putTicker(sheet, ticker);
        putInterval(sheet, interval);
        putCandles(sheet, response.getCandles());

        sheet.autoSizeColumns();

        putChartWithAverages(sheet, response);
    }

    private void putTradingConfig(ExtendedSheet sheet, TradingConfig tradingConfig) {
        final ExtendedRow labelRow = sheet.addRow();
        labelRow.createUnitedCell("Конфигурация", 2);

        putCandleResolution(sheet, tradingConfig.getCandleResolution());
        putStrategyType(sheet, tradingConfig.getStrategyType());
        putStrategyParams(sheet, tradingConfig.getStrategyParams());
    }

    private void putCandleResolution(ExtendedSheet sheet, CandleResolution candleResolution) {
        final ExtendedRow row = sheet.addRow();
        row.createCells("Размер свечи", candleResolution.getValue());
    }

    private void putStrategyType(ExtendedSheet sheet, StrategyType strategyType) {
        final ExtendedRow row = sheet.addRow();
        row.createCells("Стратегия", strategyType.toString());
    }

    private void putStrategyParams(ExtendedSheet sheet, Map<String, Object> strategyParams) {
        for (final Map.Entry<String, Object> entry : strategyParams.entrySet()) {
            final ExtendedRow row = sheet.addRow();
            row.createCells(entry.getKey(), entry.getValue());
        }
    }

    private void putCommonStatistics(final ExtendedSheet sheet, final String ticker, final SimulationResult result) {
        final ExtendedRow labelRow = sheet.addRow();
        labelRow.createUnitedCell("Общая статистика", 2);

        putTicker(sheet, ticker);
        putInterval(sheet, result.getInterval());
        putInitialBalance(sheet, result.getInitialBalance());
        putTotalInvestment(sheet, result.getTotalInvestment());
        putFinalTotalBalance(sheet, result.getFinalTotalBalance());
        putFinalBalance(sheet, result.getFinalBalance());

        putWeightedAverageInvestment(sheet, result.getWeightedAverageInvestment());
        putAbsoluteProfit(sheet, result.getAbsoluteProfit());
        putRelativeProfit(sheet, result.getRelativeProfit());
        putRelativeYearProfit(sheet, result.getRelativeYearProfit());
        putError(sheet, result.getError());
    }

    private void putTicker(final ExtendedSheet sheet, final String ticker) {
        final ExtendedRow row = sheet.addRow();
        row.createCells("Тикер", ticker);
    }

    private void putInterval(final ExtendedSheet sheet, final Interval interval) {
        final ExtendedRow row = sheet.addRow();
        row.createCells("Интервал", interval.toPrettyString());
    }

    private void putInitialBalance(final ExtendedSheet sheet, final BigDecimal initialBalance) {
        final ExtendedRow row = sheet.addRow();
        row.createCells("Начальный баланс", initialBalance);
    }

    private void putTotalInvestment(final ExtendedSheet sheet, final BigDecimal totalInvestment) {
        final ExtendedRow row = sheet.addRow();
        row.createCells("Вложения", totalInvestment);
    }

    private void putFinalTotalBalance(final ExtendedSheet sheet, final BigDecimal totalBalance) {
        final ExtendedRow row = sheet.addRow();
        row.createCells("Итоговый общий баланс", totalBalance);
    }

    private void putWeightedAverageInvestment(final ExtendedSheet sheet, final BigDecimal weightedAverageInvestment) {
        final ExtendedRow row = sheet.addRow();
        row.createCells("Средневзвешенные вложения", weightedAverageInvestment);
    }

    private void putFinalBalance(final ExtendedSheet sheet, final BigDecimal currencyBalance) {
        final ExtendedRow row = sheet.addRow();
        row.createCells("Итоговый валютный баланс", currencyBalance);
    }

    private void putAbsoluteProfit(final ExtendedSheet sheet, final BigDecimal absoluteProfit) {
        final ExtendedRow row = sheet.addRow();
        row.createCells("Абсолютный доход", absoluteProfit);
    }

    private void putRelativeProfit(final ExtendedSheet sheet, final double relativeProfit) {
        final ExtendedRow row = sheet.addRow();
        final List<ExtendedCell> cells = row.createCells("Относительный доход", relativeProfit);
        final ExtendedWorkbook workbook = (ExtendedWorkbook) sheet.getWorkbook();
        final CellStyle style = workbook.getOrCreateCellStyle(ExtendedWorkbook.CellStylesNames.PERCENT);
        cells.get(1).setCellStyle(style);
    }

    private void putRelativeYearProfit(final ExtendedSheet sheet, final double relativeYearProfit) {
        final ExtendedRow row = sheet.addRow();
        final List<ExtendedCell> cells = row.createCells("Относительный годовой доход", relativeYearProfit);
        final ExtendedWorkbook workbook = (ExtendedWorkbook) sheet.getWorkbook();
        final CellStyle style = workbook.getOrCreateCellStyle(ExtendedWorkbook.CellStylesNames.PERCENT);
        cells.get(1).setCellStyle(style);
    }

    private void putError(final ExtendedSheet sheet, final String error) {
        if (StringUtils.hasLength(error)) {
            final ExtendedRow row = sheet.addRow();
            row.createCells("Текст ошибки", error);
        }
    }

    private void putPositions(final ExtendedSheet sheet, final List<SimulatedPosition> positions) {
        final ExtendedRow labelRow = sheet.addRow();

        if (CollectionUtils.isEmpty(positions)) {
            labelRow.createCells("Позиции отсутствуют");
        } else {
            labelRow.createUnitedCell("Позиции", 3);
            final ExtendedRow headersRow = sheet.addRow();
            headersRow.createCells("Цена", "Количество");
            for (final SimulatedPosition position : positions) {
                final ExtendedRow row = sheet.addRow();
                row.createCells(position.getPrice(), position.getQuantity());
            }
        }
    }

    private void putOperations(final ExtendedSheet sheet, final List<SimulatedOperation> operations) {
        final ExtendedRow labelRow = sheet.addRow();

        if (CollectionUtils.isEmpty(operations)) {
            labelRow.createCells("Операции отсутствуют");
        } else {
            labelRow.createUnitedCell("Операции", 6);
            final ExtendedRow headersRow = sheet.addRow();
            headersRow.createCells("Дата и время", "Тип операции", "Цена", "Количество", "Комиссия");
            for (final SimulatedOperation operation : operations) {
                final ExtendedRow row = sheet.addRow();
                row.createCells(operation.getDateTime(),
                        operation.getOperationType().name(),
                        operation.getPrice(),
                        operation.getQuantity(),
                        operation.getCommission());
            }
        }
    }

    private void putCandles(final ExtendedSheet sheet, final List<Candle> candles) {
        sheet.addRow();
        sheet.addRow().createUnitedCell("Свечи", 6);
        sheet.addRow().createCells(
                "Дата-время",
                "Цена открытия",
                "Цена закрытия",
                "Набольшая цена",
                "Наименьшая цена"
        );

        for (final Candle candle : candles) {
            putCandle(sheet, candle);
        }
    }

    private void putCandle(final ExtendedSheet sheet, final Candle candle) {
        final ExtendedRow row = sheet.addRow();
        row.createCells(
                candle.getTime(),
                candle.getOpenPrice(),
                candle.getClosePrice(),
                candle.getHighestPrice(),
                candle.getLowestPrice()
        );
    }

    private void putChartWithAverages(final ExtendedSheet sheet, final GetCandlesResponse response) {
        final ExtendedChart chart = createChart(sheet);
        final ExtendedChartData chartData =
                chart.createChartData(AxisPosition.BOTTOM, AxisPosition.LEFT, ChartTypes.LINE);
        addCandles(chartData, response);
        chart.plot(chartData);
    }

    private void putChartWithOperations(
            final ExtendedSheet sheet,
            final List<Candle> candles,
            final List<SimulatedOperation> operations
    ) {
        if (CollectionUtils.isNotEmpty(candles)) {
            final ExtendedChart chart = createChart(sheet);
            final ExtendedChartData chartData =
                    chart.createChartData(AxisPosition.BOTTOM, AxisPosition.LEFT, ChartTypes.LINE);
            addCandlesAndPricesAndOperations(chartData, candles, operations);
            chart.plot(chartData);
        }
    }

    private ExtendedChart createChart(final ExtendedSheet sheet) {
        final int column1 = sheet.getColumnsCount() + 1;
        final int row1 = 0;
        final int column2 = column1 + CHART_WIDTH;
        final int row2 = row1 + CHART_HEIGHT;
        return sheet.createChart(column1, row1, column2, row2);
    }

    private void addCandles(final ExtendedChartData chartData, final GetCandlesResponse response) {
        final List<OffsetDateTime> times = response.getCandles().stream()
                .map(Candle::getTime)
                .collect(Collectors.toList());
        final XDDFCategoryDataSource timesDataSource = getTimesCategoryDataSourceFromTimes(times);
        addOpenPrices(chartData, timesDataSource, response.getCandles());
        addLine(chartData, timesDataSource, response.getShortAverages(), MarkerProperties.NO_MARKER, Color.BLUE);
        addLine(chartData, timesDataSource, response.getLongAverages(), MarkerProperties.NO_MARKER, Color.YELLOW);

        chartData.stretchChart();
    }

    private void addCandlesAndPricesAndOperations(
            final ExtendedChartData chartData,
            final List<Candle> candles,
            final List<SimulatedOperation> operations
    ) {
        final List<Candle> innerCandles = new ArrayList<>(candles);

        // interpolating candles and computing operationsIndices for future processing
        final List<Integer> operationsIndices = new ArrayList<>();
        for (final SimulatedOperation operation : operations) {
            final OffsetDateTime operationDateTime = operation.getDateTime();
            final Candle keyCandle = new Candle();
            keyCandle.setTime(operationDateTime);
            int index = Collections.binarySearch(innerCandles, keyCandle, Comparator.comparing(Candle::getTime));
            if (index < 0) {
                index = -index - 1;
                CollectionsUtils.insertInterpolated(innerCandles, index, Candle::createAverage);
            }

            operationsIndices.add(index);
        }

        final XDDFCategoryDataSource timesDataSource = getTimesCategoryDataSourceFromCandles(innerCandles);
        addOpenPrices(chartData, timesDataSource, innerCandles);
        addOperations(chartData, timesDataSource, operations, operationsIndices);

        chartData.stretchChart();
    }

    private XDDFCategoryDataSource getTimesCategoryDataSourceFromTimes(final List<OffsetDateTime> times) {
        final String[] timesArray = times.stream()
                .map(DateUtils.DATE_TIME_FORMATTER::format)
                .toArray(String[]::new);
        return XDDFDataSourcesFactory.fromArray(timesArray);
    }

    private XDDFCategoryDataSource getTimesCategoryDataSourceFromCandles(final List<Candle> innerCandles) {
        final String[] times = innerCandles.stream()
                .map(Candle::getTime)
                .map(DateUtils.DATE_TIME_FORMATTER::format)
                .toArray(String[]::new);
        return XDDFDataSourcesFactory.fromArray(times);
    }

    private void addOpenPrices(
            final ExtendedChartData chartData,
            final XDDFCategoryDataSource timesDataSource,
            final List<Candle> candles
    ) {
        final BigDecimal[] prices = candles.stream()
                .map(Candle::getOpenPrice)
                .toArray(BigDecimal[]::new);
        addSeries(chartData, timesDataSource, prices, MarkerProperties.NO_MARKER);
    }

    private void addLine(
            final ExtendedChartData chartData,
            final XDDFCategoryDataSource timesDataSource,
            final List<BigDecimal> values,
            final MarkerProperties markerProperties,
            final Color seriesColor
    ) {
        if (values.stream().anyMatch(Objects::nonNull)) {
            addSeries(chartData, timesDataSource, values.toArray(new BigDecimal[0]), markerProperties, seriesColor);
        }
    }

    @Nullable
    private BigDecimal getValueAtTime(final List<Point> points, final OffsetDateTime time) {
        return points.stream()
                .filter(extreme -> extreme.getTime().equals(time))
                .findFirst()
                .map(Point::getValue)
                .orElse(null);
    }

    private void addOperations(
            final ExtendedChartData chartData,
            final XDDFCategoryDataSource timesDataSource,
            final List<SimulatedOperation> operations,
            final List<Integer> operationsIndices
    ) {

        final BigDecimal[] buyOperationsPrices = new BigDecimal[timesDataSource.getPointCount()];
        final BigDecimal[] sellOperationsPrices = new BigDecimal[timesDataSource.getPointCount()];
        boolean buyOperationsExist = false;
        boolean sellOperationsExist = false;
        for (int i = 0; i < operations.size(); i++) {
            final SimulatedOperation operation = operations.get(i);
            final int index = operationsIndices.get(i);
            if (operation.getOperationType() == OperationType.BUY) {
                buyOperationsPrices[index] = operation.getPrice();
                buyOperationsExist = true;
            } else {
                sellOperationsPrices[index] = operation.getPrice();
                sellOperationsExist = true;
            }
        }

        if (buyOperationsExist) { // apache.poi fails when no values in dataSource
            addSeries(chartData, timesDataSource, buyOperationsPrices, BUY_OPERATION_MARKER_PROPERTIES);
        }
        if (sellOperationsExist) {
            addSeries(chartData, timesDataSource, sellOperationsPrices, SELL_OPERATION_MARKER_PROPERTIES);
        }
    }

    private void addSeries(
            final ExtendedChartData chartData,
            final XDDFCategoryDataSource timesDataSource,
            final BigDecimal[] numbers,
            final MarkerProperties markerProperties
    ) {
        addSeries(chartData, timesDataSource, numbers, markerProperties, null);
    }

    private void addSeries(
            final ExtendedChartData chartData,
            final XDDFCategoryDataSource timesDataSource,
            final BigDecimal[] numbers,
            final MarkerProperties markerProperties,
            final Color seriesColor
    ) {
        final XDDFNumericalDataSource<Number> numericalDataSource = XDDFDataSourcesFactory.fromArray(numbers);
        chartData.addSeries(timesDataSource, numericalDataSource, markerProperties, seriesColor);
    }
}