package ru.obukhov.trader.common.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xddf.usermodel.chart.XDDFChartAxis;
import org.apache.poi.xssf.usermodel.XSSFChart;
import org.apache.poi.xssf.usermodel.XSSFGraphicFrame;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.common.model.Point;
import ru.obukhov.trader.common.model.poi.ExtendedSheet;
import ru.obukhov.trader.common.model.poi.ExtendedWorkbook;
import ru.obukhov.trader.common.service.interfaces.ExcelFileService;
import ru.obukhov.trader.common.service.interfaces.MovingAverager;
import ru.obukhov.trader.common.util.DateUtils;
import ru.obukhov.trader.common.util.TrendUtils;
import ru.obukhov.trader.market.model.Candle;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.test.utils.TestDataHelper;
import ru.obukhov.trader.trading.model.StrategyType;
import ru.obukhov.trader.web.model.SimulatedOperation;
import ru.obukhov.trader.web.model.SimulatedPosition;
import ru.obukhov.trader.web.model.SimulationResult;
import ru.obukhov.trader.web.model.TradingConfig;
import ru.obukhov.trader.web.model.exchange.GetCandlesResponse;
import ru.tinkoff.invest.openapi.model.rest.CandleResolution;
import ru.tinkoff.invest.openapi.model.rest.OperationType;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@ExtendWith(MockitoExtension.class)
class ExcelServiceImplUnitTest {

    private static final int MINIMUM_ROWS_COUNT = 21;

    private final MovingAverager averager = new SimpleMovingAverager();

    @Captor
    private ArgumentCaptor<ExtendedWorkbook> workbookArgumentCaptor;
    @Mock
    private ExcelFileService excelFileService;

    @InjectMocks
    private ExcelServiceImpl excelService;

    // region saveSimulationResult tests

    @Test
    void saveSimulationResult_savesMultipleResults() throws IOException {
        final String ticker = "ticker";

        final TradingConfig tradingConfig1 = new TradingConfig()
                .setCandleResolution(CandleResolution.HOUR)
                .setStrategyType(StrategyType.CONSERVATIVE)
                .setStrategyParams(Map.of());
        final SimulationResult result1 = createSimulationResult(ticker, tradingConfig1);

        final TradingConfig tradingConfig2 = new TradingConfig()
                .setCandleResolution(CandleResolution._1MIN)
                .setStrategyType(StrategyType.SIMPLE_GOLDEN_CROSS)
                .setStrategyParams(Map.of("minimumProfit", 0.01));
        final SimulationResult result2 = createSimulationResult(ticker, tradingConfig2);

        final TradingConfig tradingConfig3 = new TradingConfig()
                .setCandleResolution(CandleResolution._1MIN)
                .setStrategyType(StrategyType.LINEAR_GOLDEN_CROSS)
                .setStrategyParams(Map.of("minimumProfit", 0.01, "indexCoefficient", 0.5));
        final SimulationResult result3 = createSimulationResult(ticker, tradingConfig3);
        final List<SimulationResult> results = List.of(result1, result2, result3);

        excelService.saveSimulationResults(ticker, results);

        final String fileNamePrefix = "SimulationResult for '" + ticker + "'";
        Mockito.verify(excelFileService, Mockito.times(3))
                .saveToFile(workbookArgumentCaptor.capture(), Mockito.startsWith(fileNamePrefix));

        final List<ExtendedWorkbook> workbooks = workbookArgumentCaptor.getAllValues();
        Assertions.assertEquals(results.size(), workbooks.size());

        for (int i = 0; i < results.size(); i++) {
            final SimulationResult result = results.get(i);

            final ExtendedWorkbook workbook = workbooks.get(i);
            Assertions.assertEquals(1, workbook.getNumberOfSheets());

            final ExtendedSheet sheet = (ExtendedSheet) workbook.getSheetAt(0);

            final int expectedRowCount = getExpectedRowCount(result);
            Assertions.assertEquals(expectedRowCount, sheet.getRowsCount());

            final Iterator<Row> rowIterator = sheet.iterator();
            assertTradingConfig(result.getTradingConfig(), rowIterator);
            assertCommonStatistics(ticker, result, rowIterator);
            assertPositions(result, rowIterator);
            assertOperations(result, rowIterator);
            assertMergedRegions(sheet);
            assertChartCreated(sheet);
        }
    }

    @Test
    void saveSimulationResult_skipsErrorMessage_whenErrorIsNull() throws IOException {
        final String ticker = "ticker";

        final TradingConfig tradingConfig = new TradingConfig()
                .setCandleResolution(CandleResolution._1MIN)
                .setStrategyType(StrategyType.SIMPLE_GOLDEN_CROSS)
                .setStrategyParams(Map.of("minimumProfit", 0.01));
        final SimulationResult result = createSimulationResult(ticker, tradingConfig);

        excelService.saveSimulationResults(ticker, List.of(result));

        final String fileNamePrefix = "SimulationResult for '" + ticker + "'";
        Mockito.verify(excelFileService, Mockito.times(1))
                .saveToFile(workbookArgumentCaptor.capture(), Mockito.startsWith(fileNamePrefix));

        final ExtendedWorkbook workbook = workbookArgumentCaptor.getValue();
        Assertions.assertEquals(1, workbook.getNumberOfSheets());

        final ExtendedSheet sheet = (ExtendedSheet) workbook.getSheetAt(0);

        final int expectedRowCount = getExpectedRowCount(result);
        Assertions.assertEquals(expectedRowCount, sheet.getRowsCount());

        final Iterator<Row> rowIterator = sheet.iterator();
        assertTradingConfig(result.getTradingConfig(), rowIterator);
        assertCommonStatistics(ticker, result, rowIterator);
        assertPositions(result, rowIterator);
        assertOperations(result, rowIterator);
        assertMergedRegions(sheet);
        assertChartCreated(sheet);
    }

    @Test
    void saveSimulationResult_skipsErrorMessage_whenErrorIsEmpty() throws IOException {
        final String ticker = "ticker";

        final TradingConfig tradingConfig = new TradingConfig()
                .setCandleResolution(CandleResolution._1MIN)
                .setStrategyType(StrategyType.SIMPLE_GOLDEN_CROSS)
                .setStrategyParams(Map.of("minimumProfit", 0.01));
        final SimulationResult result = createSimulationResult(ticker, tradingConfig);
        result.setError(StringUtils.EMPTY);

        excelService.saveSimulationResults(ticker, List.of(result));

        final String fileNamePrefix = "SimulationResult for '" + ticker + "'";
        Mockito.verify(excelFileService, Mockito.times(1))
                .saveToFile(workbookArgumentCaptor.capture(), Mockito.startsWith(fileNamePrefix));

        final ExtendedWorkbook workbook = workbookArgumentCaptor.getValue();
        Assertions.assertEquals(1, workbook.getNumberOfSheets());

        final ExtendedSheet sheet = (ExtendedSheet) workbook.getSheetAt(0);

        final int expectedRowCount = getExpectedRowCount(result);
        Assertions.assertEquals(expectedRowCount, sheet.getRowsCount());

        final Iterator<Row> rowIterator = sheet.iterator();
        assertTradingConfig(result.getTradingConfig(), rowIterator);
        assertCommonStatistics(ticker, result, rowIterator);
        assertPositions(result, rowIterator);
        assertOperations(result, rowIterator);
        assertMergedRegions(sheet);
        assertChartCreated(sheet);
    }

    @Test
    void saveSimulationResult_addsErrorMessage_whenErrorIsNotEmpty() throws IOException {
        final String ticker = "ticker";
        final String error = "error";

        final TradingConfig tradingConfig = new TradingConfig()
                .setCandleResolution(CandleResolution._1MIN)
                .setStrategyType(StrategyType.SIMPLE_GOLDEN_CROSS)
                .setStrategyParams(Map.of("minimumProfit", 0.01));
        final SimulationResult result = createSimulationResult(ticker, tradingConfig);
        result.setError(error);

        excelService.saveSimulationResults(ticker, List.of(result));

        final String fileNamePrefix = "SimulationResult for '" + ticker + "'";
        Mockito.verify(excelFileService, Mockito.times(1))
                .saveToFile(workbookArgumentCaptor.capture(), Mockito.startsWith(fileNamePrefix));

        final ExtendedWorkbook workbook = workbookArgumentCaptor.getValue();
        Assertions.assertEquals(1, workbook.getNumberOfSheets());

        final ExtendedSheet sheet = (ExtendedSheet) workbook.getSheetAt(0);

        final int expectedRowCount = getExpectedRowCount(result);
        Assertions.assertEquals(expectedRowCount, sheet.getRowsCount());

        final Iterator<Row> rowIterator = sheet.iterator();
        assertTradingConfig(result.getTradingConfig(), rowIterator);
        assertCommonStatistics(ticker, result, rowIterator);
        AssertUtils.assertRowValues(rowIterator.next(), "Текст ошибки", result.getError());

        assertPositions(result, rowIterator);
        assertOperations(result, rowIterator);
        assertMergedRegions(sheet);

        assertChartCreated(sheet);
    }

    @Test
    void saveSimulationResult_skipsChart_whenCandlesAreNull() throws IOException {
        final String ticker = "ticker";

        final TradingConfig tradingConfig = new TradingConfig()
                .setCandleResolution(CandleResolution._1MIN)
                .setStrategyType(StrategyType.SIMPLE_GOLDEN_CROSS)
                .setStrategyParams(Map.of("minimumProfit", 0.01));
        final SimulationResult result = createSimulationResult(ticker, tradingConfig);
        result.setCandles(null);

        excelService.saveSimulationResults(ticker, List.of(result));

        final String fileNamePrefix = "SimulationResult for '" + ticker + "'";
        Mockito.verify(excelFileService, Mockito.times(1))
                .saveToFile(workbookArgumentCaptor.capture(), Mockito.startsWith(fileNamePrefix));

        final ExtendedWorkbook workbook = workbookArgumentCaptor.getValue();
        Assertions.assertEquals(1, workbook.getNumberOfSheets());

        final ExtendedSheet sheet = (ExtendedSheet) workbook.getSheetAt(0);

        final int expectedRowCount = getExpectedRowCount(result);
        Assertions.assertEquals(expectedRowCount, sheet.getRowsCount());

        final Iterator<Row> rowIterator = sheet.iterator();
        assertTradingConfig(result.getTradingConfig(), rowIterator);
        assertCommonStatistics(ticker, result, rowIterator);
        assertPositions(result, rowIterator);
        assertOperations(result, rowIterator);
        assertMergedRegions(sheet);

        Assertions.assertNull(sheet.getDrawingPatriarch());
    }

    @Test
    void saveSimulationResult_skipsChart_whenCandlesAreEmpty() throws IOException {
        final String ticker = "ticker";

        final TradingConfig tradingConfig = new TradingConfig()
                .setCandleResolution(CandleResolution._1MIN)
                .setStrategyType(StrategyType.SIMPLE_GOLDEN_CROSS)
                .setStrategyParams(Map.of("minimumProfit", 0.01));
        final SimulationResult result = createSimulationResult(ticker, tradingConfig);
        result.setCandles(Collections.emptyList());

        excelService.saveSimulationResults(ticker, List.of(result));

        final String fileNamePrefix = "SimulationResult for '" + ticker + "'";
        Mockito.verify(excelFileService, Mockito.times(1))
                .saveToFile(workbookArgumentCaptor.capture(), Mockito.startsWith(fileNamePrefix));

        final ExtendedWorkbook workbook = workbookArgumentCaptor.getValue();
        Assertions.assertEquals(1, workbook.getNumberOfSheets());

        final ExtendedSheet sheet = (ExtendedSheet) workbook.getSheetAt(0);

        final int expectedRowCount = getExpectedRowCount(result);
        Assertions.assertEquals(expectedRowCount, sheet.getRowsCount());

        final Iterator<Row> rowIterator = sheet.iterator();
        assertTradingConfig(result.getTradingConfig(), rowIterator);
        assertCommonStatistics(ticker, result, rowIterator);
        assertPositions(result, rowIterator);
        assertOperations(result, rowIterator);
        assertMergedRegions(sheet);

        Assertions.assertNull(sheet.getDrawingPatriarch());
    }

    @Test
    @SuppressWarnings("java:S2699")
        // Tests should include assertions
    void saveSimulationResult_catchesIOExceptionOfFileSaving() throws IOException {
        final String ticker = "ticker";

        final TradingConfig tradingConfig = new TradingConfig()
                .setCandleResolution(CandleResolution._1MIN)
                .setStrategyType(StrategyType.SIMPLE_GOLDEN_CROSS)
                .setStrategyParams(Map.of());
        final SimulationResult result1 = createSimulationResult(ticker, tradingConfig);
        final SimulationResult result2 = createSimulationResult(ticker, tradingConfig);
        final SimulationResult result3 = createSimulationResult(ticker, tradingConfig);
        final List<SimulationResult> results = List.of(result1, result2, result3);

        final String fileNamePrefix = "SimulationResult for '" + ticker + "'";
        Mockito.doThrow(new IOException())
                .when(excelFileService)
                .saveToFile(Mockito.any(Workbook.class), Mockito.startsWith(fileNamePrefix));

        excelService.saveSimulationResults(ticker, results);
    }

    private void assertTradingConfig(TradingConfig tradingConfig, Iterator<Row> rowIterator) {
        AssertUtils.assertRowValues(rowIterator.next(), "Конфигурация");
        AssertUtils.assertRowValues(rowIterator.next(), "Размер свечи", tradingConfig.getCandleResolution().toString());
        AssertUtils.assertRowValues(rowIterator.next(), "Стратегия", tradingConfig.getStrategyType().toString());
        for (final Map.Entry<String, Object> entry : tradingConfig.getStrategyParams().entrySet()) {
            AssertUtils.assertRowValues(rowIterator.next(), entry.getKey(), entry.getValue());
        }
    }

    private void assertCommonStatistics(String ticker, SimulationResult result, Iterator<Row> rowIterator) {
        AssertUtils.assertRowValues(rowIterator.next());
        AssertUtils.assertRowValues(rowIterator.next(), "Общая статистика");
        AssertUtils.assertRowValues(rowIterator.next(), "Тикер", ticker);
        AssertUtils.assertRowValues(rowIterator.next(), "Интервал", result.getInterval().toPrettyString());
        AssertUtils.assertRowValues(rowIterator.next(), "Начальный баланс", result.getInitialBalance());
        AssertUtils.assertRowValues(rowIterator.next(), "Вложения", result.getTotalInvestment());
        AssertUtils.assertRowValues(rowIterator.next(), "Итоговый общий баланс", result.getFinalTotalBalance());
        AssertUtils.assertRowValues(rowIterator.next(), "Итоговый валютный баланс", result.getFinalBalance());
        AssertUtils.assertRowValues(rowIterator.next(),
                "Средневзвешенные вложения", result.getWeightedAverageInvestment());
        AssertUtils.assertRowValues(rowIterator.next(), "Абсолютный доход", result.getAbsoluteProfit());
        AssertUtils.assertRowValues(rowIterator.next(), "Относительный доход", result.getRelativeProfit());
        AssertUtils.assertRowValues(rowIterator.next(),
                "Относительный годовой доход", result.getRelativeYearProfit());
    }

    private void assertPositions(SimulationResult result, Iterator<Row> rowIterator) {
        AssertUtils.assertRowValues(rowIterator.next());
        AssertUtils.assertRowValues(rowIterator.next(), "Позиции");
        AssertUtils.assertRowValues(rowIterator.next(), "Цена", "Количество");
        for (final SimulatedPosition position : result.getPositions()) {
            AssertUtils.assertRowValues(rowIterator.next(), position.getPrice(), position.getQuantity());
        }
    }

    private void assertOperations(SimulationResult result, Iterator<Row> rowIterator) {
        AssertUtils.assertRowValues(rowIterator.next());
        AssertUtils.assertRowValues(rowIterator.next(), "Операции");
        AssertUtils.assertRowValues(
                rowIterator.next(),
                "Дата и время",
                "Тип операции",
                "Цена",
                "Количество",
                "Комиссия"
        );

        for (final SimulatedOperation operation : result.getOperations()) {
            AssertUtils.assertRowValues(
                    rowIterator.next(),
                    operation.getDateTime(),
                    operation.getOperationType().name(),
                    operation.getPrice(),
                    operation.getQuantity(),
                    operation.getCommission()
            );
        }
    }

    private void assertMergedRegions(ExtendedSheet sheet) {
        final List<CellRangeAddress> mergedRegions = sheet.getMergedRegions();
        Assertions.assertEquals(4, mergedRegions.size());
    }

    // endregion

    @Test
    void saveCandles_createsAndSaveWorkbook() throws IOException {
        final String ticker = "ticker";

        final OffsetDateTime from = DateUtils.getDateTime(2021, 1, 1, 0, 0, 0);
        final OffsetDateTime to = DateUtils.getDateTime(2021, 2, 1, 0, 0, 0);
        final Interval interval = Interval.of(from, to);

        final GetCandlesResponse response = createGetCandlesResponse();

        excelService.saveCandles(ticker, interval, response);

        final String fileNamePrefix = "Candles for '" + ticker + "'";
        Mockito.verify(excelFileService, Mockito.times(1))
                .saveToFile(workbookArgumentCaptor.capture(), Mockito.startsWith(fileNamePrefix));

        final ExtendedWorkbook workbook = workbookArgumentCaptor.getValue();
        Assertions.assertEquals(1, workbook.getNumberOfSheets());
        final ExtendedSheet sheet = (ExtendedSheet) workbook.getSheet(ticker);

        Assertions.assertEquals(10, sheet.getRowsCount());

        final Iterator<Row> rowIterator = sheet.iterator();
        AssertUtils.assertRowValues(rowIterator.next(), "Тикер", ticker);
        AssertUtils.assertRowValues(rowIterator.next(), "Интервал", interval.toPrettyString());
        AssertUtils.assertRowValues(rowIterator.next());
        AssertUtils.assertRowValues(rowIterator.next(), "Свечи");
        AssertUtils.assertRowValues(
                rowIterator.next(),
                "Дата-время",
                "Цена открытия",
                "Цена закрытия",
                "Набольшая цена",
                "Наименьшая цена"
        );
        for (final Candle candle : response.getCandles()) {
            AssertUtils.assertRowValues(
                    rowIterator.next(),
                    candle.getTime(),
                    candle.getOpenPrice(),
                    candle.getClosePrice(),
                    candle.getHighestPrice(),
                    candle.getLowestPrice()
            );
        }

        assertChartCreated(sheet);
    }

    @Test
    @SuppressWarnings("java:S2699")
        // Tests should include assertions
    void saveCandles_catchesIOExceptionOfFileSaving() throws IOException {
        final String ticker = "ticker";

        final OffsetDateTime from = DateUtils.getDateTime(2021, 1, 1, 0, 0, 0);
        final OffsetDateTime to = DateUtils.getDateTime(2021, 2, 1, 0, 0, 0);
        final Interval interval = Interval.of(from, to);

        final GetCandlesResponse response = createGetCandlesResponse();

        final String fileNamePrefix = "Candles for '" + ticker + "'";
        Mockito.doThrow(new IOException())
                .when(excelFileService)
                .saveToFile(Mockito.any(Workbook.class), Mockito.startsWith(fileNamePrefix));

        excelService.saveCandles(ticker, interval, response);
    }

    private SimulationResult createSimulationResult(String ticker, TradingConfig tradingConfig) {
        return SimulationResult.builder()
                .tradingConfig(tradingConfig)
                .interval(createInterval())
                .initialBalance(BigDecimal.valueOf(700))
                .finalTotalBalance(BigDecimal.valueOf(1000))
                .finalBalance(BigDecimal.valueOf(200))
                .totalInvestment(BigDecimal.valueOf(800))
                .weightedAverageInvestment(BigDecimal.valueOf(750))
                .absoluteProfit(BigDecimal.valueOf(300))
                .relativeProfit(0.25)
                .relativeYearProfit(6d)
                .positions(createPositions(ticker))
                .operations(createSimulatedOperations(ticker))
                .candles(createCandles())
                .build();
    }

    private Interval createInterval() {
        final OffsetDateTime from = DateUtils.getDate(2020, 10, 1);
        final OffsetDateTime to = DateUtils.getDate(2020, 10, 15);
        return Interval.of(from, to);
    }

    private List<SimulatedPosition> createPositions(String ticker) {
        return List.of(
                new SimulatedPosition(ticker, BigDecimal.valueOf(200), 3),
                new SimulatedPosition(ticker, BigDecimal.valueOf(100), 2)
        );
    }

    private List<SimulatedOperation> createSimulatedOperations(String ticker) {
        SimulatedOperation operation1 = SimulatedOperation.builder()
                .ticker(ticker)
                .dateTime(DateUtils.getDateTime(2020, 10, 1, 10, 0, 0))
                .operationType(OperationType.BUY)
                .price(BigDecimal.valueOf(150))
                .quantity(1)
                .commission(BigDecimal.valueOf(0.45))
                .build();
        SimulatedOperation operation2 = SimulatedOperation.builder()
                .ticker(ticker)
                .dateTime(DateUtils.getDateTime(2020, 10, 5, 10, 11, 0))
                .operationType(OperationType.SELL)
                .price(BigDecimal.valueOf(180))
                .quantity(1)
                .commission(BigDecimal.valueOf(0.54))
                .build();
        SimulatedOperation operation3 = SimulatedOperation.builder()
                .ticker(ticker)
                .dateTime(DateUtils.getDateTime(2020, 10, 10, 10, 50, 0))
                .operationType(OperationType.BUY)
                .price(BigDecimal.valueOf(160))
                .quantity(3)
                .commission(BigDecimal.valueOf(0.48))
                .build();
        SimulatedOperation operation4 = SimulatedOperation.builder()
                .ticker(ticker)
                .dateTime(DateUtils.getDateTime(2020, 11, 1, 10, 0, 0))
                .operationType(OperationType.BUY)
                .price(BigDecimal.valueOf(120))
                .quantity(2)
                .commission(BigDecimal.valueOf(0.36))
                .build();

        return List.of(operation1, operation2, operation3, operation4);
    }

    private List<Candle> createCandles() {
        final Candle candle1 = TestDataHelper.createCandleWithOpenPriceAndTime(
                150,
                DateUtils.getDateTime(2020, 10, 1, 10, 0, 0)
        );
        final Candle candle2 = TestDataHelper.createCandleWithOpenPriceAndTime(
                160,
                DateUtils.getDateTime(2020, 10, 1, 11, 0, 0)
        );
        final Candle candle3 = TestDataHelper.createCandleWithOpenPriceAndTime(
                180,
                DateUtils.getDateTime(2020, 10, 5, 10, 11, 0)
        );
        final Candle candle4 = TestDataHelper.createCandleWithOpenPriceAndTime(
                160,
                DateUtils.getDateTime(2020, 10, 10, 10, 50, 0)
        );
        final Candle candle5 = TestDataHelper.createCandleWithOpenPriceAndTime(
                120,
                DateUtils.getDateTime(2020, 11, 1, 10, 0, 0)
        );

        return List.of(candle1, candle2, candle3, candle4, candle5);
    }

    private GetCandlesResponse createGetCandlesResponse() {
        final List<Candle> candles = createCandles();
        final List<OffsetDateTime> times = candles.stream().map(Candle::getTime).collect(Collectors.toList());
        final List<BigDecimal> openPrices = candles.stream().map(Candle::getOpenPrice).collect(Collectors.toList());
        final Function<BigDecimal, BigDecimal> selfExtractor = (BigDecimal number) -> number;
        final List<BigDecimal> averages = averager.getAverages(openPrices, selfExtractor, 5);

        final List<Integer> localMinimumsIndices =
                TrendUtils.getLocalExtremes(averages, selfExtractor, Comparator.reverseOrder());
        final List<Point> localMinimums = localMinimumsIndices.stream()
                .map(minimum -> TestDataHelper.createPoint(candles.get(minimum)))
                .collect(Collectors.toList());

        final List<Integer> localMaximumsIndices =
                TrendUtils.getLocalExtremes(averages, selfExtractor, Comparator.naturalOrder());
        final List<Point> localMaximums = localMaximumsIndices.stream()
                .map(maximum -> TestDataHelper.createPoint(candles.get(maximum)))
                .collect(Collectors.toList());

        final List<List<Point>> supportLines = TrendUtils.getRestraintLines(times, averages, localMinimumsIndices);
        final List<List<Point>> resistanceLines = TrendUtils.getRestraintLines(times, averages, localMaximumsIndices);

        return new GetCandlesResponse(
                candles,
                averages,
                localMinimums,
                localMaximums,
                supportLines,
                resistanceLines
        );
    }

    private int getExpectedRowCount(SimulationResult result) {
        return MINIMUM_ROWS_COUNT +
                result.getTradingConfig().getStrategyParams().size() +
                result.getPositions().size() +
                result.getOperations().size() +
                (StringUtils.isEmpty(result.getError()) ? 0 : 1);
    }

    private void assertChartCreated(ExtendedSheet sheet) {
        final Iterator<?> iterator = sheet.getDrawingPatriarch().iterator();
        final XSSFGraphicFrame frame = (XSSFGraphicFrame) iterator.next();
        Assertions.assertFalse(iterator.hasNext());
        final List<XSSFChart> charts = frame.getDrawing().getCharts();
        Assertions.assertEquals(1, charts.size());
        final XSSFChart chart = charts.get(0);
        final List<? extends XDDFChartAxis> axes = chart.getAxes();
        Assertions.assertEquals(2, axes.size());
        AssertUtils.assertEquals(120, axes.get(1).getMinimum());
        AssertUtils.assertEquals(180, axes.get(1).getMaximum());
    }

}