package ru.obukhov.trader.common.model.poi;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xddf.usermodel.chart.XDDFChartAxis;
import org.apache.poi.xssf.usermodel.XSSFChart;
import org.apache.poi.xssf.usermodel.XSSFGraphicFrame;
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
import ru.obukhov.trader.market.model.Candle;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.web.model.pojo.SimulatedOperation;
import ru.obukhov.trader.web.model.pojo.SimulatedPosition;
import ru.obukhov.trader.web.model.pojo.SimulationResult;
import ru.tinkoff.invest.openapi.models.operations.OperationType;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.verify;
import static ru.obukhov.trader.test.utils.AssertUtils.assertRowValues;

class ExcelServiceImplTest extends BaseMockedTest {

    @Captor
    private ArgumentCaptor<ExtendedWorkbook> workbookArgumentCaptor;
    @Mock
    private ExcelFileService excelFileService;
    private ExcelServiceImpl excelService;

    @BeforeEach
    public void setUp() {
        this.excelService = new ExcelServiceImpl(excelFileService);
    }

    @Test
    void saveSimulationResult() {

        SimulationResult result = createSimulationResult();

        final String ticker = "ticker";
        excelService.saveSimulationResults(ticker, Collections.singletonList(result));

        String fileNamePrefix = "SimulationResult for '" + ticker + "'";
        verify(excelFileService).saveToFile(workbookArgumentCaptor.capture(), Mockito.startsWith(fileNamePrefix));

        ExtendedWorkbook workbook = workbookArgumentCaptor.getValue();
        assertEquals(1, workbook.getNumberOfSheets());
        ExtendedSheet sheet = (ExtendedSheet) workbook.getSheet(result.getBotName());

        int expectedRowCount = 17 + result.getPositions().size() + result.getOperations().size();
        assertEquals(expectedRowCount, sheet.getRowsCount());

        Iterator<Row> rowIterator = sheet.iterator();
        assertRowValues(rowIterator.next(), "Общая статистика");
        assertRowValues(rowIterator.next(), "Тикер", ticker);
        assertRowValues(rowIterator.next(), "Интервал", result.getInterval().toPrettyString());
        assertRowValues(rowIterator.next(), "Начальный баланс", result.getInitialBalance());
        assertRowValues(rowIterator.next(), "Вложения", result.getTotalInvestment());
        assertRowValues(rowIterator.next(), "Итоговый общий баланс", result.getFinalTotalBalance());
        assertRowValues(rowIterator.next(), "Итоговый валютный баланс", result.getFinalBalance());
        assertRowValues(rowIterator.next(), "Средневзвешенные вложения", result.getWeightedAverageInvestment());
        assertRowValues(rowIterator.next(), "Абсолютный доход", result.getAbsoluteProfit());
        assertRowValues(rowIterator.next(), "Относительный доход", result.getRelativeProfit());
        assertRowValues(rowIterator.next(), "Относительный годовой доход", result.getRelativeYearProfit());

        assertRowValues(rowIterator.next());
        assertRowValues(rowIterator.next(), "Позиции");
        assertRowValues(rowIterator.next(), "Цена", "Количество");
        for (SimulatedPosition position : result.getPositions()) {
            assertRowValues(rowIterator.next(), position.getPrice(), position.getQuantity());
        }

        assertRowValues(rowIterator.next());
        assertRowValues(rowIterator.next(), "Операции");
        assertRowValues(rowIterator.next(), "Дата и время", "Тип операции", "Цена", "Количество", "Комиссия");
        for (SimulatedOperation operation : result.getOperations()) {
            assertRowValues(rowIterator.next(),
                    operation.getDateTime(),
                    operation.getOperationType().name(),
                    operation.getPrice(),
                    operation.getQuantity(),
                    operation.getCommission());
        }

        List<CellRangeAddress> mergedRegions = sheet.getMergedRegions();
        assertEquals(3, mergedRegions.size());

        assertChartCreated(sheet);
    }

    @Test
    void saveCandles() {
        final String ticker = "ticker";

        final OffsetDateTime from = DateUtils.getDateTime(2021, 1, 1, 0, 0, 0);
        final OffsetDateTime to = DateUtils.getDateTime(2021, 2, 1, 0, 0, 0);
        final Interval interval = Interval.of(from, to);

        final List<Candle> candles = createCandles();

        excelService.saveCandles(ticker, interval, candles);

        String fileNamePrefix = "Candles for '" + ticker + "'";
        verify(excelFileService).saveToFile(workbookArgumentCaptor.capture(), Mockito.startsWith(fileNamePrefix));

        ExtendedWorkbook workbook = workbookArgumentCaptor.getValue();
        assertEquals(1, workbook.getNumberOfSheets());
        ExtendedSheet sheet = (ExtendedSheet) workbook.getSheet(ticker);

        assertEquals(10, sheet.getRowsCount());

        Iterator<Row> rowIterator = sheet.iterator();
        assertRowValues(rowIterator.next(), "Тикер", ticker);
        assertRowValues(rowIterator.next(), "Интервал", interval.toPrettyString());
        assertRowValues(rowIterator.next());
        assertRowValues(rowIterator.next(), "Свечи");
        assertRowValues(rowIterator.next(),
                "Дата-время", "Цена открытия", "Цена закрытия", "Набольшая цена", "Наименьшая цена");
        for (Candle candle : candles) {
            assertRowValues(rowIterator.next(),
                    candle.getTime(),
                    candle.getOpenPrice(),
                    candle.getClosePrice(),
                    candle.getHighestPrice(),
                    candle.getLowestPrice());
        }

        assertChartCreated(sheet);
    }

    private SimulationResult createSimulationResult() {
        String ticker = "ticker";

        return SimulationResult.builder()
                .botName("bot")
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
                .operationType(OperationType.Buy)
                .price(BigDecimal.valueOf(150))
                .quantity(1)
                .commission(BigDecimal.valueOf(0.45))
                .build();
        SimulatedOperation operation2 = SimulatedOperation.builder()
                .ticker(ticker)
                .dateTime(DateUtils.getDateTime(2020, 10, 5, 10, 11, 0))
                .operationType(OperationType.Sell)
                .price(BigDecimal.valueOf(180))
                .quantity(1)
                .commission(BigDecimal.valueOf(0.54))
                .build();
        SimulatedOperation operation3 = SimulatedOperation.builder()
                .ticker(ticker)
                .dateTime(DateUtils.getDateTime(2020, 10, 10, 10, 50, 0))
                .operationType(OperationType.Buy)
                .price(BigDecimal.valueOf(160))
                .quantity(3)
                .commission(BigDecimal.valueOf(0.48))
                .build();
        SimulatedOperation operation4 = SimulatedOperation.builder()
                .ticker(ticker)
                .dateTime(DateUtils.getDateTime(2020, 11, 1, 10, 0, 0))
                .operationType(OperationType.Buy)
                .price(BigDecimal.valueOf(120))
                .quantity(2)
                .commission(BigDecimal.valueOf(0.36))
                .build();

        return Arrays.asList(operation1, operation2, operation3, operation4);
    }

    private List<Candle> createCandles() {
        Candle candle1 = Candle.builder()
                .time(DateUtils.getDateTime(2020, 10, 1, 10, 0, 0))
                .openPrice(BigDecimal.valueOf(150))
                .build();
        Candle candle2 = Candle.builder()
                .time(DateUtils.getDateTime(2020, 10, 1, 11, 0, 0))
                .openPrice(BigDecimal.valueOf(160))
                .build();
        Candle candle3 = Candle.builder()
                .time(DateUtils.getDateTime(2020, 10, 5, 10, 11, 0))
                .openPrice(BigDecimal.valueOf(180))
                .build();
        Candle candle4 = Candle.builder()
                .time(DateUtils.getDateTime(2020, 10, 10, 10, 50, 0))
                .openPrice(BigDecimal.valueOf(160))
                .build();
        Candle candle5 = Candle.builder()
                .time(DateUtils.getDateTime(2020, 11, 1, 10, 0, 0))
                .openPrice(BigDecimal.valueOf(120))
                .build();

        return Arrays.asList(candle1, candle2, candle3, candle4, candle5);
    }

    private void assertChartCreated(ExtendedSheet sheet) {
        Iterator<?> iterator = sheet.getDrawingPatriarch().iterator();
        XSSFGraphicFrame frame = (XSSFGraphicFrame) iterator.next();
        assertFalse(iterator.hasNext());
        List<XSSFChart> charts = frame.getDrawing().getCharts();
        assertEquals(1, charts.size());
        XSSFChart chart = charts.get(0);
        List<? extends XDDFChartAxis> axes = chart.getAxes();
        assertEquals(2, axes.size());
        AssertUtils.assertEquals(120, axes.get(1).getMinimum());
        AssertUtils.assertEquals(180, axes.get(1).getMaximum());
    }

}