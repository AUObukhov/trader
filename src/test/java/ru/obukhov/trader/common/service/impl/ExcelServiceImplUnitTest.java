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
import ru.obukhov.trader.common.model.poi.ExtendedSheet;
import ru.obukhov.trader.common.model.poi.ExtendedWorkbook;
import ru.obukhov.trader.common.service.interfaces.ExcelFileService;
import ru.obukhov.trader.common.util.DecimalUtils;
import ru.obukhov.trader.common.util.TimestampUtils;
import ru.obukhov.trader.market.model.Candle;
import ru.obukhov.trader.market.model.PositionBuilder;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.test.utils.model.CandleBuilder;
import ru.obukhov.trader.test.utils.model.DateTimeTestData;
import ru.obukhov.trader.test.utils.model.TestData;
import ru.obukhov.trader.test.utils.model.share.TestShares;
import ru.obukhov.trader.trading.model.BackTestResult;
import ru.obukhov.trader.trading.model.Balances;
import ru.obukhov.trader.trading.model.Profits;
import ru.obukhov.trader.trading.model.StrategyType;
import ru.obukhov.trader.web.model.BotConfig;
import ru.obukhov.trader.web.model.exchange.GetCandlesResponse;
import ru.tinkoff.piapi.contract.v1.CandleInterval;
import ru.tinkoff.piapi.contract.v1.Operation;
import ru.tinkoff.piapi.contract.v1.OperationType;
import ru.tinkoff.piapi.core.models.Position;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@ExtendWith(MockitoExtension.class)
class ExcelServiceImplUnitTest {

    private static final int MINIMUM_ROWS_COUNT = 22;

    private final MovingAverager averager = new SimpleMovingAverager();

    @Captor
    private ArgumentCaptor<ExtendedWorkbook> workbookArgumentCaptor;
    @Mock
    private ExcelFileService excelFileService;

    @InjectMocks
    private ExcelServiceImpl excelService;

    // region saveBackTestResult tests

    @Test
    void saveBackTestResult_savesMultipleResults() throws IOException {
        final String figi = TestShares.APPLE.share().figi();

        final BotConfig botConfig1 = new BotConfig(
                TestData.ACCOUNT_ID1,
                figi,
                CandleInterval.CANDLE_INTERVAL_HOUR,
                DecimalUtils.ZERO,
                StrategyType.CONSERVATIVE,
                Collections.emptyMap()
        );
        final BackTestResult result1 = createBackTestResult(botConfig1);

        final BotConfig botConfig2 = new BotConfig(
                TestData.ACCOUNT_ID1,
                figi,
                CandleInterval.CANDLE_INTERVAL_1_MIN,
                DecimalUtils.ZERO,
                StrategyType.CROSS,
                Map.of("minimumProfit", 0.01)
        );
        final BackTestResult result2 = createBackTestResult(botConfig2);

        final BotConfig botConfig3 = new BotConfig(
                TestData.ACCOUNT_ID1,
                figi,
                CandleInterval.CANDLE_INTERVAL_1_MIN,
                DecimalUtils.ZERO,
                StrategyType.CROSS,
                Map.of("minimumProfit", 0.01, "indexCoefficient", 0.5)
        );
        final BackTestResult result3 = createBackTestResult(botConfig3);
        final List<BackTestResult> results = List.of(result1, result2, result3);

        excelService.saveBackTestResults(results);

        Mockito.verify(excelFileService, Mockito.times(3))
                .saveToFile(workbookArgumentCaptor.capture(), Mockito.startsWith("BackTestResul"));

        final List<ExtendedWorkbook> workbooks = workbookArgumentCaptor.getAllValues();
        Assertions.assertEquals(results.size(), workbooks.size());

        for (int i = 0; i < results.size(); i++) {
            final BackTestResult result = results.get(i);

            final ExtendedWorkbook workbook = workbooks.get(i);
            Assertions.assertEquals(1, workbook.getNumberOfSheets());

            final ExtendedSheet sheet = (ExtendedSheet) workbook.getSheetAt(0);

            final int expectedRowCount = getExpectedRowCount(result);
            Assertions.assertEquals(expectedRowCount, sheet.getRowsCount());

            final Iterator<Row> rowIterator = sheet.iterator();
            assertBotConfig(result.botConfig(), rowIterator);
            assertCommonStatistics(figi, result, rowIterator);
            assertPositions(result, rowIterator);
            assertOperations(result, rowIterator);
            assertMergedRegions(sheet);
            assertChartCreated(sheet);
        }
    }

    @Test
    void saveBackTestResult_skipsErrorMessage_whenErrorIsNull() throws IOException {
        final String figi = TestShares.APPLE.share().figi();

        final BotConfig botConfig = new BotConfig(
                TestData.ACCOUNT_ID1,
                figi,
                CandleInterval.CANDLE_INTERVAL_1_MIN,
                DecimalUtils.ZERO,
                StrategyType.CROSS,
                Map.of("minimumProfit", 0.01)
        );
        final BackTestResult result = createBackTestResult(botConfig);

        excelService.saveBackTestResults(List.of(result));

        Mockito.verify(excelFileService, Mockito.times(1))
                .saveToFile(workbookArgumentCaptor.capture(), Mockito.startsWith("BackTestResul"));

        final ExtendedWorkbook workbook = workbookArgumentCaptor.getValue();
        Assertions.assertEquals(1, workbook.getNumberOfSheets());

        final ExtendedSheet sheet = (ExtendedSheet) workbook.getSheetAt(0);

        final int expectedRowCount = getExpectedRowCount(result);
        Assertions.assertEquals(expectedRowCount, sheet.getRowsCount());

        final Iterator<Row> rowIterator = sheet.iterator();
        assertBotConfig(result.botConfig(), rowIterator);
        assertCommonStatistics(figi, result, rowIterator);
        assertPositions(result, rowIterator);
        assertOperations(result, rowIterator);
        assertMergedRegions(sheet);
        assertChartCreated(sheet);
    }

    @Test
    void saveBackTestResult_skipsErrorMessage_whenErrorIsEmpty() throws IOException {
        final String figi = TestShares.APPLE.share().figi();

        final BotConfig botConfig = new BotConfig(
                TestData.ACCOUNT_ID1,
                figi,
                CandleInterval.CANDLE_INTERVAL_1_MIN,
                DecimalUtils.ZERO,
                StrategyType.CROSS,
                Map.of("minimumProfit", 0.01)
        );
        final BackTestResult result = createBackTestResult(botConfig, StringUtils.EMPTY);

        excelService.saveBackTestResults(List.of(result));

        Mockito.verify(excelFileService, Mockito.times(1))
                .saveToFile(workbookArgumentCaptor.capture(), Mockito.startsWith("BackTestResul"));

        final ExtendedWorkbook workbook = workbookArgumentCaptor.getValue();
        Assertions.assertEquals(1, workbook.getNumberOfSheets());

        final ExtendedSheet sheet = (ExtendedSheet) workbook.getSheetAt(0);

        final int expectedRowCount = getExpectedRowCount(result);
        Assertions.assertEquals(expectedRowCount, sheet.getRowsCount());

        final Iterator<Row> rowIterator = sheet.iterator();
        assertBotConfig(result.botConfig(), rowIterator);
        assertCommonStatistics(figi, result, rowIterator);
        assertPositions(result, rowIterator);
        assertOperations(result, rowIterator);
        assertMergedRegions(sheet);
        assertChartCreated(sheet);
    }

    @Test
    void saveBackTestResult_addsErrorMessage_whenErrorIsNotEmpty() throws IOException {
        final String figi = TestShares.APPLE.share().figi();
        final String error = "error";

        final BotConfig botConfig = new BotConfig(
                TestData.ACCOUNT_ID1,
                figi,
                CandleInterval.CANDLE_INTERVAL_1_MIN,
                DecimalUtils.ZERO,
                StrategyType.CROSS,
                Map.of("minimumProfit", 0.01)
        );
        final BackTestResult result = createBackTestResult(botConfig, error);

        excelService.saveBackTestResults(List.of(result));

        Mockito.verify(excelFileService, Mockito.times(1))
                .saveToFile(workbookArgumentCaptor.capture(), Mockito.startsWith("BackTestResul"));

        final ExtendedWorkbook workbook = workbookArgumentCaptor.getValue();
        Assertions.assertEquals(1, workbook.getNumberOfSheets());

        final ExtendedSheet sheet = (ExtendedSheet) workbook.getSheetAt(0);

        final int expectedRowCount = getExpectedRowCount(result);
        Assertions.assertEquals(expectedRowCount, sheet.getRowsCount());

        final Iterator<Row> rowIterator = sheet.iterator();
        assertBotConfig(result.botConfig(), rowIterator);
        assertCommonStatistics(figi, result, rowIterator);
        AssertUtils.assertRowValues(rowIterator.next(), "Текст ошибки", result.error());

        assertPositions(result, rowIterator);
        assertOperations(result, rowIterator);
        assertMergedRegions(sheet);

        assertChartCreated(sheet);
    }

    @Test
    void saveBackTestResult_skipsChart_whenCandlesAreNull() throws IOException {
        final String figi = TestShares.APPLE.share().figi();

        final BotConfig botConfig = new BotConfig(
                TestData.ACCOUNT_ID1,
                figi,
                CandleInterval.CANDLE_INTERVAL_1_MIN,
                DecimalUtils.ZERO,
                StrategyType.CROSS,
                Map.of("minimumProfit", 0.01)
        );
        final BackTestResult result = createBackTestResult(botConfig, (List<Candle>) null);

        excelService.saveBackTestResults(List.of(result));

        Mockito.verify(excelFileService, Mockito.times(1))
                .saveToFile(workbookArgumentCaptor.capture(), Mockito.startsWith("BackTestResul"));

        final ExtendedWorkbook workbook = workbookArgumentCaptor.getValue();
        Assertions.assertEquals(1, workbook.getNumberOfSheets());

        final ExtendedSheet sheet = (ExtendedSheet) workbook.getSheetAt(0);

        final int expectedRowCount = getExpectedRowCount(result);
        Assertions.assertEquals(expectedRowCount, sheet.getRowsCount());

        final Iterator<Row> rowIterator = sheet.iterator();
        assertBotConfig(result.botConfig(), rowIterator);
        assertCommonStatistics(figi, result, rowIterator);
        assertPositions(result, rowIterator);
        assertOperations(result, rowIterator);
        assertMergedRegions(sheet);

        Assertions.assertNull(sheet.getDrawingPatriarch());
    }

    @Test
    void saveBackTestResult_skipsChart_whenCandlesAreEmpty() throws IOException {
        final String figi = TestShares.APPLE.share().figi();

        final BotConfig botConfig = new BotConfig(
                TestData.ACCOUNT_ID1,
                figi,
                CandleInterval.CANDLE_INTERVAL_1_MIN,
                DecimalUtils.ZERO,
                StrategyType.CROSS,
                Map.of("minimumProfit", 0.01)
        );
        final BackTestResult result = createBackTestResult(botConfig, Collections.emptyList());

        excelService.saveBackTestResults(List.of(result));

        Mockito.verify(excelFileService, Mockito.times(1))
                .saveToFile(workbookArgumentCaptor.capture(), Mockito.startsWith("BackTestResul"));

        final ExtendedWorkbook workbook = workbookArgumentCaptor.getValue();
        Assertions.assertEquals(1, workbook.getNumberOfSheets());

        final ExtendedSheet sheet = (ExtendedSheet) workbook.getSheetAt(0);

        final int expectedRowCount = getExpectedRowCount(result);
        Assertions.assertEquals(expectedRowCount, sheet.getRowsCount());

        final Iterator<Row> rowIterator = sheet.iterator();
        assertBotConfig(result.botConfig(), rowIterator);
        assertCommonStatistics(figi, result, rowIterator);
        assertPositions(result, rowIterator);
        assertOperations(result, rowIterator);
        assertMergedRegions(sheet);

        Assertions.assertNull(sheet.getDrawingPatriarch());
    }

    @Test
    void saveBackTestResult_catchesIOExceptionOfFileSaving() throws IOException {
        final String figi = TestShares.APPLE.share().figi();

        final BotConfig botConfig = new BotConfig(
                TestData.ACCOUNT_ID1,
                figi,
                CandleInterval.CANDLE_INTERVAL_1_MIN,
                DecimalUtils.ZERO,
                StrategyType.CROSS,
                Collections.emptyMap()
        );
        final BackTestResult result1 = createBackTestResult(botConfig);
        final BackTestResult result2 = createBackTestResult(botConfig);
        final BackTestResult result3 = createBackTestResult(botConfig);
        final List<BackTestResult> results = List.of(result1, result2, result3);

        Mockito.doThrow(new IOException())
                .when(excelFileService)
                .saveToFile(Mockito.any(Workbook.class), Mockito.startsWith("BackTestResul"));

        Assertions.assertDoesNotThrow(() -> excelService.saveBackTestResults(results));
    }

    private void assertBotConfig(BotConfig botConfig, Iterator<Row> rowIterator) {
        AssertUtils.assertRowValues(rowIterator.next(), "Конфигурация");
        AssertUtils.assertRowValues(rowIterator.next(), "Размер свечи", botConfig.candleInterval().toString());
        AssertUtils.assertRowValues(rowIterator.next(), "Стратегия", botConfig.strategyType().toString());
        for (final Map.Entry<String, Object> entry : botConfig.strategyParams().entrySet()) {
            AssertUtils.assertRowValues(rowIterator.next(), entry.getKey(), entry.getValue());
        }
    }

    @SuppressWarnings("SameParameterValue")
    private void assertCommonStatistics(String figi, BackTestResult result, Iterator<Row> rowIterator) {
        AssertUtils.assertRowValues(rowIterator.next());
        AssertUtils.assertRowValues(rowIterator.next(), "Общая статистика");
        AssertUtils.assertRowValues(rowIterator.next(), "Счёт", result.botConfig().accountId());
        AssertUtils.assertRowValues(rowIterator.next(), "FIGI", figi);
        AssertUtils.assertRowValues(rowIterator.next(), "Интервал", result.interval().toPrettyString());
        AssertUtils.assertRowValues(rowIterator.next(), "Начальный баланс", result.balances().initialInvestment());
        AssertUtils.assertRowValues(rowIterator.next(), "Вложения", result.balances().totalInvestment());
        AssertUtils.assertRowValues(rowIterator.next(), "Итоговый общий баланс", result.balances().finalTotalSavings());
        AssertUtils.assertRowValues(rowIterator.next(), "Итоговый валютный баланс", result.balances().finalBalance());
        AssertUtils.assertRowValues(rowIterator.next(), "Средневзвешенные вложения", result.balances().weightedAverageInvestment());
        AssertUtils.assertRowValues(rowIterator.next(), "Абсолютный доход", result.profits().absolute());
        AssertUtils.assertRowValues(rowIterator.next(), "Относительный доход", result.profits().relative());
        AssertUtils.assertRowValues(rowIterator.next(), "Относительный годовой доход", result.profits().relativeAnnual());
    }

    private void assertPositions(BackTestResult result, Iterator<Row> rowIterator) {
        AssertUtils.assertRowValues(rowIterator.next());
        AssertUtils.assertRowValues(rowIterator.next(), "Позиции");
        AssertUtils.assertRowValues(rowIterator.next(), "Цена", "Количество");
        for (final Position position : result.positions()) {
            AssertUtils.assertRowValues(rowIterator.next(), position.getCurrentPrice().getValue(), position.getQuantity());
        }
    }

    private void assertOperations(BackTestResult result, Iterator<Row> rowIterator) {
        AssertUtils.assertRowValues(rowIterator.next());
        AssertUtils.assertRowValues(rowIterator.next(), "Операции");
        AssertUtils.assertRowValues(rowIterator.next(), "Дата и время", "Тип операции", "Цена", "Количество");

        for (final Operation operation : result.operations()) {
            AssertUtils.assertRowValues(
                    rowIterator.next(),
                    operation.getDate(),
                    operation.getOperationType().name(),
                    DecimalUtils.newBigDecimal(operation.getPrice()).doubleValue(),
                    operation.getQuantity()
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
        final String figi = TestShares.APPLE.share().figi();

        final OffsetDateTime from = DateTimeTestData.createDateTime(2021, 1, 1);
        final OffsetDateTime to = DateTimeTestData.createDateTime(2021, 2, 1);
        final Interval interval = Interval.of(from, to);

        final GetCandlesResponse response = createGetCandlesResponse();

        excelService.saveCandles(figi, interval, response);

        final String fileNamePrefix = "Candles for FIGI '" + figi + "'";
        Mockito.verify(excelFileService, Mockito.times(1))
                .saveToFile(workbookArgumentCaptor.capture(), Mockito.startsWith(fileNamePrefix));

        final ExtendedWorkbook workbook = workbookArgumentCaptor.getValue();
        Assertions.assertEquals(1, workbook.getNumberOfSheets());
        final ExtendedSheet sheet = (ExtendedSheet) workbook.getSheet(figi);

        Assertions.assertEquals(10, sheet.getRowsCount());

        final Iterator<Row> rowIterator = sheet.iterator();
        AssertUtils.assertRowValues(rowIterator.next(), "FIGI", figi);
        AssertUtils.assertRowValues(rowIterator.next(), "Интервал", interval.toPrettyString());
        AssertUtils.assertRowValues(rowIterator.next());
        AssertUtils.assertRowValues(rowIterator.next(), "Свечи");
        AssertUtils.assertRowValues(rowIterator.next(), "Дата-время", "Цена открытия", "Цена закрытия", "Набольшая цена", "Наименьшая цена");
        for (final Candle candle : response.getCandles()) {
            AssertUtils.assertRowValues(
                    rowIterator.next(),
                    candle.getTime(),
                    candle.getOpen(),
                    candle.getClose(),
                    candle.getHigh(),
                    candle.getLow()
            );
        }

        assertChartCreated(sheet);
    }

    @Test
    void saveCandles_catchesIOExceptionOfFileSaving() throws IOException {
        final String figi = TestShares.APPLE.share().figi();

        final OffsetDateTime from = DateTimeTestData.createDateTime(2021, 1, 1);
        final OffsetDateTime to = DateTimeTestData.createDateTime(2021, 2, 1);
        final Interval interval = Interval.of(from, to);

        final GetCandlesResponse response = createGetCandlesResponse();

        final String fileNamePrefix = "Candles for FIGI '" + figi + "'";
        Mockito.doThrow(new IOException())
                .when(excelFileService)
                .saveToFile(Mockito.any(Workbook.class), Mockito.startsWith(fileNamePrefix));

        Assertions.assertDoesNotThrow(() -> excelService.saveCandles(figi, interval, response));
    }

    private BackTestResult createBackTestResult(final BotConfig botConfig, final String error) {
        final Balances balances = new Balances(
                DecimalUtils.setDefaultScale(700L),
                DecimalUtils.setDefaultScale(800L),
                DecimalUtils.setDefaultScale(750L),
                DecimalUtils.setDefaultScale(200L),
                DecimalUtils.setDefaultScale(1000L)
        );
        return new BackTestResult(
                botConfig,
                createInterval(),
                balances,
                new Profits(DecimalUtils.setDefaultScale(300L), 0.25, 6.0),
                createPositions(botConfig.figi()),
                createBackTestOperations(botConfig.figi()),
                createCandles(),
                error
        );
    }

    private BackTestResult createBackTestResult(final BotConfig botConfig, final List<Candle> candles) {
        final Balances balances = new Balances(
                DecimalUtils.setDefaultScale(700L),
                DecimalUtils.setDefaultScale(800L),
                DecimalUtils.setDefaultScale(750L),
                DecimalUtils.setDefaultScale(200L),
                DecimalUtils.setDefaultScale(1000L)
        );
        return new BackTestResult(
                botConfig,
                createInterval(),
                balances,
                new Profits(DecimalUtils.setDefaultScale(300L), 0.25, 6.0),
                createPositions(botConfig.figi()),
                createBackTestOperations(botConfig.figi()),
                candles,
                null
        );
    }

    private BackTestResult createBackTestResult(final BotConfig botConfig) {
        final Balances balances = new Balances(
                DecimalUtils.setDefaultScale(700L),
                DecimalUtils.setDefaultScale(800L),
                DecimalUtils.setDefaultScale(750L),
                DecimalUtils.setDefaultScale(200L),
                DecimalUtils.setDefaultScale(1000L)
        );
        return new BackTestResult(
                botConfig,
                createInterval(),
                balances,
                new Profits(DecimalUtils.setDefaultScale(300L), 0.25, 6.0),
                createPositions(botConfig.figi()),
                createBackTestOperations(botConfig.figi()),
                createCandles(),
                null
        );
    }

    private Interval createInterval() {
        final OffsetDateTime from = DateTimeTestData.createDateTime(2020, 10, 1);
        final OffsetDateTime to = DateTimeTestData.createDateTime(2020, 10, 15);
        return Interval.of(from, to);
    }

    private List<Position> createPositions(final String figi) {
        return List.of(
                new PositionBuilder()
                        .setFigi(figi)
                        .setQuantity(3)
                        .setCurrentPrice(200)
                        .build(),
                new PositionBuilder()
                        .setFigi(figi)
                        .setQuantity(2)
                        .setCurrentPrice(100)
                        .build()
        );
    }

    private List<Operation> createBackTestOperations(String figi) {
        Operation operation1 = Operation.newBuilder()
                .setFigi(figi)
                .setDate(TimestampUtils.newTimestamp(2020, 10, 1, 10))
                .setOperationType(OperationType.OPERATION_TYPE_BUY)
                .setPrice(TestData.newMoneyValue(150, ""))
                .setQuantity(1L)
                .build();
        Operation operation2 = Operation.newBuilder()
                .setFigi(figi)
                .setDate(TimestampUtils.newTimestamp(2020, 10, 5, 10, 11))
                .setOperationType(OperationType.OPERATION_TYPE_SELL)
                .setPrice(TestData.newMoneyValue(180, ""))
                .setQuantity(1L)
                .build();
        Operation operation3 = Operation.newBuilder()
                .setFigi(figi)
                .setDate(TimestampUtils.newTimestamp(2020, 10, 10, 10, 50))
                .setOperationType(OperationType.OPERATION_TYPE_BUY)
                .setPrice(TestData.newMoneyValue(160, ""))
                .setQuantity(3L)
                .build();
        Operation operation4 = Operation.newBuilder()
                .setFigi(figi)
                .setDate(TimestampUtils.newTimestamp(2020, 11, 1, 10))
                .setOperationType(OperationType.OPERATION_TYPE_BUY)
                .setPrice(TestData.newMoneyValue(120, ""))
                .setQuantity(2L)
                .build();

        return List.of(operation1, operation2, operation3, operation4);
    }

    private List<Candle> createCandles() {
        final Candle candle1 = new CandleBuilder()
                .setOpen(150)
                .setTime(DateTimeTestData.createDateTime(2020, 10, 1, 10))
                .build();
        final Candle candle2 = new CandleBuilder()
                .setOpen(160)
                .setTime(DateTimeTestData.createDateTime(2020, 10, 1, 11))
                .build();
        final Candle candle3 = new CandleBuilder()
                .setOpen(180)
                .setTime(DateTimeTestData.createDateTime(2020, 10, 5, 10, 11))
                .build();
        final Candle candle4 = new CandleBuilder()
                .setOpen(160)
                .setTime(DateTimeTestData.createDateTime(2020, 10, 10, 10, 50))
                .build();
        final Candle candle5 = new CandleBuilder()
                .setOpen(120)
                .setTime(DateTimeTestData.createDateTime(2020, 11, 1, 10))
                .build();

        return List.of(candle1, candle2, candle3, candle4, candle5);
    }

    private GetCandlesResponse createGetCandlesResponse() {
        final List<Candle> candles = createCandles();
        final List<BigDecimal> opens = candles.stream().map(Candle::getOpen).toList();
        final List<BigDecimal> shortAverages = averager.getAverages(opens, 2);
        final List<BigDecimal> longAverages = averager.getAverages(opens, 5);

        return new GetCandlesResponse(candles, shortAverages, longAverages);
    }

    private int getExpectedRowCount(BackTestResult result) {
        return MINIMUM_ROWS_COUNT +
                result.botConfig().strategyParams().size() +
                result.positions().size() +
                result.operations().size() +
                (StringUtils.isEmpty(result.error()) ? 0 : 1);
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