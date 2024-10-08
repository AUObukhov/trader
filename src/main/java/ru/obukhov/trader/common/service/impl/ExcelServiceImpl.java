package ru.obukhov.trader.common.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xddf.usermodel.chart.AxisPosition;
import org.apache.poi.xddf.usermodel.chart.ChartTypes;
import org.apache.poi.xddf.usermodel.chart.MarkerStyle;
import org.apache.poi.xddf.usermodel.chart.XDDFCategoryDataSource;
import org.apache.poi.xddf.usermodel.chart.XDDFDataSourcesFactory;
import org.apache.poi.xddf.usermodel.chart.XDDFNumericalDataSource;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import ru.obukhov.trader.common.model.Interval;
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
import ru.obukhov.trader.common.util.DecimalUtils;
import ru.obukhov.trader.common.util.TimestampUtils;
import ru.obukhov.trader.market.model.Candle;
import ru.obukhov.trader.trading.model.BackTestResult;
import ru.obukhov.trader.trading.model.Balances;
import ru.obukhov.trader.trading.model.Profits;
import ru.obukhov.trader.trading.model.StrategyType;
import ru.obukhov.trader.web.model.BotConfig;
import ru.obukhov.trader.web.model.exchange.GetCandlesResponse;
import ru.obukhov.trader.web.model.exchange.WeightedShare;
import ru.tinkoff.piapi.contract.v1.CandleInterval;
import ru.tinkoff.piapi.contract.v1.MoneyValue;
import ru.tinkoff.piapi.contract.v1.Operation;
import ru.tinkoff.piapi.contract.v1.OperationType;
import ru.tinkoff.piapi.core.models.Position;

import java.awt.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExcelServiceImpl implements ExcelService {

    private static final String PERCENT_FORMAT = "0.00%";

    private static final int CHART_WIDTH = 20;
    private static final int CHART_HEIGHT = 40;
    private static final short MARKER_SIZE = (short) 10;

    private static final MarkerProperties SELL_OPERATION_MARKER_PROPERTIES = new MarkerProperties(MARKER_SIZE, MarkerStyle.DIAMOND, Color.GREEN);
    private static final MarkerProperties BUY_OPERATION_MARKER_PROPERTIES = new MarkerProperties(MARKER_SIZE, MarkerStyle.TRIANGLE, Color.RED);

    private final ExcelFileService excelFileService;

    @Override
    public void saveBackTestResults(final Collection<BackTestResult> results) {
        for (final BackTestResult result : results) {
            final ExtendedWorkbook workBook = createWorkBook();
            createSheet(workBook, result);
            saveToFile(workBook, "BackTestResult");
        }
    }

    @Override
    public void saveCandles(final String figi, final Interval interval, final GetCandlesResponse response) {
        final ExtendedWorkbook workBook = createWorkBook();
        createSheet(workBook, figi, interval, response);
        saveToFile(workBook, "Candles for FIGI '" + figi + "'");
    }

    @Override
    public void saveWeightedShares(final List<WeightedShare> weightedShares) {
        final ExtendedWorkbook workBook = createWorkBook();
        createSheet(workBook, weightedShares);
        saveToFile(workBook, "Weighted shares");
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

    private void createSheet(final ExtendedWorkbook workbook, final BackTestResult result) {
        final ExtendedSheet sheet = (ExtendedSheet) workbook.createSheet();

        putBotConfig(sheet, result.botConfig());
        sheet.addRow();

        putCommonStatistics(sheet, result);
        sheet.addRow();

        putPositions(sheet, result.positions());
        sheet.addRow();

        putOperations(sheet, result.operations());

        sheet.autoSizeColumns();

        putChartWithOperations(sheet, result.candles(), result.operations());
    }

    private void createSheet(final ExtendedWorkbook workbook, final String figi, final Interval interval, final GetCandlesResponse response) {
        final ExtendedSheet sheet = (ExtendedSheet) workbook.createSheet(figi);

        putFigi(sheet, figi);
        putInterval(sheet, interval);
        putCandles(sheet, response.getCandles());

        sheet.autoSizeColumns();

        putChartWithAverages(sheet, response);
    }

    private void createSheet(final ExtendedWorkbook workbook, final List<WeightedShare> weightedShares) {
        final ExtendedSheet sheet = (ExtendedSheet) workbook.createSheet();

        final int tickerColumn = 0;
        final int nameColumn = 1;
        final int priceRubColumn = 2;
        final int capitalizationWeightColumn = 3;
        final int lotColumn = 4;
        final int portfolioLotQuantityColumn = 5;
        final int lotPriceColumn = 6;
        final int portfolioSharesQuantityColumn = 7;
        final int totalPriceRubColumn = 8;
        final int portfolioWeightColumn = 9;
        final int needToBuyColumn = 10;

        final String tickerHeader = "тикер";
        final String nameHeader = "название";
        final String priceRubHeader = "цена";
        final String capitalizationWeightHeader = "вес в индексе";
        final String lotHeader = "размер лота";
        final String portfolioLotsHeader = "лотов в портфеле";
        final String lotPriceRubHeader = "стоимость лота";
        final String shareQuantityHeader = "акций в портфеле";
        final String totalPriceRubHeader = "стоимость в портфеле";
        final String portfolioWeightHeader = "вес в портфеле";
        final String needToBuyHeader = "надо докупить, %";

        final ExtendedRow headerRow = sheet.addRow();
        headerRow.createCell(tickerColumn, tickerHeader);
        headerRow.createCell(nameColumn, nameHeader);
        headerRow.createCell(priceRubColumn, priceRubHeader);
        headerRow.createCell(capitalizationWeightColumn, capitalizationWeightHeader);
        headerRow.createCell(lotColumn, lotHeader);
        headerRow.createCell(portfolioLotQuantityColumn, portfolioLotsHeader);
        headerRow.createCell(lotPriceColumn, lotPriceRubHeader);
        headerRow.createCell(portfolioSharesQuantityColumn, shareQuantityHeader);
        headerRow.createCell(totalPriceRubColumn, totalPriceRubHeader);
        headerRow.createCell(portfolioWeightColumn, portfolioWeightHeader);
        headerRow.createCell(needToBuyColumn, needToBuyHeader);

        String percentCellStyle = ExtendedWorkbook.CellStylesNames.PERCENT;
        for (int i = 0; i < weightedShares.size(); i++) {
            final int index = i + 2;
            final WeightedShare weightedShare = weightedShares.get(i);

            final String priceCell = CellReference.convertNumToColString(priceRubColumn) + index;
            final String lotCell = CellReference.convertNumToColString(lotColumn) + index;
            final String portfolioLotQuantityCell =
                    CellReference.convertNumToColString(portfolioLotQuantityColumn) + index;
            final String portfolioSharesQuantityCell =
                    CellReference.convertNumToColString(portfolioSharesQuantityColumn) + index;
            final String totalPriceRubCell = CellReference.convertNumToColString(totalPriceRubColumn) + index;
            final String totalPriceRubSumCell =
                    CellReference.convertNumToColString(totalPriceRubColumn) + "$" + (weightedShares.size() + 2);
            final String capitalizationWeightCell =
                    CellReference.convertNumToColString(capitalizationWeightColumn) + index;
            final String portfolioWeightCell = CellReference.convertNumToColString(portfolioWeightColumn) + index;

            final ExtendedRow row = sheet.addRow();
            row.createCell(tickerColumn, weightedShare.getTicker());
            row.createCell(nameColumn, weightedShare.getName());
            row.createCell(priceRubColumn, weightedShare.getPriceRub());
            row.createCell(capitalizationWeightColumn, weightedShare.getCapitalizationWeight(), percentCellStyle);
            row.createCell(lotColumn, weightedShare.getLot());

            final int portfolioLotQuantity = weightedShare.getPortfolioSharesQuantity() / weightedShare.getLot();
            row.createCell(portfolioLotQuantityColumn, portfolioLotQuantity);

            row.createFormulaCell(lotPriceColumn, priceCell + "*" + lotCell);
            row.createFormulaCell(portfolioSharesQuantityColumn, lotCell + "*" + portfolioLotQuantityCell);
            row.createFormulaCell(totalPriceRubColumn, priceCell + "*" + portfolioSharesQuantityCell);

            final String portfolioWeightFormula = totalPriceRubCell + "/" + totalPriceRubSumCell;
            row.createFormulaCell(portfolioWeightColumn, portfolioWeightFormula, percentCellStyle);

            final String needToBuyFormula = "(" + capitalizationWeightCell + "-" + portfolioWeightCell + ")/"
                    + portfolioWeightCell;
            row.createFormulaCell(needToBuyColumn, needToBuyFormula, percentCellStyle);
        }

        int lastRowIndex = weightedShares.size() + 1;

        final ExtendedRow summaryRow = sheet.addRow();
        summaryRow.createCell(0, "Итого");

        final String indexWeightFormula = "SUM(D2:D" + lastRowIndex + ")";
        summaryRow.createFormulaCell(capitalizationWeightColumn, indexWeightFormula, percentCellStyle);

        summaryRow.createFormulaCell(totalPriceRubColumn, "SUM(I2:I" + lastRowIndex + ")");

        final String portfolioWeightFormula = "SUM(J2:J" + lastRowIndex + ")";
        summaryRow.createFormulaCell(portfolioWeightColumn, portfolioWeightFormula, percentCellStyle);

        sheet.autoSizeColumns();
    }

    private void putBotConfig(final ExtendedSheet sheet, final BotConfig botConfig) {
        sheet.addRow().createUnitedCell("Конфигурация", 2);

        putCandleInterval(sheet, botConfig.candleInterval());
        putStrategyType(sheet, botConfig.strategyType());
        putStrategyParams(sheet, botConfig.strategyParams());
    }

    private void putCandleInterval(final ExtendedSheet sheet, final CandleInterval candleInterval) {
        sheet.addRow("Размер свечи", candleInterval.name());
    }

    private void putStrategyType(final ExtendedSheet sheet, final StrategyType strategyType) {
        sheet.addRow("Стратегия", strategyType.toString());
    }

    private void putStrategyParams(final ExtendedSheet sheet, final Map<String, Object> strategyParams) {
        if (strategyParams.isEmpty()) {
            return;
        }

        sheet.addRow().createUnitedCell("Параметры стратегии", 2);
        for (final Map.Entry<String, Object> entry : strategyParams.entrySet()) {
            sheet.addRow(entry.getKey(), entry.getValue());
        }
    }

    private void putCommonStatistics(final ExtendedSheet sheet, final BackTestResult result) {
        sheet.addRow().createUnitedCell("Общая статистика", 2);

        putAccountId(sheet, result.botConfig().accountId());
        putFigies(sheet, result.botConfig().figies());
        putInterval(sheet, result.interval());
        putBalances(sheet, result);
        putProfits(sheet, result);
        putError(sheet, result.error());
    }

    private void putAccountId(final ExtendedSheet sheet, final String accountId) {
        sheet.addRow("Счёт", accountId);
    }

    private void putFigi(final ExtendedSheet sheet, final String figi) {
        sheet.addRow("FIGI", figi);
    }

    private void putFigies(final ExtendedSheet sheet, final List<String> figies) {
        sheet.addRow("FIGIes", String.join(", ", figies));
    }

    private void putInterval(final ExtendedSheet sheet, final Interval interval) {
        sheet.addRow("Интервал", interval.toPrettyString());
    }

    private void putBalances(final ExtendedSheet sheet, final BackTestResult result) {
        sheet.addRow();
        sheet.addRow().createUnitedCell("Балансы", 2);
        for (final Map.Entry<String, Balances> entry : result.balances().entrySet()) {
            sheet.addRow("Валюта", entry.getKey());
            final Balances balances = entry.getValue();
            putInitialInvestment(sheet, balances.initialInvestment());
            putTotalInvestment(sheet, balances.totalInvestment());
            putFinalTotalSavings(sheet, balances.finalTotalSavings());
            putFinalBalance(sheet, balances.finalBalance());
            putWeightedAverageInvestment(sheet, balances.weightedAverageInvestment());
        }
    }

    private void putInitialInvestment(final ExtendedSheet sheet, final BigDecimal initialInvestment) {
        sheet.addRow("Начальный баланс", initialInvestment);
    }

    private void putTotalInvestment(final ExtendedSheet sheet, final BigDecimal totalInvestment) {
        sheet.addRow("Вложения", totalInvestment);
    }

    private void putFinalTotalSavings(final ExtendedSheet sheet, final BigDecimal totalBalance) {
        sheet.addRow("Итоговый общий баланс", totalBalance);
    }

    private void putFinalBalance(final ExtendedSheet sheet, final BigDecimal currencyBalance) {
        sheet.addRow("Итоговый валютный баланс", currencyBalance);
    }

    private void putWeightedAverageInvestment(final ExtendedSheet sheet, final BigDecimal weightedAverageInvestment) {
        sheet.addRow("Средневзвешенные вложения", weightedAverageInvestment);
    }

    private void putProfits(final ExtendedSheet sheet, final BackTestResult result) {
        sheet.addRow();
        sheet.addRow().createUnitedCell("Доходы", 2);
        for (final Map.Entry<String, Profits> entry : result.profits().entrySet()) {
            sheet.addRow("Валюта", entry.getKey());
            final Profits profits = entry.getValue();
            putAbsoluteProfit(sheet, profits.absolute());
            putRelativeProfit(sheet, profits.relative());
            putRelativeYearProfit(sheet, profits.relativeAnnual());
        }
    }

    private void putAbsoluteProfit(final ExtendedSheet sheet, final BigDecimal absoluteProfit) {
        sheet.addRow("Абсолютный доход", absoluteProfit);
    }

    private void putRelativeProfit(final ExtendedSheet sheet, final double relativeProfit) {
        final List<ExtendedCell> cells = sheet.addRow("Относительный доход", relativeProfit);
        final ExtendedWorkbook workbook = (ExtendedWorkbook) sheet.getWorkbook();
        final CellStyle style = workbook.getOrCreateCellStyle(ExtendedWorkbook.CellStylesNames.PERCENT);
        cells.get(1).setCellStyle(style);
    }

    private void putRelativeYearProfit(final ExtendedSheet sheet, final double relativeYearProfit) {
        final List<ExtendedCell> cells = sheet.addRow("Относительный годовой доход", relativeYearProfit);
        final ExtendedWorkbook workbook = (ExtendedWorkbook) sheet.getWorkbook();
        final CellStyle style = workbook.getOrCreateCellStyle(ExtendedWorkbook.CellStylesNames.PERCENT);
        cells.get(1).setCellStyle(style);
    }

    private void putError(final ExtendedSheet sheet, final String error) {
        if (StringUtils.hasLength(error)) {
            sheet.addRow("Текст ошибки", error);
        }
    }

    private void putPositions(final ExtendedSheet sheet, final List<Position> positions) {
        if (CollectionUtils.isEmpty(positions)) {
            sheet.addRow("Позиции отсутствуют");
        } else {
            sheet.addRow().createUnitedCell("Позиции", 2);
            sheet.addRow("Цена", "Количество");
            for (final Position position : positions) {
                sheet.addRow(position.getCurrentPrice(), position.getQuantity());
            }
        }
    }

    private void putOperations(final ExtendedSheet sheet, final Map<String, List<Operation>> operations) {
        if (operations.isEmpty()) {
            sheet.addRow("Операции отсутствуют");
        } else {
            for (final Map.Entry<String, List<Operation>> entry : operations.entrySet()) {
                sheet.addRow().createUnitedCell("Операции с " + entry.getKey(), 4);
                sheet.addRow("Дата и время", "Тип операции", "Цена", "Количество");
                for (final Operation operation : entry.getValue()) {
                    sheet.addRow(
                            operation.getDate(),
                            operation.getOperationType().name(),
                            operation.getPrice(),
                            operation.getQuantity()
                    );
                }
                sheet.addRow();
            }
        }
    }

    private void putCandles(final ExtendedSheet sheet, final List<Candle> candles) {
        sheet.addRow();
        sheet.addRow().createUnitedCell("Свечи", 6);
        sheet.addRow("Дата-время", "Цена открытия", "Цена закрытия", "Набольшая цена", "Наименьшая цена");

        for (final Candle candle : candles) {
            putCandle(sheet, candle);
        }
    }

    private void putCandle(final ExtendedSheet sheet, final Candle candle) {
        sheet.addRow(candle.getTime(), candle.getOpen(), candle.getClose(), candle.getHigh(), candle.getLow());
    }

    private void putChartWithAverages(final ExtendedSheet sheet, final GetCandlesResponse response) {
        final ExtendedChart chart = createChart(sheet);
        final ExtendedChartData chartData = chart.createChartData(AxisPosition.BOTTOM, AxisPosition.LEFT, ChartTypes.LINE);
        addCandles(chartData, response);
        chart.plot(chartData);
    }

    private void putChartWithOperations(
            final ExtendedSheet sheet,
            final Map<String, List<Candle>> candles,
            final Map<String, List<Operation>> operations
    ) {
        if (candles.entrySet().stream().allMatch(entry -> entry.getValue().isEmpty())) {
            log.debug("No candles found. Skipping chart");
            return;
        }

        final ExtendedChart chart = createChart(sheet);
        final ExtendedChartData chartData = chart.createChartData(AxisPosition.BOTTOM, AxisPosition.LEFT, ChartTypes.LINE);
        for (final Map.Entry<String, List<Candle>> entry : candles.entrySet()) {
            final List<Candle> currentCandles = entry.getValue();
            if (CollectionUtils.isEmpty(currentCandles)) {
                log.debug("No candles found for FIGI {}", entry.getKey());
            } else {
                final List<Operation> currentOperations = operations.get(entry.getKey());
                addCandlesAndPricesAndOperations(chartData, currentCandles, currentOperations);
            }
        }

        chartData.stretchChart();
        chart.plot(chartData);
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
                .toList();
        final XDDFCategoryDataSource timesDataSource = getTimesCategoryDataSourceFromTimes(times);
        addOpens(chartData, timesDataSource, response.getCandles());
        addLine(chartData, timesDataSource, response.getAverages1(), MarkerProperties.NO_MARKER, Color.BLUE);
        addLine(chartData, timesDataSource, response.getAverages2(), MarkerProperties.NO_MARKER, Color.YELLOW);

        chartData.stretchChart();
    }

    private void addCandlesAndPricesAndOperations(
            final ExtendedChartData chartData,
            final List<Candle> candles,
            final List<Operation> operations
    ) {
        final List<Candle> innerCandles = new ArrayList<>(candles);

        // interpolating candles and computing operationsIndices for future processing
        final List<Integer> operationsIndices = new ArrayList<>();
        for (final Operation operation : operations) {
            final OffsetDateTime operationDateTime = TimestampUtils.toOffsetDateTime(operation.getDate());
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
        addOpens(chartData, timesDataSource, innerCandles);
        addOperations(chartData, timesDataSource, operations, operationsIndices);
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

    private void addOpens(final ExtendedChartData chartData, final XDDFCategoryDataSource timesDataSource, final List<Candle> candles) {
        final BigDecimal[] prices = candles.stream()
                .map(Candle::getOpen)
                .toArray(BigDecimal[]::new);
        addSeries(chartData, timesDataSource, prices, MarkerProperties.NO_MARKER);
    }

    @SuppressWarnings("SameParameterValue")
    private void addLine(
            final ExtendedChartData chartData,
            final XDDFCategoryDataSource timesDataSource,
            final List<BigDecimal> decimals,
            final MarkerProperties markerProperties,
            final Color seriesColor
    ) {
        if (decimals.stream().anyMatch(Objects::nonNull)) {
            final BigDecimal[] bigDecimals = decimals.toArray(BigDecimal[]::new);
            addSeries(chartData, timesDataSource, bigDecimals, markerProperties, seriesColor);
        }
    }

    private void addOperations(
            final ExtendedChartData chartData,
            final XDDFCategoryDataSource timesDataSource,
            final List<Operation> operations,
            final List<Integer> operationsIndices
    ) {
        final MoneyValue[] buyOperationsPrices = new MoneyValue[timesDataSource.getPointCount()];
        final MoneyValue[] sellOperationsPrices = new MoneyValue[timesDataSource.getPointCount()];
        boolean buyOperationsExist = false;
        boolean sellOperationsExist = false;
        for (int i = 0; i < operations.size(); i++) {
            final Operation operation = operations.get(i);
            final int index = operationsIndices.get(i);
            if (operation.getOperationType() == OperationType.OPERATION_TYPE_BUY) {
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
            final MoneyValue[] moneyValues,
            final MarkerProperties markerProperties
    ) {
        final BigDecimal[] bigDecimals = Arrays.stream(moneyValues)
                .map(DecimalUtils::newBigDecimal)
                .toArray(BigDecimal[]::new);
        addSeries(chartData, timesDataSource, bigDecimals, markerProperties, null);
    }

    @SuppressWarnings("SameParameterValue")
    private void addSeries(
            final ExtendedChartData chartData,
            final XDDFCategoryDataSource timesDataSource,
            final BigDecimal[] decimals,
            final MarkerProperties markerProperties
    ) {
        addSeries(chartData, timesDataSource, decimals, markerProperties, null);
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