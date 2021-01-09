package ru.obukhov.investor.service.impl;

import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import ru.obukhov.investor.model.Interval;
import ru.obukhov.investor.service.interfaces.ExcelFileService;
import ru.obukhov.investor.service.interfaces.ExcelService;
import ru.obukhov.investor.util.poi.ExtendedRow;
import ru.obukhov.investor.util.poi.ExtendedSheet;
import ru.obukhov.investor.util.poi.ExtendedWorkbook;
import ru.obukhov.investor.web.model.SimulatedOperation;
import ru.obukhov.investor.web.model.SimulatedPosition;
import ru.obukhov.investor.web.model.SimulationResult;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExcelServiceImpl implements ExcelService {

    public static final String DATE_TIME_FORMAT = "d.m.yyyy h:mm:ss";

    private final ExcelFileService excelFileService;

    @Override
    public void saveSimulationResults(Collection<SimulationResult> results) {
        ExtendedWorkbook workBook = createWorkBook();
        for (SimulationResult result : results) {
            createSheet(workBook, result);
        }
        excelFileService.saveToFile(workBook, "SimulationResult");
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
    }

    private void createSheet(ExtendedWorkbook workbook, SimulationResult result) {
        ExtendedSheet sheet = (ExtendedSheet) workbook.createSheet(result.getBotName());

        putCommonStatistics(result, sheet);
        putPositions(sheet, result.getPositions());
        putOperations(sheet, result.getOperations());

        sheet.autoSizeColumns();
    }

    private void putCommonStatistics(SimulationResult result, ExtendedSheet sheet) {
        ExtendedRow labelRow = sheet.addRow();
        labelRow.createCells("Общая статистика");

        putInterval(sheet, result.getInterval());
        putInitialBalance(sheet, result.getInitialBalance());
        putTotalBalance(sheet, result.getTotalBalance());
        putCurrencyBalance(sheet, result.getCurrencyBalance());
        putAbsoluteProfit(sheet, result.getAbsoluteProfit());
        putRelativeProfit(sheet, result.getRelativeProfit());
        putRelativeYearProfit(sheet, result.getRelativeYearProfit());
    }

    private void putInterval(ExtendedSheet sheet, Interval interval) {
        ExtendedRow row = sheet.addRow();
        row.createCells("Интервал", interval.toPrettyString());
    }

    private void putInitialBalance(ExtendedSheet sheet, BigDecimal initialBalance) {
        ExtendedRow row = sheet.addRow();
        row.createCells("Начальный баланс", initialBalance);
    }

    private void putTotalBalance(ExtendedSheet sheet, BigDecimal totalBalance) {
        ExtendedRow row = sheet.addRow();
        row.createCells("Общий баланс", totalBalance);
    }

    private void putCurrencyBalance(ExtendedSheet sheet, BigDecimal currencyBalance) {
        ExtendedRow row = sheet.addRow();
        row.createCells("Валютный баланс", currencyBalance);
    }

    private void putAbsoluteProfit(ExtendedSheet sheet, BigDecimal absoluteProfit) {
        ExtendedRow row = sheet.addRow();
        row.createCells("Абсолютный доход", absoluteProfit);
    }

    private void putRelativeProfit(ExtendedSheet sheet, double relativeProfit) {
        ExtendedRow row = sheet.addRow();
        row.createCells("Относительный доход", relativeProfit);
    }

    private void putRelativeYearProfit(ExtendedSheet sheet, double relativeYearProfit) {
        ExtendedRow row = sheet.addRow();
        row.createCells("Относительный годовой доход", relativeYearProfit);
    }

    private void putPositions(ExtendedSheet sheet, List<SimulatedPosition> positions) {
        sheet.addRow();
        ExtendedRow labelRow = sheet.addRow();

        if (CollectionUtils.isEmpty(positions)) {
            labelRow.createCells("Позиции отсутствуют");
        } else {
            labelRow.createCell(0, "Позиции");
            ExtendedRow headersRow = sheet.addRow();
            headersRow.createCells("Тикер", "Цена", "Количество");
            for (SimulatedPosition position : positions) {
                ExtendedRow row = sheet.addRow();
                row.createCells(position.getTicker(), position.getPrice(), position.getQuantity());
            }
        }
    }

    private void putOperations(ExtendedSheet sheet, List<SimulatedOperation> operations) {
        sheet.addRow();
        ExtendedRow labelRow = sheet.addRow();

        if (CollectionUtils.isEmpty(operations)) {
            labelRow.createCells("Операции отсутствуют");
        } else {
            labelRow.createCell(0, "Операции");
            ExtendedRow headersRow = sheet.addRow();
            headersRow.createCells("Тикер", "Дата и время", "Тип операции", "Цена", "Количество", "Комиссия");
            for (SimulatedOperation operation : operations) {
                ExtendedRow row = sheet.addRow();
                row.createCells(operation.getTicker(),
                        operation.getDateTime(),
                        operation.getOperationType().name(),
                        operation.getPrice(),
                        operation.getQuantity(),
                        operation.getCommission());
            }
        }
    }

}