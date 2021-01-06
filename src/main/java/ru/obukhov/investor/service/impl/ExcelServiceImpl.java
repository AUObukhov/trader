package ru.obukhov.investor.service.impl;

import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import ru.obukhov.investor.service.interfaces.ExcelFileService;
import ru.obukhov.investor.service.interfaces.ExcelService;
import ru.obukhov.investor.util.poi.ExtendedRow;
import ru.obukhov.investor.util.poi.ExtendedSheet;
import ru.obukhov.investor.util.poi.ExtendedWorkbook;
import ru.obukhov.investor.web.model.SimulatedOperation;
import ru.obukhov.investor.web.model.SimulatedPosition;
import ru.obukhov.investor.web.model.SimulationResult;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExcelServiceImpl implements ExcelService {

    public static final String DATE_TIME_FORMAT = "d.m.yyyy h:mm:ss";

    private final ExcelFileService excelFileService;

    @Override
    public void saveSimulationResult(SimulationResult result) {
        ExtendedWorkbook workBook = createWorkBook();
        createSheet(workBook, result);
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
        ExtendedSheet sheet = (ExtendedSheet) workbook.createSheet("Simulation result");

        putTotalBalance(sheet, result.getTotalBalance());
        putCurrencyBalance(sheet, result.getCurrencyBalance());
        putPositions(sheet, result.getPositions());
        putOperations(sheet, result.getOperations());

        sheet.autoSizeColumns();
    }

    private void putTotalBalance(ExtendedSheet sheet, BigDecimal totalBalance) {
        ExtendedRow row = sheet.addRow();
        row.createCells("Общий баланс", totalBalance);
    }

    private void putCurrencyBalance(ExtendedSheet sheet, BigDecimal currencyBalance) {
        ExtendedRow row = sheet.addRow();
        row.createCells("Валютный баланс", currencyBalance);
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