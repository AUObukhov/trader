package ru.obukhov.investor.common.model.poi;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xddf.usermodel.chart.AxisPosition;
import org.apache.poi.xddf.usermodel.chart.ChartTypes;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ExcelTestDataHelper {

    public static ExtendedWorkbook createExtendedWorkbook() {
        Workbook workbook = new XSSFWorkbook();
        return new ExtendedWorkbook(workbook);
    }

    public static ExtendedSheet createExtendedSheet() {
        return (ExtendedSheet) createExtendedWorkbook().createSheet();
    }

    public static XSSFSheet createXSSFSheet() {
        Workbook workbook = new XSSFWorkbook();
        return (XSSFSheet) workbook.createSheet();
    }

    public static ExtendedChart createExtendedChart() {
        ExtendedSheet extendedSheet = createExtendedSheet();
        return extendedSheet.createChart(0, 0, 1, 1);
    }

    public static ExtendedChartData createExtendedChartData() {
        ExtendedChart extendedChart = createExtendedChart();
        return extendedChart.createChartData(AxisPosition.BOTTOM, AxisPosition.LEFT, ChartTypes.LINE);
    }

    public static ExtendedRow createExtendedRow() {
        return (ExtendedRow) createExtendedSheet().createRow(0);
    }

    public static void addRow(Sheet sheet, int columnCount) {
        Row row = sheet.createRow(sheet.getLastRowNum() + 1);
        for (int column = 0; column < columnCount; column++) {
            row.createCell(column);
        }
    }

    public static List<Row> addRows(ExtendedSheet extendedSheet, int... rownums) {
        return Arrays.stream(rownums)
                .mapToObj(extendedSheet::createRow)
                .collect(Collectors.toList());
    }

    public static List<Sheet> createSheets(Workbook workbook, String... sheetNames) {
        return Arrays.stream(sheetNames)
                .map(workbook::createSheet)
                .collect(Collectors.toList());
    }

    public static Cell createCell(Row row, int column, String value) {
        Cell cell = row.createCell(column);
        cell.setCellValue(value);
        return cell;
    }

}