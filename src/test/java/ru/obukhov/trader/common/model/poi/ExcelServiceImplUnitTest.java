package ru.obukhov.trader.common.model.poi;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xddf.usermodel.chart.XDDFChartAxis;
import org.apache.poi.xssf.usermodel.XSSFChart;
import org.apache.poi.xssf.usermodel.XSSFGraphicFrame;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import ru.obukhov.trader.BaseMockedTest;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.common.service.impl.ExcelServiceImpl;
import ru.obukhov.trader.common.service.interfaces.ExcelFileService;
import ru.obukhov.trader.common.util.DateUtils;
import ru.obukhov.trader.common.util.TrendUtils;
import ru.obukhov.trader.market.model.Candle;
import ru.obukhov.trader.market.model.ExtendedCandle;
import ru.obukhov.trader.market.model.Extremum;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.test.utils.TestDataHelper;
import ru.obukhov.trader.web.model.pojo.SimulatedOperation;
import ru.obukhov.trader.web.model.pojo.SimulatedPosition;
import ru.obukhov.trader.web.model.pojo.SimulationResult;
import ru.tinkoff.invest.openapi.model.rest.OperationType;

import java.io.File;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

class ExcelServiceImplUnitTest extends BaseMockedTest {

    @Captor
    private ArgumentCaptor<ExtendedWorkbook> workbookArgumentCaptor;
    @Mock
    private ExcelFileService excelFileService;
    @Mock
    private File file;

    private ExcelServiceImpl excelService;

    @BeforeEach
    public void setUp() {
        this.excelService = new ExcelServiceImpl(excelFileService);
    }

    // region saveSimulationResult tests

    @Test
    void saveSimulationResult_savesMultipleResults() {

        final String ticker = "ticker";

        SimulationResult result1 = createSimulationResult(ticker, "bot1");
        SimulationResult result2 = createSimulationResult(ticker, "bot2");
        SimulationResult result3 = createSimulationResult(ticker, "bot3");
        List<SimulationResult> results = Arrays.asList(result1, result2, result3);

        final String fileNamePrefix = "SimulationResult for '" + ticker + "'";

        Mockito.when(excelFileService.saveToFile(workbookArgumentCaptor.capture(), Mockito.startsWith(fileNamePrefix)))
                .thenReturn(file);

        File returnedFile = excelService.saveSimulationResults(ticker, results);

        Assertions.assertEquals(file, returnedFile);

        ExtendedWorkbook workbook = workbookArgumentCaptor.getValue();
        Assertions.assertEquals(results.size(), workbook.getNumberOfSheets());

        for (SimulationResult result : results) {
            ExtendedSheet sheet = (ExtendedSheet) workbook.getSheet(result.getBotName());

            int expectedRowCount = 17 + result.getPositions().size() + result.getOperations().size();
            Assertions.assertEquals(expectedRowCount, sheet.getRowsCount());

            Iterator<Row> rowIterator = sheet.iterator();
            assertCommonStatistics(ticker, result, rowIterator);
            assertPositions(result, rowIterator);
            assertOperations(result, rowIterator);
            assertMergedRegions(sheet);
            assertChartCreated(sheet);
        }
    }

    @Test
    void saveSimulationResult_skipsErrorMessage_whenErrorIsNull() {
        final String ticker = "ticker";

        SimulationResult result = createSimulationResult(ticker, "bot");

        final String fileNamePrefix = "SimulationResult for '" + ticker + "'";

        Mockito.when(excelFileService.saveToFile(workbookArgumentCaptor.capture(), Mockito.startsWith(fileNamePrefix)))
                .thenReturn(file);

        File returnedFile = excelService.saveSimulationResults(ticker, Collections.singletonList(result));

        Assertions.assertEquals(file, returnedFile);

        ExtendedWorkbook workbook = workbookArgumentCaptor.getValue();
        Assertions.assertEquals(1, workbook.getNumberOfSheets());

        ExtendedSheet sheet = (ExtendedSheet) workbook.getSheet(result.getBotName());

        int expectedRowCount = 17 + result.getPositions().size() + result.getOperations().size();
        Assertions.assertEquals(expectedRowCount, sheet.getRowsCount());

        Iterator<Row> rowIterator = sheet.iterator();
        assertCommonStatistics(ticker, result, rowIterator);
        assertPositions(result, rowIterator);
        assertOperations(result, rowIterator);
        assertMergedRegions(sheet);
        assertChartCreated(sheet);
    }

    @Test
    void saveSimulationResult_skipsErrorMessage_whenErrorIsEmpty() {
        final String ticker = "ticker";

        SimulationResult result = createSimulationResult(ticker, "bot");
        result.setError(StringUtils.EMPTY);

        final String fileNamePrefix = "SimulationResult for '" + ticker + "'";

        Mockito.when(excelFileService.saveToFile(workbookArgumentCaptor.capture(), Mockito.startsWith(fileNamePrefix)))
                .thenReturn(file);

        File returnedFile = excelService.saveSimulationResults(ticker, Collections.singletonList(result));

        Assertions.assertEquals(file, returnedFile);

        ExtendedWorkbook workbook = workbookArgumentCaptor.getValue();
        Assertions.assertEquals(1, workbook.getNumberOfSheets());

        ExtendedSheet sheet = (ExtendedSheet) workbook.getSheet(result.getBotName());

        int expectedRowCount = 17 + result.getPositions().size() + result.getOperations().size();
        Assertions.assertEquals(expectedRowCount, sheet.getRowsCount());

        Iterator<Row> rowIterator = sheet.iterator();
        assertCommonStatistics(ticker, result, rowIterator);
        assertPositions(result, rowIterator);
        assertOperations(result, rowIterator);
        assertMergedRegions(sheet);
        assertChartCreated(sheet);
    }

    @Test
    void saveSimulationResult_addsErrorMessage_whenErrorIsNotEmpty() {
        final String ticker = "ticker";
        final String error = "error";

        SimulationResult result = createSimulationResult(ticker, "bot");
        result.setError(error);

        final String fileNamePrefix = "SimulationResult for '" + ticker + "'";

        Mockito.when(excelFileService.saveToFile(workbookArgumentCaptor.capture(), Mockito.startsWith(fileNamePrefix)))
                .thenReturn(file);

        File returnedFile = excelService.saveSimulationResults(ticker, Collections.singletonList(result));

        Assertions.assertEquals(file, returnedFile);

        ExtendedWorkbook workbook = workbookArgumentCaptor.getValue();
        Assertions.assertEquals(1, workbook.getNumberOfSheets());

        ExtendedSheet sheet = (ExtendedSheet) workbook.getSheet(result.getBotName());

        int expectedRowCount = 18 + result.getPositions().size() + result.getOperations().size();
        Assertions.assertEquals(expectedRowCount, sheet.getRowsCount());

        Iterator<Row> rowIterator = sheet.iterator();
        assertCommonStatistics(ticker, result, rowIterator);
        AssertUtils.assertRowValues(rowIterator.next(), "Текст ошибки", result.getError());

        assertPositions(result, rowIterator);
        assertOperations(result, rowIterator);
        assertMergedRegions(sheet);

        assertChartCreated(sheet);
    }

    @Test
    void saveSimulationResult_skipsChart_whenCandlesAreNull() {

        final String ticker = "ticker";

        SimulationResult result = createSimulationResult(ticker, "bot");
        result.setCandles(null);

        final String fileNamePrefix = "SimulationResult for '" + ticker + "'";

        Mockito.when(excelFileService.saveToFile(workbookArgumentCaptor.capture(), Mockito.startsWith(fileNamePrefix)))
                .thenReturn(file);

        File returnedFile = excelService.saveSimulationResults(ticker, Collections.singletonList(result));

        Assertions.assertEquals(file, returnedFile);

        ExtendedWorkbook workbook = workbookArgumentCaptor.getValue();
        Assertions.assertEquals(1, workbook.getNumberOfSheets());

        ExtendedSheet sheet = (ExtendedSheet) workbook.getSheet(result.getBotName());

        int expectedRowCount = 17 + result.getPositions().size() + result.getOperations().size();
        Assertions.assertEquals(expectedRowCount, sheet.getRowsCount());

        Iterator<Row> rowIterator = sheet.iterator();
        assertCommonStatistics(ticker, result, rowIterator);
        assertPositions(result, rowIterator);
        assertOperations(result, rowIterator);
        assertMergedRegions(sheet);

        Assertions.assertNull(sheet.getDrawingPatriarch());
    }

    @Test
    void saveSimulationResult_skipsChart_whenCandlesAreEmpty() {

        final String ticker = "ticker";

        SimulationResult result = createSimulationResult(ticker, "bot");
        result.setCandles(Collections.emptyList());

        final String fileNamePrefix = "SimulationResult for '" + ticker + "'";

        Mockito.when(excelFileService.saveToFile(workbookArgumentCaptor.capture(), Mockito.startsWith(fileNamePrefix)))
                .thenReturn(file);

        File returnedFile = excelService.saveSimulationResults(ticker, Collections.singletonList(result));

        Assertions.assertEquals(file, returnedFile);

        ExtendedWorkbook workbook = workbookArgumentCaptor.getValue();
        Assertions.assertEquals(1, workbook.getNumberOfSheets());

        ExtendedSheet sheet = (ExtendedSheet) workbook.getSheet(result.getBotName());

        int expectedRowCount = 17 + result.getPositions().size() + result.getOperations().size();
        Assertions.assertEquals(expectedRowCount, sheet.getRowsCount());

        Iterator<Row> rowIterator = sheet.iterator();
        assertCommonStatistics(ticker, result, rowIterator);
        assertPositions(result, rowIterator);
        assertOperations(result, rowIterator);
        assertMergedRegions(sheet);

        Assertions.assertNull(sheet.getDrawingPatriarch());
    }

    private void assertCommonStatistics(String ticker, SimulationResult result, Iterator<Row> rowIterator) {
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
        for (SimulatedPosition position : result.getPositions()) {
            AssertUtils.assertRowValues(rowIterator.next(), position.getPrice(), position.getQuantity());
        }
    }

    private void assertOperations(SimulationResult result, Iterator<Row> rowIterator) {
        AssertUtils.assertRowValues(rowIterator.next());
        AssertUtils.assertRowValues(rowIterator.next(), "Операции");
        AssertUtils.assertRowValues(rowIterator.next(),
                "Дата и время", "Тип операции", "Цена", "Количество", "Комиссия");
        for (SimulatedOperation operation : result.getOperations()) {
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
        List<CellRangeAddress> mergedRegions = sheet.getMergedRegions();
        Assertions.assertEquals(3, mergedRegions.size());
    }

    // endregion

    @Test
    void saveCandles() {
        final String ticker = "ticker";

        final OffsetDateTime from = DateUtils.getDateTime(2021, 1, 1, 0, 0, 0);
        final OffsetDateTime to = DateUtils.getDateTime(2021, 2, 1, 0, 0, 0);
        final Interval interval = Interval.of(from, to);

        final List<ExtendedCandle> extendedCandles = createExtendedCandles();

        final String fileNamePrefix = "Candles for '" + ticker + "'";

        Mockito.when(excelFileService.saveToFile(workbookArgumentCaptor.capture(), Mockito.startsWith(fileNamePrefix)))
                .thenReturn(file);

        File returnedFile = excelService.saveCandles(ticker, interval, extendedCandles);

        Assertions.assertSame(file, returnedFile);

        ExtendedWorkbook workbook = workbookArgumentCaptor.getValue();
        Assertions.assertEquals(1, workbook.getNumberOfSheets());
        ExtendedSheet sheet = (ExtendedSheet) workbook.getSheet(ticker);

        Assertions.assertEquals(10, sheet.getRowsCount());

        Iterator<Row> rowIterator = sheet.iterator();
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
                "Наименьшая цена",
                "Усреднённая цена",
                "Экстремум"
        );
        for (ExtendedCandle candle : extendedCandles) {
            AssertUtils.assertRowValues(
                    rowIterator.next(),
                    candle.getTime(),
                    candle.getOpenPrice(),
                    candle.getClosePrice(),
                    candle.getHighestPrice(),
                    candle.getLowestPrice(),
                    candle.getAveragePrice(),
                    getLocalExtremumString(candle)
            );
        }

        assertChartCreated(sheet);
    }

    private String getLocalExtremumString(ExtendedCandle candle) {
        if (candle.isLocalMaximum()) {
            return "макс";
        } else {
            return candle.isLocalMinimum() ? "мин" : null;
        }
    }

    private SimulationResult createSimulationResult(String ticker, String botName) {
        return SimulationResult.builder()
                .botName(botName)
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
        OffsetDateTime from = DateUtils.getDate(2020, 10, 1);
        OffsetDateTime to = DateUtils.getDate(2020, 10, 15);
        return Interval.of(from, to);
    }

    private List<SimulatedPosition> createPositions(String ticker) {
        return Arrays.asList(
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

        return Arrays.asList(operation1, operation2, operation3, operation4);
    }

    private List<Candle> createCandles() {
        Candle candle1 = TestDataHelper.createCandleWithOpenPriceAndTime(
                150,
                DateUtils.getDateTime(2020, 10, 1, 10, 0, 0)
        );
        Candle candle2 = TestDataHelper.createCandleWithOpenPriceAndTime(
                160,
                DateUtils.getDateTime(2020, 10, 1, 11, 0, 0)
        );
        Candle candle3 = TestDataHelper.createCandleWithOpenPriceAndTime(
                180,
                DateUtils.getDateTime(2020, 10, 5, 10, 11, 0)
        );
        Candle candle4 = TestDataHelper.createCandleWithOpenPriceAndTime(
                160,
                DateUtils.getDateTime(2020, 10, 10, 10, 50, 0)
        );
        Candle candle5 = TestDataHelper.createCandleWithOpenPriceAndTime(
                120,
                DateUtils.getDateTime(2020, 11, 1, 10, 0, 0)
        );

        return Arrays.asList(candle1, candle2, candle3, candle4, candle5);
    }

    private List<ExtendedCandle> createExtendedCandles() {
        final List<Candle> candles = createCandles();
        final List<BigDecimal> openPrices = candles.stream().map(Candle::getOpenPrice).collect(Collectors.toList());
        Function<BigDecimal, BigDecimal> selfExtractor = (BigDecimal number) -> number;
        final List<BigDecimal> averages = TrendUtils.getSimpleMovingAverages(openPrices, selfExtractor, 5);

        final List<ExtendedCandle> extendedCandles = new ArrayList<>(candles.size());
        for (int i = 0; i < candles.size(); i++) {
            extendedCandles.add(new ExtendedCandle(candles.get(i), averages.get(i)));
        }

        final List<Integer> maximums = TrendUtils.getLocalExtremes(averages, selfExtractor, Comparator.naturalOrder());
        for (Integer maximum : maximums) {
            extendedCandles.get(maximum).setExtremum(Extremum.MAX);
        }

        final List<Integer> minimums = TrendUtils.getLocalExtremes(averages, selfExtractor, Comparator.reverseOrder());
        for (Integer minimum : minimums) {
            extendedCandles.get(minimum).setExtremum(Extremum.MIN);
        }

        return extendedCandles;
    }

    private void assertChartCreated(ExtendedSheet sheet) {
        Iterator<?> iterator = sheet.getDrawingPatriarch().iterator();
        XSSFGraphicFrame frame = (XSSFGraphicFrame) iterator.next();
        Assertions.assertFalse(iterator.hasNext());
        List<XSSFChart> charts = frame.getDrawing().getCharts();
        Assertions.assertEquals(1, charts.size());
        XSSFChart chart = charts.get(0);
        List<? extends XDDFChartAxis> axes = chart.getAxes();
        Assertions.assertEquals(2, axes.size());
        AssertUtils.assertEquals(120, axes.get(1).getMinimum());
        AssertUtils.assertEquals(180, axes.get(1).getMaximum());
    }

}