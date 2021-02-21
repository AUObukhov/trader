package ru.obukhov.investor.util.poi;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xddf.usermodel.chart.XDDFChartAxis;
import org.apache.poi.xssf.usermodel.XSSFChart;
import org.apache.poi.xssf.usermodel.XSSFGraphicFrame;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import ru.obukhov.investor.BaseMockedTest;
import ru.obukhov.investor.model.Candle;
import ru.obukhov.investor.model.Interval;
import ru.obukhov.investor.service.impl.ExcelServiceImpl;
import ru.obukhov.investor.service.interfaces.ExcelFileService;
import ru.obukhov.investor.test.utils.AssertUtils;
import ru.obukhov.investor.util.DateUtils;
import ru.obukhov.investor.web.model.SimulatedOperation;
import ru.obukhov.investor.web.model.SimulatedPosition;
import ru.obukhov.investor.web.model.SimulationResult;
import ru.tinkoff.invest.openapi.models.operations.OperationType;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static ru.obukhov.investor.test.utils.AssertUtils.assertRowValues;

public class ExcelServiceImplTest extends BaseMockedTest {

    @Captor
    private ArgumentCaptor<ExtendedWorkbook> workbookArgumentCaptor;
    @Mock
    private ExcelFileService excelFileService;
    private ExcelServiceImpl excelService;

    @Before
    public void setUp() {
        this.excelService = new ExcelServiceImpl(excelFileService);
    }

    @Test
    public void saveSimulationResult() {

        SimulationResult result = createSimulationResult();

        excelService.saveSimulationResults(Collections.singletonList(result));

        verify(excelFileService).saveToFile(workbookArgumentCaptor.capture(), anyString());

        ExtendedWorkbook workbook = workbookArgumentCaptor.getValue();
        assertEquals(1, workbook.getNumberOfSheets());
        ExtendedSheet sheet = (ExtendedSheet) workbook.getSheet(result.getBotName());

        int expectedRowCount = 14 + result.getPositions().size() + result.getOperations().size();
        assertEquals(expectedRowCount, sheet.getRowsCount());

        Iterator<Row> rowIterator = sheet.iterator();
        assertRowValues(rowIterator.next(), "Общая статистика");
        assertRowValues(rowIterator.next(), "Интервал", result.getInterval().toPrettyString());
        assertRowValues(rowIterator.next(), "Начальный баланс", result.getInitialBalance());
        assertRowValues(rowIterator.next(), "Общий баланс", result.getTotalBalance());
        assertRowValues(rowIterator.next(), "Валютный баланс", result.getCurrencyBalance());
        assertRowValues(rowIterator.next(), "Абсолютный доход", result.getAbsoluteProfit());
        assertRowValues(rowIterator.next(), "Относительный доход", result.getRelativeProfit());
        assertRowValues(rowIterator.next(), "Относительный годовой доход",
                result.getRelativeYearProfit());

        assertRowValues(rowIterator.next());
        assertRowValues(rowIterator.next(), "Позиции");
        assertRowValues(rowIterator.next(), "Тикер", "Цена", "Количество");
        for (SimulatedPosition position : result.getPositions()) {
            assertRowValues(rowIterator.next(),
                    position.getTicker(), position.getPrice(), position.getQuantity());
        }

        assertRowValues(rowIterator.next());
        assertRowValues(rowIterator.next(), "Операции");
        assertRowValues(rowIterator.next(),
                "Тикер", "Дата и время", "Тип операции", "Цена", "Количество", "Комиссия");
        for (SimulatedOperation operation : result.getOperations()) {
            assertRowValues(rowIterator.next(),
                    operation.getTicker(),
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

    private SimulationResult createSimulationResult() {
        SimulationResult.SimulationResultBuilder builder = SimulationResult.builder()
                .botName("bot");

        String ticker = "ticker";

        builder.interval(createInterval());
        builder.initialBalance(BigDecimal.valueOf(700));
        builder.totalBalance(BigDecimal.valueOf(1000));
        builder.currencyBalance(BigDecimal.valueOf(200));
        builder.absoluteProfit(BigDecimal.valueOf(300));
        builder.relativeProfit(0.25);
        builder.relativeYearProfit(6d);
        builder.positions(createPositions(ticker));
        builder.operations(createSimulatedOperations(ticker));
        builder.candles(createCandles());

        return builder.build();
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
                .closePrice(BigDecimal.valueOf(150))
                .build();
        Candle candle2 = Candle.builder()
                .time(DateUtils.getDateTime(2020, 10, 1, 11, 0, 0))
                .closePrice(BigDecimal.valueOf(160))
                .build();
        Candle candle3 = Candle.builder()
                .time(DateUtils.getDateTime(2020, 10, 5, 10, 11, 0))
                .closePrice(BigDecimal.valueOf(180))
                .build();
        Candle candle4 = Candle.builder()
                .time(DateUtils.getDateTime(2020, 10, 10, 10, 50, 0))
                .closePrice(BigDecimal.valueOf(160))
                .build();
        Candle candle5 = Candle.builder()
                .time(DateUtils.getDateTime(2020, 11, 1, 10, 0, 0))
                .closePrice(BigDecimal.valueOf(120))
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