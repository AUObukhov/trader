package ru.obukhov.trader.common.service.impl;

import lombok.RequiredArgsConstructor;
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
import org.springframework.stereotype.Service;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.common.model.poi.ExtendedCell;
import ru.obukhov.trader.common.model.poi.ExtendedChart;
import ru.obukhov.trader.common.model.poi.ExtendedChartData;
import ru.obukhov.trader.common.model.poi.ExtendedRow;
import ru.obukhov.trader.common.model.poi.ExtendedSheet;
import ru.obukhov.trader.common.model.poi.ExtendedWorkbook;
import ru.obukhov.trader.common.service.interfaces.ExcelFileService;
import ru.obukhov.trader.common.service.interfaces.ExcelService;
import ru.obukhov.trader.common.util.CollectionsUtils;
import ru.obukhov.trader.market.model.Candle;
import ru.obukhov.trader.web.model.pojo.SimulatedOperation;
import ru.obukhov.trader.web.model.pojo.SimulatedPosition;
import ru.obukhov.trader.web.model.pojo.SimulationResult;
import ru.tinkoff.invest.openapi.models.operations.OperationType;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExcelServiceImpl implements ExcelService {

    private static final String DATE_TIME_FORMAT = "dd.MM.yyyy HH:mm:ss";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT);
    private static final String PERCENT_FORMAT = "0.00%";

    private static final int CHART_WIDTH = 20;
    private static final int CHART_HEIGHT = 40;
    private static final short OPERATION_MARKER_SIZE = (short) 10;

    private final ExcelFileService excelFileService;

    @Override
    public void saveSimulationResults(String ticker, Collection<SimulationResult> results) {
        ExtendedWorkbook workBook = createWorkBook();
        for (SimulationResult result : results) {
            createSheet(workBook, ticker, result);
        }

        excelFileService.saveToFile(workBook, "SimulationResult for '" + ticker + "'");
    }

    @Override
    public void saveCandles(String ticker, Interval interval, List<Candle> candles) {
        ExtendedWorkbook workBook = createWorkBook();
        createSheet(workBook, ticker, interval, candles);

        excelFileService.saveToFile(workBook, "Candles for '" + ticker + "'");
    }

    @NotNull
    private ExtendedWorkbook createWorkBook() {
        ExtendedWorkbook workbook = new ExtendedWorkbook(new XSSFWorkbook());

        createCellStyles(workbook);

        return workbook;
    }

    private void createCellStyles(ExtendedWorkbook workbook) {
        workbook.createCellStyle(ExtendedWorkbook.CellStylesNames.STRING);
        workbook.createCellStyle(ExtendedWorkbook.CellStylesNames.NUMERIC);

        CellStyle dateTimeCellStyle = workbook.createCellStyle(ExtendedWorkbook.CellStylesNames.DATE_TIME);
        dateTimeCellStyle.setDataFormat(workbook.createDataFormat().getFormat(DATE_TIME_FORMAT));

        CellStyle percentCellStyle = workbook.createCellStyle(ExtendedWorkbook.CellStylesNames.PERCENT);
        percentCellStyle.setDataFormat(workbook.createDataFormat().getFormat(PERCENT_FORMAT));
    }

    private void createSheet(ExtendedWorkbook workbook, String ticker, SimulationResult result) {
        ExtendedSheet sheet = (ExtendedSheet) workbook.createSheet(result.getBotName());

        putCommonStatistics(sheet, ticker, result);
        putPositions(sheet, result.getPositions());
        putOperations(sheet, result.getOperations());

        sheet.autoSizeColumns();

        putChart(sheet, result.getCandles(), result.getOperations());
    }

    private void createSheet(ExtendedWorkbook workbook, String ticker, Interval interval, List<Candle> candles) {
        ExtendedSheet sheet = (ExtendedSheet) workbook.createSheet(ticker);

        putTicker(sheet, ticker);
        putInterval(sheet, interval);
        putCandles(sheet, candles);

        sheet.autoSizeColumns();

        putChart(sheet, candles);
    }

    private void putCommonStatistics(ExtendedSheet sheet, String ticker, SimulationResult result) {
        ExtendedRow labelRow = sheet.addRow();
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
    }

    private void putTicker(ExtendedSheet sheet, String ticker) {
        ExtendedRow row = sheet.addRow();
        row.createCells("Тикер", ticker);
    }

    private void putInterval(ExtendedSheet sheet, Interval interval) {
        ExtendedRow row = sheet.addRow();
        row.createCells("Интервал", interval.toPrettyString());
    }

    private void putInitialBalance(ExtendedSheet sheet, BigDecimal initialBalance) {
        ExtendedRow row = sheet.addRow();
        row.createCells("Начальный баланс", initialBalance);
    }

    private void putTotalInvestment(ExtendedSheet sheet, BigDecimal totalInvestment) {
        ExtendedRow row = sheet.addRow();
        row.createCells("Вложения", totalInvestment);
    }

    private void putFinalTotalBalance(ExtendedSheet sheet, BigDecimal totalBalance) {
        ExtendedRow row = sheet.addRow();
        row.createCells("Итоговый общий баланс", totalBalance);
    }

    private void putWeightedAverageInvestment(ExtendedSheet sheet, BigDecimal weightedAverageInvestment) {
        ExtendedRow row = sheet.addRow();
        row.createCells("Средневзвешенные вложения", weightedAverageInvestment);
    }

    private void putFinalBalance(ExtendedSheet sheet, BigDecimal currencyBalance) {
        ExtendedRow row = sheet.addRow();
        row.createCells("Итоговый валютный баланс", currencyBalance);
    }

    private void putAbsoluteProfit(ExtendedSheet sheet, BigDecimal absoluteProfit) {
        ExtendedRow row = sheet.addRow();
        row.createCells("Абсолютный доход", absoluteProfit);
    }

    private void putRelativeProfit(ExtendedSheet sheet, double relativeProfit) {
        ExtendedRow row = sheet.addRow();
        List<ExtendedCell> cells = row.createCells("Относительный доход", relativeProfit);
        ExtendedWorkbook workbook = (ExtendedWorkbook) sheet.getWorkbook();
        CellStyle style = workbook.getOrCreateCellStyle(ExtendedWorkbook.CellStylesNames.PERCENT);
        cells.get(1).setCellStyle(style);
    }

    private void putRelativeYearProfit(ExtendedSheet sheet, double relativeYearProfit) {
        ExtendedRow row = sheet.addRow();
        List<ExtendedCell> cells = row.createCells("Относительный годовой доход", relativeYearProfit);
        ExtendedWorkbook workbook = (ExtendedWorkbook) sheet.getWorkbook();
        CellStyle style = workbook.getOrCreateCellStyle(ExtendedWorkbook.CellStylesNames.PERCENT);
        cells.get(1).setCellStyle(style);
    }

    private void putPositions(ExtendedSheet sheet, List<SimulatedPosition> positions) {
        sheet.addRow();
        ExtendedRow labelRow = sheet.addRow();

        if (CollectionUtils.isEmpty(positions)) {
            labelRow.createCells("Позиции отсутствуют");
        } else {
            labelRow.createUnitedCell("Позиции", 3);
            ExtendedRow headersRow = sheet.addRow();
            headersRow.createCells("Цена", "Количество");
            for (SimulatedPosition position : positions) {
                ExtendedRow row = sheet.addRow();
                row.createCells(position.getPrice(), position.getQuantity());
            }
        }
    }

    private void putOperations(ExtendedSheet sheet, List<SimulatedOperation> operations) {
        sheet.addRow();
        ExtendedRow labelRow = sheet.addRow();

        if (CollectionUtils.isEmpty(operations)) {
            labelRow.createCells("Операции отсутствуют");
        } else {
            labelRow.createUnitedCell("Операции", 6);
            ExtendedRow headersRow = sheet.addRow();
            headersRow.createCells("Дата и время", "Тип операции", "Цена", "Количество", "Комиссия");
            for (SimulatedOperation operation : operations) {
                ExtendedRow row = sheet.addRow();
                row.createCells(operation.getDateTime(),
                        operation.getOperationType().name(),
                        operation.getPrice(),
                        operation.getQuantity(),
                        operation.getCommission());
            }
        }
    }

    private void putCandles(ExtendedSheet sheet, List<Candle> candles) {
        sheet.addRow();
        sheet.addRow().createUnitedCell("Свечи", 5);
        sheet.addRow().createCells(
                "Дата-время", "Цена открытия", "Цена закрытия", "Набольшая цена", "Наименьшая цена");

        for (Candle candle : candles) {
            putCandle(sheet, candle);
        }
    }

    private void putCandle(ExtendedSheet sheet, Candle candle) {
        ExtendedRow row = sheet.addRow();
        row.createCells(
                candle.getTime(),
                candle.getOpenPrice(),
                candle.getClosePrice(),
                candle.getHighestPrice(),
                candle.getLowestPrice()
        );

    }

    private void putChart(ExtendedSheet sheet, List<Candle> candles) {
        ExtendedChart chart = createChart(sheet);
        ExtendedChartData chartData = chart.createChartData(AxisPosition.BOTTOM, AxisPosition.LEFT, ChartTypes.LINE);
        addCandles(chartData, candles);
        chart.plot(chartData);
    }

    private void putChart(ExtendedSheet sheet, List<Candle> candles, List<SimulatedOperation> operations) {
        ExtendedChart chart = createChart(sheet);
        ExtendedChartData chartData = chart.createChartData(AxisPosition.BOTTOM, AxisPosition.LEFT, ChartTypes.LINE);
        addCandlesAndPricesAndOperations(chartData, candles, operations);
        chart.plot(chartData);
    }

    private ExtendedChart createChart(ExtendedSheet sheet) {
        int column1 = sheet.getColumnsCount() + 1;
        int row1 = 0;
        int column2 = column1 + CHART_WIDTH;
        int row2 = row1 + CHART_HEIGHT;
        return sheet.createChart(column1, row1, column2, row2);
    }

    private void addCandles(ExtendedChartData chartData, List<Candle> candles) {
        XDDFCategoryDataSource timesDataSource = getTimesCategoryDataSource(candles);
        addOpenPrices(chartData, timesDataSource, candles);

        chartData.stretchChart();
    }

    private void addCandlesAndPricesAndOperations(ExtendedChartData chartData,
                                                  List<Candle> candles,
                                                  List<SimulatedOperation> operations) {
        List<Candle> innerCandles = new ArrayList<>(candles);

        // interpolating candles and computing operationsIndices for future processing
        List<Integer> operationsIndices = new ArrayList<>();
        for (SimulatedOperation operation : operations) {
            OffsetDateTime operationDateTime = operation.getDateTime();
            Candle keyCandle = Candle.builder().time(operationDateTime).build();
            int index = Collections.binarySearch(innerCandles, keyCandle, Comparator.comparing(Candle::getTime));
            if (index < 0) {
                index = -index - 1;
                CollectionsUtils.insertInterpolated(innerCandles, index, Candle::createAverage);
            }

            operationsIndices.add(index);
        }

        XDDFCategoryDataSource timesDataSource = getTimesCategoryDataSource(innerCandles);
        addOpenPrices(chartData, timesDataSource, innerCandles);
        addOperations(chartData, timesDataSource, operations, operationsIndices);

        chartData.stretchChart();
    }

    private XDDFCategoryDataSource getTimesCategoryDataSource(List<Candle> innerCandles) {
        String[] times = innerCandles.stream()
                .map(Candle::getTime)
                .map(DATE_TIME_FORMATTER::format)
                .toArray(String[]::new);
        return XDDFDataSourcesFactory.fromArray(times);
    }

    private void addOpenPrices(ExtendedChartData chartData,
                               XDDFCategoryDataSource timesDataSource,
                               List<Candle> candles) {
        BigDecimal[] prices = candles.stream()
                .map(Candle::getOpenPrice)
                .toArray(BigDecimal[]::new);
        addSeries(chartData, timesDataSource, prices, MarkerStyle.NONE);
    }

    private void addOperations(ExtendedChartData chartData,
                               XDDFCategoryDataSource timesDataSource,
                               List<SimulatedOperation> operations,
                               List<Integer> operationsIndices) {

        BigDecimal[] buyOperationsPrices = new BigDecimal[timesDataSource.getPointCount()];
        BigDecimal[] sellOperationsPrices = new BigDecimal[timesDataSource.getPointCount()];
        boolean buyOperationsExist = false;
        boolean sellOperationsExist = false;
        for (int i = 0; i < operations.size(); i++) {
            SimulatedOperation operation = operations.get(i);
            int index = operationsIndices.get(i);
            if (operation.getOperationType() == OperationType.Buy) {
                buyOperationsPrices[index] = operation.getPrice();
                buyOperationsExist = true;
            } else {
                sellOperationsPrices[index] = operation.getPrice();
                sellOperationsExist = true;
            }
        }

        if (buyOperationsExist) { // apache.poi fails when no values in dataSource
            addSeries(chartData, timesDataSource, buyOperationsPrices, MarkerStyle.TRIANGLE);
        }
        if (sellOperationsExist) {
            addSeries(chartData, timesDataSource, sellOperationsPrices, MarkerStyle.DIAMOND);
        }
    }

    private void addSeries(ExtendedChartData chartData,
                           XDDFCategoryDataSource timesDataSource,
                           BigDecimal[] numbers,
                           MarkerStyle markerStyle) {
        XDDFNumericalDataSource<Number> numericalDataSource = XDDFDataSourcesFactory.fromArray(numbers);
        chartData.addSeries(timesDataSource, numericalDataSource, OPERATION_MARKER_SIZE, markerStyle);
    }
}