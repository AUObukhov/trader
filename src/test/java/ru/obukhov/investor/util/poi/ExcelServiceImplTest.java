package ru.obukhov.investor.util.poi;

import org.apache.poi.ss.usermodel.Row;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import ru.obukhov.investor.BaseMockedTest;
import ru.obukhov.investor.service.impl.ExcelServiceImpl;
import ru.obukhov.investor.service.interfaces.ExcelFileService;
import ru.obukhov.investor.util.AssertUtils;
import ru.obukhov.investor.util.DateUtils;
import ru.obukhov.investor.web.model.SimulatedOperation;
import ru.obukhov.investor.web.model.SimulatedPosition;
import ru.obukhov.investor.web.model.SimulationResult;
import ru.tinkoff.invest.openapi.models.operations.OperationType;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;

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
        Assert.assertEquals(1, workbook.getNumberOfSheets());
        ExtendedSheet sheet = (ExtendedSheet) workbook.getSheet(result.getBotName());

        int expectedRowCount = 8 + result.getPositions().size() + result.getOperations().size();
        Assert.assertEquals(expectedRowCount, sheet.getRowsCount());

        Iterator<Row> rowIterator = sheet.iterator();
        AssertUtils.assertRowValues(rowIterator.next(), "Общий баланс", result.getTotalBalance());
        AssertUtils.assertRowValues(rowIterator.next(), "Валютный баланс", result.getCurrencyBalance());

        AssertUtils.assertRowValues(rowIterator.next());
        AssertUtils.assertRowValues(rowIterator.next(), "Позиции");
        AssertUtils.assertRowValues(rowIterator.next(), "Тикер", "Цена", "Количество");
        for (SimulatedPosition position : result.getPositions()) {
            AssertUtils.assertRowValues(rowIterator.next(),
                    position.getTicker(), position.getPrice(), position.getQuantity());
        }

        AssertUtils.assertRowValues(rowIterator.next());
        AssertUtils.assertRowValues(rowIterator.next(), "Операции");
        AssertUtils.assertRowValues(rowIterator.next(),
                "Тикер", "Дата и время", "Тип операции", "Цена", "Количество", "Комиссия");
        for (SimulatedOperation operation : result.getOperations()) {
            AssertUtils.assertRowValues(rowIterator.next(),
                    operation.getTicker(),
                    operation.getDateTime(),
                    operation.getOperationType().name(),
                    operation.getPrice(),
                    operation.getQuantity(),
                    operation.getCommission());
        }
    }

    private SimulationResult createSimulationResult() {
        String ticker1 = "ticker1";
        String ticker2 = "ticker2";

        BigDecimal totalBalance = BigDecimal.valueOf(1000);
        BigDecimal currencyBalance = BigDecimal.valueOf(200);

        List<SimulatedPosition> positions = Arrays.asList(
                new SimulatedPosition(ticker1, BigDecimal.valueOf(200), 3),
                new SimulatedPosition(ticker2, BigDecimal.valueOf(100), 2)
        );

        SimulatedOperation operation1 = SimulatedOperation.builder()
                .ticker(ticker1)
                .dateTime(DateUtils.getDateTime(2020, 10, 1, 10, 0, 0))
                .operationType(OperationType.Buy)
                .price(BigDecimal.valueOf(150))
                .quantity(1)
                .commission(BigDecimal.valueOf(0.45))
                .build();
        SimulatedOperation operation2 = SimulatedOperation.builder()
                .ticker(ticker1)
                .dateTime(DateUtils.getDateTime(2020, 10, 5, 10, 11, 0))
                .operationType(OperationType.Sell)
                .price(BigDecimal.valueOf(180))
                .quantity(1)
                .commission(BigDecimal.valueOf(0.54))
                .build();
        SimulatedOperation operation3 = SimulatedOperation.builder()
                .ticker(ticker1)
                .dateTime(DateUtils.getDateTime(2020, 10, 10, 10, 50, 0))
                .operationType(OperationType.Buy)
                .price(BigDecimal.valueOf(160))
                .quantity(3)
                .commission(BigDecimal.valueOf(0.48))
                .build();
        SimulatedOperation operation4 = SimulatedOperation.builder()
                .ticker(ticker2)
                .dateTime(DateUtils.getDateTime(2020, 11, 1, 10, 0, 0))
                .operationType(OperationType.Buy)
                .price(BigDecimal.valueOf(120))
                .quantity(2)
                .commission(BigDecimal.valueOf(0.36))
                .build();
        List<SimulatedOperation> operations = Arrays.asList(
                operation1, operation2, operation3, operation4
        );

        return SimulationResult.builder()
                .botName("bot")
                .totalBalance(totalBalance)
                .currencyBalance(currencyBalance)
                .positions(positions)
                .operations(operations)
                .build();
    }

}