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
import ru.obukhov.trader.common.util.MapUtils;
import ru.obukhov.trader.market.model.Candle;
import ru.obukhov.trader.market.model.PositionBuilder;
import ru.obukhov.trader.market.model.Share;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.test.utils.model.CandleBuilder;
import ru.obukhov.trader.test.utils.model.DateTimeTestData;
import ru.obukhov.trader.test.utils.model.TestData;
import ru.obukhov.trader.test.utils.model.account.TestAccounts;
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

    private static final int CONSTANT_ROWS_COUNT = 12;

    private final MovingAverager averager = new SimpleMovingAverager();

    @Captor
    private ArgumentCaptor<ExtendedWorkbook> workbookArgumentCaptor;
    @Mock
    private ExcelFileService excelFileService;

    @InjectMocks
    private ExcelServiceImpl excelService;

    // region saveBackTestResult tests

    @Test
    void saveBackTestResults_savesResults_whenFullData() throws IOException {
        final String accountId = TestAccounts.TINKOFF.getId();

        final Share share1 = TestShares.APPLE.share();
        final Share share2 = TestShares.SBER.share();

        final String figi1 = share1.figi();
        final String figi2 = share2.figi();

        final List<String> figies = List.of(figi1, figi2);

        final CandleInterval candleInterval1 = CandleInterval.CANDLE_INTERVAL_HOUR;
        final Map<String, Object> strategyParams1 = Collections.emptyMap();
        final BotConfig botConfig1 = new BotConfig(accountId, figies, candleInterval1, DecimalUtils.ZERO, StrategyType.CONSERVATIVE, strategyParams1);
        final BackTestResult result1 = createBackTestResult(botConfig1, share2.currency());

        final CandleInterval candleInterval2 = CandleInterval.CANDLE_INTERVAL_1_MIN;
        final Map<String, Object> strategyParams2 = Map.of("minimumProfit", 0.01);
        final BotConfig botConfig2 = new BotConfig(accountId, figies, candleInterval2, DecimalUtils.ZERO, StrategyType.CROSS, strategyParams2);
        final BackTestResult result2 = createBackTestResult(botConfig2, share2.currency());

        final CandleInterval candleInterval3 = CandleInterval.CANDLE_INTERVAL_1_MIN;
        final Map<String, Object> strategyParams3 = Map.of("minimumProfit", 0.01, "indexCoefficient", 0.5);
        final BotConfig botConfig3 = new BotConfig(accountId, figies, candleInterval3, DecimalUtils.ZERO, StrategyType.CROSS, strategyParams3);
        final BackTestResult result3 = createBackTestResult(botConfig3, share2.currency());
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
            assertCommonStatistics(figies, result, rowIterator);
            assertPositions(result, rowIterator);
            assertOperations(result, rowIterator);
            assertMergedRegions(result, sheet);
            assertChartCreated(sheet);
        }
    }

    @Test
    void saveBackTestResults_savesResults_whenNoPositions() throws IOException {
        final String accountId = TestAccounts.TINKOFF.getId();

        final Share share1 = TestShares.APPLE.share();
        final Share share2 = TestShares.SBER.share();

        final String figi1 = share1.figi();
        final String figi2 = share2.figi();

        final List<String> figies = List.of(figi1, figi2);

        final CandleInterval candleInterval1 = CandleInterval.CANDLE_INTERVAL_HOUR;
        final Map<String, Object> strategyParams1 = Collections.emptyMap();
        final BotConfig botConfig1 = new BotConfig(accountId, figies, candleInterval1, DecimalUtils.ZERO, StrategyType.CONSERVATIVE, strategyParams1);
        final BackTestResult result1 = createBackTestResultWithoutPositions(botConfig1, share2.currency());

        final CandleInterval candleInterval2 = CandleInterval.CANDLE_INTERVAL_1_MIN;
        final Map<String, Object> strategyParams2 = Map.of("minimumProfit", 0.01);
        final BotConfig botConfig2 = new BotConfig(accountId, figies, candleInterval2, DecimalUtils.ZERO, StrategyType.CROSS, strategyParams2);
        final BackTestResult result2 = createBackTestResultWithoutPositions(botConfig2, share2.currency());

        final CandleInterval candleInterval3 = CandleInterval.CANDLE_INTERVAL_1_MIN;
        final Map<String, Object> strategyParams3 = Map.of("minimumProfit", 0.01, "indexCoefficient", 0.5);
        final BotConfig botConfig3 = new BotConfig(accountId, figies, candleInterval3, DecimalUtils.ZERO, StrategyType.CROSS, strategyParams3);
        final BackTestResult result3 = createBackTestResultWithoutPositions(botConfig3, share2.currency());
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
            assertCommonStatistics(figies, result, rowIterator);
            assertPositions(result, rowIterator);
            assertOperations(result, rowIterator);
            assertMergedRegions(result, sheet);
            assertChartCreated(sheet);
        }
    }

    @Test
    void saveBackTestResults_savesResults_whenNoOperations() throws IOException {
        final String accountId = TestAccounts.TINKOFF.getId();

        final Share share1 = TestShares.APPLE.share();
        final Share share2 = TestShares.SBER.share();

        final String figi1 = share1.figi();
        final String figi2 = share2.figi();

        final List<String> figies = List.of(figi1, figi2);

        final CandleInterval candleInterval1 = CandleInterval.CANDLE_INTERVAL_HOUR;
        final Map<String, Object> strategyParams1 = Collections.emptyMap();
        final BotConfig botConfig1 = new BotConfig(accountId, figies, candleInterval1, DecimalUtils.ZERO, StrategyType.CONSERVATIVE, strategyParams1);
        final BackTestResult result1 = createBackTestResultWithoutOperations(botConfig1, share2.currency());

        final CandleInterval candleInterval2 = CandleInterval.CANDLE_INTERVAL_1_MIN;
        final Map<String, Object> strategyParams2 = Map.of("minimumProfit", 0.01);
        final BotConfig botConfig2 = new BotConfig(accountId, figies, candleInterval2, DecimalUtils.ZERO, StrategyType.CROSS, strategyParams2);
        final BackTestResult result2 = createBackTestResultWithoutOperations(botConfig2, share2.currency());

        final CandleInterval candleInterval3 = CandleInterval.CANDLE_INTERVAL_1_MIN;
        final Map<String, Object> strategyParams3 = Map.of("minimumProfit", 0.01, "indexCoefficient", 0.5);
        final BotConfig botConfig3 = new BotConfig(accountId, figies, candleInterval3, DecimalUtils.ZERO, StrategyType.CROSS, strategyParams3);
        final BackTestResult result3 = createBackTestResultWithoutOperations(botConfig3, share2.currency());
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
            assertCommonStatistics(figies, result, rowIterator);
            assertPositions(result, rowIterator);
            assertOperations(result, rowIterator);
            assertMergedRegions(result, sheet);
            assertChartCreated(sheet);
        }
    }

    @Test
    void saveBackTestResults_skipsErrorMessage_whenErrorIsNull() throws IOException {
        final Share share1 = TestShares.APPLE.share();
        final Share share2 = TestShares.SBER.share();

        final String figi1 = share1.figi();
        final String figi2 = share2.figi();

        final String accountId = TestAccounts.TINKOFF.getId();
        final List<String> figies = List.of(figi1, figi2);
        final CandleInterval candleInterval = CandleInterval.CANDLE_INTERVAL_1_MIN;
        final Map<String, Object> strategyParams = Map.of("minimumProfit", 0.01);
        final BotConfig botConfig = new BotConfig(accountId, figies, candleInterval, DecimalUtils.ZERO, StrategyType.CROSS, strategyParams);
        final BackTestResult result = createBackTestResult(botConfig, share1.currency());

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
        assertCommonStatistics(figies, result, rowIterator);
        assertPositions(result, rowIterator);
        assertOperations(result, rowIterator);
        assertMergedRegions(result, sheet);
        assertChartCreated(sheet);
    }

    @Test
    void saveBackTestResults_skipsErrorMessage_whenErrorIsEmpty() throws IOException {
        final Share share1 = TestShares.APPLE.share();
        final Share share2 = TestShares.SBER.share();

        final String figi1 = share1.figi();
        final String figi2 = share2.figi();

        final String accountId = TestAccounts.TINKOFF.getId();
        final List<String> figies = List.of(figi1, figi2);
        final CandleInterval candleInterval = CandleInterval.CANDLE_INTERVAL_1_MIN;
        final Map<String, Object> strategyParams = Map.of("minimumProfit", 0.01);
        final BotConfig botConfig = new BotConfig(accountId, figies, candleInterval, DecimalUtils.ZERO, StrategyType.CROSS, strategyParams);
        final BackTestResult result = createBackTestResult(botConfig, share1.currency());

        excelService.saveBackTestResults(List.of(result));

        Mockito.verify(excelFileService, Mockito.times(1))
                .saveToFile(workbookArgumentCaptor.capture(), Mockito.startsWith("BackTestResult"));

        final ExtendedWorkbook workbook = workbookArgumentCaptor.getValue();
        Assertions.assertEquals(1, workbook.getNumberOfSheets());

        final ExtendedSheet sheet = (ExtendedSheet) workbook.getSheetAt(0);

        final int expectedRowCount = getExpectedRowCount(result);
        Assertions.assertEquals(expectedRowCount, sheet.getRowsCount());

        final Iterator<Row> rowIterator = sheet.iterator();
        assertBotConfig(result.botConfig(), rowIterator);
        assertCommonStatistics(figies, result, rowIterator);
        assertPositions(result, rowIterator);
        assertOperations(result, rowIterator);
        assertMergedRegions(result, sheet);
        assertChartCreated(sheet);
    }

    @Test
    void saveBackTestResults_addsErrorMessage_whenErrorIsNotEmpty() throws IOException {
        final Share share1 = TestShares.APPLE.share();
        final Share share2 = TestShares.SBER.share();

        final String figi1 = share1.figi();
        final String figi2 = share2.figi();

        final String error = "Test error";

        final String accountId = TestAccounts.TINKOFF.getId();
        final List<String> figies = List.of(figi1, figi2);
        final CandleInterval candleInterval = CandleInterval.CANDLE_INTERVAL_1_MIN;
        final Map<String, Object> strategyParams = Map.of("minimumProfit", 0.01);
        final BotConfig botConfig = new BotConfig(accountId, figies, candleInterval, DecimalUtils.ZERO, StrategyType.CROSS, strategyParams);
        final BackTestResult result = createBackTestResult(botConfig, share1.currency(), error);

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
        assertCommonStatistics(figies, result, rowIterator);
        AssertUtils.assertRowValues(rowIterator.next(), "Текст ошибки", result.error());

        assertPositions(result, rowIterator);
        assertOperations(result, rowIterator);
        assertMergedRegions(result, sheet);
        assertChartCreated(sheet);
    }

    @Test
    void saveBackTestResults_skipsChart_whenNoCandles() throws IOException {
        final Share share1 = TestShares.APPLE.share();
        final Share share2 = TestShares.SBER.share();

        final String figi1 = share1.figi();
        final String figi2 = share2.figi();

        final String accountId = TestAccounts.TINKOFF.getId();
        final List<String> figies = List.of(figi1, figi2);
        final CandleInterval candleInterval = CandleInterval.CANDLE_INTERVAL_1_MIN;
        final Map<String, Object> strategyParams = Map.of("minimumProfit", 0.01);
        final BotConfig botConfig = new BotConfig(accountId, figies, candleInterval, DecimalUtils.ZERO, StrategyType.CROSS, strategyParams);
        final BackTestResult result = createBackTestResult(botConfig, share1.currency(), Collections.emptyMap());

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
        assertCommonStatistics(figies, result, rowIterator);
        assertPositions(result, rowIterator);
        assertOperations(result, rowIterator);
        assertMergedRegions(result, sheet);

        Assertions.assertNull(sheet.getDrawingPatriarch());
    }

    @Test
    void saveBackTestResults_skipsChart_whenCandlesAreEmpty() throws IOException {
        final Share share1 = TestShares.APPLE.share();
        final Share share2 = TestShares.SBER.share();

        final String figi1 = share1.figi();
        final String figi2 = share2.figi();

        final String accountId = TestAccounts.TINKOFF.getId();
        final List<String> figies = List.of(figi1, figi2);
        final CandleInterval candleInterval = CandleInterval.CANDLE_INTERVAL_1_MIN;
        final Map<String, Object> strategyParams = Map.of("minimumProfit", 0.01);
        final BotConfig botConfig = new BotConfig(accountId, figies, candleInterval, DecimalUtils.ZERO, StrategyType.CROSS, strategyParams);

        final BackTestResult result = createBackTestResult(botConfig, share1.currency(), Map.of(share1.currency(), Collections.emptyList()));

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
        assertCommonStatistics(figies, result, rowIterator);
        assertPositions(result, rowIterator);
        assertOperations(result, rowIterator);
        assertMergedRegions(result, sheet);

        Assertions.assertNull(sheet.getDrawingPatriarch());
    }

    @Test
    void saveBackTestResults_catchesIOExceptionOfFileSaving() throws IOException {
        final Share share1 = TestShares.APPLE.share();
        final Share share2 = TestShares.SBER.share();

        final String figi1 = share1.figi();
        final String figi2 = share2.figi();

        final String accountId = TestAccounts.TINKOFF.getId();
        final List<String> figies = List.of(figi1, figi2);
        final CandleInterval candleInterval = CandleInterval.CANDLE_INTERVAL_1_MIN;
        final BotConfig botConfig = new BotConfig(accountId, figies, candleInterval, DecimalUtils.ZERO, StrategyType.CROSS, Collections.emptyMap());

        final BackTestResult result1 = createBackTestResult(botConfig, share1.currency());
        final BackTestResult result2 = createBackTestResult(botConfig, share1.currency());
        final BackTestResult result3 = createBackTestResult(botConfig, share1.currency());
        final List<BackTestResult> results = List.of(result1, result2, result3);

        Mockito.doThrow(new IOException())
                .when(excelFileService)
                .saveToFile(Mockito.any(Workbook.class), Mockito.startsWith("BackTestResul"));

        Assertions.assertDoesNotThrow(() -> excelService.saveBackTestResults(results));
    }

    @SuppressWarnings("SameParameterValue")
    private static BackTestResult createBackTestResult(final BotConfig botConfig, final String currency, final String error) {
        final Map<String, Balances> balances = createBalances(currency);
        final Map<String, Profits> profits = createProfits(currency);
        final List<Position> positions = createPositions(botConfig);
        final Map<String, List<Operation>> operations = createOperations(botConfig);
        final Map<String, List<Candle>> candles = createCandles(botConfig);

        return new BackTestResult(
                botConfig,
                createInterval(),
                balances,
                profits,
                positions,
                operations,
                candles,
                error
        );
    }

    private static BackTestResult createBackTestResult(final BotConfig botConfig, final String currency, final Map<String, List<Candle>> candles) {
        final Map<String, Balances> balances = createBalances(currency);
        final Map<String, Profits> profits = createProfits(currency);
        final List<Position> positions = createPositions(botConfig);
        final Map<String, List<Operation>> operations = createOperations(botConfig);

        return new BackTestResult(
                botConfig,
                createInterval(),
                balances,
                profits,
                positions,
                operations,
                candles,
                null
        );
    }

    private static BackTestResult createBackTestResult(final BotConfig botConfig, final String currency) {
        final Map<String, Balances> balances = createBalances(currency);
        final Map<String, Profits> profits = createProfits(currency);
        final List<Position> positions = createPositions(botConfig);
        final Map<String, List<Operation>> operations = createOperations(botConfig);
        final Map<String, List<Candle>> candles = createCandles(botConfig);

        return new BackTestResult(
                botConfig,
                createInterval(),
                balances,
                profits,
                positions,
                operations,
                candles,
                null
        );
    }

    private static BackTestResult createBackTestResultWithoutPositions(final BotConfig botConfig, final String currency) {
        final Map<String, Balances> balances = createBalances(currency);
        final Map<String, Profits> profits = createProfits(currency);
        final Map<String, List<Operation>> operations = createOperations(botConfig);
        final Map<String, List<Candle>> candles = createCandles(botConfig);

        return new BackTestResult(
                botConfig,
                createInterval(),
                balances,
                profits,
                Collections.emptyList(),
                operations,
                candles,
                null
        );
    }

    private static BackTestResult createBackTestResultWithoutOperations(final BotConfig botConfig, final String currency) {
        final Map<String, Balances> balances = createBalances(currency);
        final Map<String, Profits> profits = createProfits(currency);
        final List<Position> positions = createPositions(botConfig);
        final Map<String, List<Operation>> operations = botConfig.figies().stream()
                .collect(MapUtils.newMapValueCollector(figi -> Collections.emptyList()));
        final Map<String, List<Candle>> candles = createCandles(botConfig);

        return new BackTestResult(
                botConfig,
                createInterval(),
                balances,
                profits,
                positions,
                operations,
                candles,
                null
        );
    }

    private static Map<String, Balances> createBalances(final String currency) {
        final Balances balances = new Balances(
                DecimalUtils.setDefaultScale(700L),
                DecimalUtils.setDefaultScale(800L),
                DecimalUtils.setDefaultScale(750L),
                DecimalUtils.setDefaultScale(200L),
                DecimalUtils.setDefaultScale(1000L)
        );
        return Map.of(currency, balances);
    }

    private static Map<String, Profits> createProfits(final String currency) {
        final Profits profits = new Profits(DecimalUtils.setDefaultScale(300L), 0.25, 6.0);
        return Map.of(currency, profits);
    }

    private static List<Position> createPositions(final BotConfig botConfig) {
        return botConfig.figies().stream()
                .map(ExcelServiceImplUnitTest::createPosition)
                .toList();
    }

    private static Map<String, List<Operation>> createOperations(final BotConfig botConfig) {
        return botConfig.figies().stream()
                .collect(MapUtils.newMapValueCollector(ExcelServiceImplUnitTest::createBackTestOperations));
    }

    private static Map<String, List<Candle>> createCandles(final BotConfig botConfig) {
        return botConfig.figies().stream()
                .collect(MapUtils.newMapValueCollector(figi -> createCandles()));
    }

    private static Interval createInterval() {
        final OffsetDateTime from = DateTimeTestData.newDateTime(2020, 10, 1);
        final OffsetDateTime to = DateTimeTestData.newDateTime(2020, 10, 15);
        return Interval.of(from, to);
    }

    private static Position createPosition(final String figi) {
        return new PositionBuilder()
                .setFigi(figi)
                .setQuantity(3)
                .setCurrentPrice(200)
                .build();
    }

    private static List<Operation> createBackTestOperations(String figi) {
        Operation operation1 = Operation.newBuilder()
                .setFigi(figi)
                .setDate(DateTimeTestData.newTimestamp(2020, 10, 1, 10))
                .setOperationType(OperationType.OPERATION_TYPE_BUY)
                .setPrice(TestData.newMoneyValue(150, ""))
                .setQuantity(1L)
                .build();
        Operation operation2 = Operation.newBuilder()
                .setFigi(figi)
                .setDate(DateTimeTestData.newTimestamp(2020, 10, 5, 10, 11))
                .setOperationType(OperationType.OPERATION_TYPE_SELL)
                .setPrice(TestData.newMoneyValue(180, ""))
                .setQuantity(1L)
                .build();
        Operation operation3 = Operation.newBuilder()
                .setFigi(figi)
                .setDate(DateTimeTestData.newTimestamp(2020, 10, 10, 10, 50))
                .setOperationType(OperationType.OPERATION_TYPE_BUY)
                .setPrice(TestData.newMoneyValue(160, ""))
                .setQuantity(3L)
                .build();
        Operation operation4 = Operation.newBuilder()
                .setFigi(figi)
                .setDate(DateTimeTestData.newTimestamp(2020, 11, 1, 10))
                .setOperationType(OperationType.OPERATION_TYPE_BUY)
                .setPrice(TestData.newMoneyValue(120, ""))
                .setQuantity(2L)
                .build();

        return List.of(operation1, operation2, operation3, operation4);
    }

    private static List<Candle> createCandles() {
        final Candle candle1 = new CandleBuilder()
                .setOpen(150)
                .setTime(DateTimeTestData.newDateTime(2020, 10, 1, 10))
                .build();
        final Candle candle2 = new CandleBuilder()
                .setOpen(160)
                .setTime(DateTimeTestData.newDateTime(2020, 10, 1, 11))
                .build();
        final Candle candle3 = new CandleBuilder()
                .setOpen(180)
                .setTime(DateTimeTestData.newDateTime(2020, 10, 5, 10, 11))
                .build();
        final Candle candle4 = new CandleBuilder()
                .setOpen(160)
                .setTime(DateTimeTestData.newDateTime(2020, 10, 10, 10, 50))
                .build();
        final Candle candle5 = new CandleBuilder()
                .setOpen(120)
                .setTime(DateTimeTestData.newDateTime(2020, 11, 1, 10))
                .build();

        return List.of(candle1, candle2, candle3, candle4, candle5);
    }

    private static int getExpectedRowCount(final BackTestResult result) {
        return CONSTANT_ROWS_COUNT +
                getStrategyParamsRowCount(result.botConfig().strategyParams()) +
                getBalancesRowCount(result.balances()) +
                getProfitsRowCount(result.profits()) +
                getPositionsRowCount(result.positions()) +
                getExpectedOperationsRowCount(result.operations()) +
                (StringUtils.isEmpty(result.error()) ? 0 : 1);
    }

    private static int getStrategyParamsRowCount(final Map<String, Object> strategyParams) {
        return strategyParams.isEmpty()
                ? 0
                : 1 + strategyParams.size();
    }

    private static int getBalancesRowCount(final Map<String, Balances> balances) {
        return 1 + balances.size() * 6;
    }

    private static int getProfitsRowCount(final Map<String, Profits> profits) {
        return 1 + profits.size() * 4;
    }

    private static int getPositionsRowCount(final List<Position> positions) {
        return positions.isEmpty()
                ? 1
                : 2 + positions.size();
    }

    private static int getExpectedOperationsRowCount(final Map<String, List<Operation>> operations) {
        return operations.values().stream()
                .map(operationList -> operationList.size() + 3)
                .reduce(Integer::sum)
                .orElseThrow();
    }

    private static void assertBotConfig(final BotConfig botConfig, final Iterator<Row> rowIterator) {
        AssertUtils.assertRowValues(rowIterator.next(), "Конфигурация");
        AssertUtils.assertRowValues(rowIterator.next(), "Размер свечи", botConfig.candleInterval().toString());
        AssertUtils.assertRowValues(rowIterator.next(), "Стратегия", botConfig.strategyType().toString());
        assertStrategyParams(botConfig.strategyParams(), rowIterator);
    }

    private static void assertStrategyParams(final Map<String, Object> strategyParams, final Iterator<Row> rowIterator) {
        if (strategyParams.isEmpty()) {
            return;
        }

        AssertUtils.assertRowValues(rowIterator.next(), "Параметры стратегии");
        for (final Map.Entry<String, Object> entry : strategyParams.entrySet()) {
            AssertUtils.assertRowValues(rowIterator.next(), entry.getKey(), entry.getValue());
        }
    }

    @SuppressWarnings("SameParameterValue")
    private static void assertCommonStatistics(final List<String> figies, final BackTestResult result, final Iterator<Row> rowIterator) {
        AssertUtils.assertRowValues(rowIterator.next());
        AssertUtils.assertRowValues(rowIterator.next(), "Общая статистика");
        AssertUtils.assertRowValues(rowIterator.next(), "Счёт", result.botConfig().accountId());
        AssertUtils.assertRowValues(rowIterator.next(), "FIGIes", String.join(", ", figies));
        AssertUtils.assertRowValues(rowIterator.next(), "Интервал", result.interval().toPrettyString());
        AssertUtils.assertRowValues(rowIterator.next());
        assertBalances(result, rowIterator);
        AssertUtils.assertRowValues(rowIterator.next());
        assertProfits(result, rowIterator);
    }

    private static void assertBalances(final BackTestResult result, final Iterator<Row> rowIterator) {
        AssertUtils.assertRowValues(rowIterator.next(), "Балансы");
        for (final Map.Entry<String, Balances> entry : result.balances().entrySet()) {
            AssertUtils.assertRowValues(rowIterator.next(), "Валюта", entry.getKey());
            final Balances balances = entry.getValue();
            AssertUtils.assertRowValues(rowIterator.next(), "Начальный баланс", balances.initialInvestment());
            AssertUtils.assertRowValues(rowIterator.next(), "Вложения", balances.totalInvestment());
            AssertUtils.assertRowValues(rowIterator.next(), "Итоговый общий баланс", balances.finalTotalSavings());
            AssertUtils.assertRowValues(rowIterator.next(), "Итоговый валютный баланс", balances.finalBalance());
            AssertUtils.assertRowValues(rowIterator.next(), "Средневзвешенные вложения", balances.weightedAverageInvestment());
        }
    }

    private static void assertProfits(final BackTestResult result, final Iterator<Row> rowIterator) {
        AssertUtils.assertRowValues(rowIterator.next(), "Доходы");
        for (final Map.Entry<String, Profits> entry : result.profits().entrySet()) {
            AssertUtils.assertRowValues(rowIterator.next(), "Валюта", entry.getKey());
            final Profits profits = entry.getValue();
            AssertUtils.assertRowValues(rowIterator.next(), "Абсолютный доход", profits.absolute());
            AssertUtils.assertRowValues(rowIterator.next(), "Относительный доход", profits.relative());
            AssertUtils.assertRowValues(rowIterator.next(), "Относительный годовой доход", profits.relativeAnnual());
        }
    }

    private static void assertPositions(final BackTestResult result, final Iterator<Row> rowIterator) {
        AssertUtils.assertRowValues(rowIterator.next());
        if (result.positions().isEmpty()) {
            AssertUtils.assertRowValues(rowIterator.next(), "Позиции отсутствуют");
        } else {
            AssertUtils.assertRowValues(rowIterator.next(), "Позиции");
            AssertUtils.assertRowValues(rowIterator.next(), "Цена", "Количество");
            for (final Position position : result.positions()) {
                AssertUtils.assertRowValues(rowIterator.next(), position.getCurrentPrice().getValue(), position.getQuantity());
            }
        }
    }

    private static void assertOperations(final BackTestResult result, final Iterator<Row> rowIterator) {
        AssertUtils.assertRowValues(rowIterator.next());

        for (final Map.Entry<String, List<Operation>> entry : result.operations().entrySet()) {
            AssertUtils.assertRowValues(rowIterator.next(), "Операции с " + entry.getKey());
            AssertUtils.assertRowValues(rowIterator.next(), "Дата и время", "Тип операции", "Цена", "Количество");
            for (final Operation operation : entry.getValue()) {
                AssertUtils.assertRowValues(
                        rowIterator.next(),
                        operation.getDate(),
                        operation.getOperationType().name(),
                        DecimalUtils.newBigDecimal(operation.getPrice()).doubleValue(),
                        operation.getQuantity()
                );
            }
            AssertUtils.assertRowValues(rowIterator.next());
        }
    }

    private static void assertMergedRegions(final BackTestResult result, final ExtendedSheet sheet) {
        final List<CellRangeAddress> mergedRegions = sheet.getMergedRegions();

        int regionIndex = 0;

        final CellRangeAddress configurationMergedRegion = mergedRegions.get(regionIndex++);
        Assertions.assertEquals(configurationMergedRegion.getFirstRow(), configurationMergedRegion.getLastRow());
        Assertions.assertEquals(0, configurationMergedRegion.getFirstColumn());
        Assertions.assertEquals(1, configurationMergedRegion.getLastColumn());
        Assertions.assertEquals("Конфигурация", getRegionFirstCellStringValue(sheet, configurationMergedRegion));

        if (!result.botConfig().strategyParams().isEmpty()) {
            final CellRangeAddress strategyParamsRegion = mergedRegions.get(regionIndex++);
            Assertions.assertEquals(strategyParamsRegion.getFirstRow(), strategyParamsRegion.getLastRow());
            Assertions.assertEquals(0, strategyParamsRegion.getFirstColumn());
            Assertions.assertEquals(1, strategyParamsRegion.getLastColumn());
            final String stringCellValue = getRegionFirstCellStringValue(sheet, strategyParamsRegion);
            Assertions.assertEquals("Параметры стратегии", stringCellValue);
        }

        final CellRangeAddress commonStatisticsRegion = mergedRegions.get(regionIndex++);
        Assertions.assertEquals(commonStatisticsRegion.getFirstRow(), commonStatisticsRegion.getLastRow());
        Assertions.assertEquals(0, commonStatisticsRegion.getFirstColumn());
        Assertions.assertEquals(1, commonStatisticsRegion.getLastColumn());
        Assertions.assertEquals("Общая статистика", getRegionFirstCellStringValue(sheet, commonStatisticsRegion));

        final CellRangeAddress balancesRegion = mergedRegions.get(regionIndex++);
        Assertions.assertEquals(balancesRegion.getFirstRow(), balancesRegion.getLastRow());
        Assertions.assertEquals(0, balancesRegion.getFirstColumn());
        Assertions.assertEquals(1, balancesRegion.getLastColumn());
        Assertions.assertEquals("Балансы", getRegionFirstCellStringValue(sheet, balancesRegion));

        final CellRangeAddress profitsRegion = mergedRegions.get(regionIndex++);
        Assertions.assertEquals(profitsRegion.getFirstRow(), profitsRegion.getLastRow());
        Assertions.assertEquals(0, profitsRegion.getFirstColumn());
        Assertions.assertEquals(1, profitsRegion.getLastColumn());
        Assertions.assertEquals("Доходы", getRegionFirstCellStringValue(sheet, profitsRegion));

        if (!result.positions().isEmpty()) {
            final CellRangeAddress positionsRegion = mergedRegions.get(regionIndex++);
            Assertions.assertEquals(positionsRegion.getFirstRow(), positionsRegion.getLastRow());
            Assertions.assertEquals(0, positionsRegion.getFirstColumn());
            Assertions.assertEquals(1, positionsRegion.getLastColumn());
            Assertions.assertEquals("Позиции", getRegionFirstCellStringValue(sheet, positionsRegion));
        }

        for (final String figi : result.operations().keySet()) {
            final CellRangeAddress positionsRegion = mergedRegions.get(regionIndex++);
            Assertions.assertEquals(positionsRegion.getFirstRow(), positionsRegion.getLastRow());
            Assertions.assertEquals(0, positionsRegion.getFirstColumn());
            Assertions.assertEquals(3, positionsRegion.getLastColumn());
            Assertions.assertEquals("Операции с " + figi, getRegionFirstCellStringValue(sheet, positionsRegion));
        }

        Assertions.assertEquals(regionIndex, mergedRegions.size());
    }

    private static String getRegionFirstCellStringValue(final ExtendedSheet sheet, final CellRangeAddress strategyParamsRegion) {
        return sheet.getRow(strategyParamsRegion.getFirstRow()).getCell(0).getStringCellValue();
    }

    // endregion

    // region saveCandles tests

    @Test
    void saveCandles_createsAndSaveWorkbook() throws IOException {
        final String figi = TestShares.APPLE.share().figi();

        final OffsetDateTime from = DateTimeTestData.newDateTime(2021, 1, 1);
        final OffsetDateTime to = DateTimeTestData.newDateTime(2021, 2, 1);
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

        final OffsetDateTime from = DateTimeTestData.newDateTime(2021, 1, 1);
        final OffsetDateTime to = DateTimeTestData.newDateTime(2021, 2, 1);
        final Interval interval = Interval.of(from, to);

        final GetCandlesResponse response = createGetCandlesResponse();

        final String fileNamePrefix = "Candles for FIGI '" + figi + "'";
        Mockito.doThrow(new IOException())
                .when(excelFileService)
                .saveToFile(Mockito.any(Workbook.class), Mockito.startsWith(fileNamePrefix));

        Assertions.assertDoesNotThrow(() -> excelService.saveCandles(figi, interval, response));
    }

    private GetCandlesResponse createGetCandlesResponse() {
        final List<Candle> candles = createCandles();
        final List<BigDecimal> opens = candles.stream().map(Candle::getOpen).toList();
        final List<BigDecimal> shortAverages = averager.getAverages(opens, 2);
        final List<BigDecimal> longAverages = averager.getAverages(opens, 5);

        return new GetCandlesResponse(candles, shortAverages, longAverages);
    }

    // endregion

    private static void assertChartCreated(ExtendedSheet sheet) {
        final Iterator<?> iterator = sheet.getDrawingPatriarch().iterator();
        final XSSFGraphicFrame frame = (XSSFGraphicFrame) iterator.next();
        Assertions.assertFalse(iterator.hasNext());

        final List<XSSFChart> charts = frame.getDrawing().getCharts();
        Assertions.assertEquals(1, charts.size());

        final XSSFChart chart = charts.getFirst();
        final List<? extends XDDFChartAxis> axes = chart.getAxes();
        Assertions.assertEquals(2, axes.size());
        AssertUtils.assertEquals(120, axes.get(1).getMinimum());
        AssertUtils.assertEquals(180, axes.get(1).getMaximum());
    }

}