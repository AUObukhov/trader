package ru.obukhov.trader.test.utils.model;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xddf.usermodel.chart.AxisPosition;
import org.apache.poi.xddf.usermodel.chart.ChartTypes;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import ru.obukhov.trader.common.model.poi.ExtendedChart;
import ru.obukhov.trader.common.model.poi.ExtendedChartData;
import ru.obukhov.trader.common.model.poi.ExtendedRow;
import ru.obukhov.trader.common.model.poi.ExtendedSheet;
import ru.obukhov.trader.common.model.poi.ExtendedWorkbook;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class PoiTestData {

    public static ExtendedWorkbook createExtendedWorkbook() {
        final Workbook workbook = new XSSFWorkbook();
        return new ExtendedWorkbook(workbook);
    }

    public static ExtendedSheet createExtendedSheet() throws IOException {
        try (final ExtendedWorkbook workbook = createExtendedWorkbook()) {
            return (ExtendedSheet) workbook.createSheet();
        }
    }

    public static XSSFSheet createXSSFSheet() throws IOException {
        try (final Workbook workbook = new XSSFWorkbook()) {
            return (XSSFSheet) workbook.createSheet();
        }
    }

    public static ExtendedChart createExtendedChart() throws IOException {
        final ExtendedSheet extendedSheet = createExtendedSheet();
        return extendedSheet.createChart(0, 0, 1, 1);
    }

    public static ExtendedChartData createExtendedChartData() throws IOException {
        final ExtendedChart extendedChart = createExtendedChart();
        return extendedChart.createChartData(AxisPosition.BOTTOM, AxisPosition.LEFT, ChartTypes.LINE);
    }

    public static ExtendedRow createExtendedRow() throws IOException {
        return (ExtendedRow) createExtendedSheet().createRow(0);
    }

    public static void addRow(Sheet sheet, int columnCount) {
        final Row row = sheet.createRow(sheet.getLastRowNum() + 1);
        for (int column = 0; column < columnCount; column++) {
            row.createCell(column);
        }
    }

    public static List<Row> addRows(ExtendedSheet extendedSheet, int... rownums) {
        return Arrays.stream(rownums)
                .mapToObj(extendedSheet::createRow)
                .toList();
    }

    public static List<Sheet> createSheets(Workbook workbook, String... sheetNames) {
        return Arrays.stream(sheetNames)
                .map(workbook::createSheet)
                .toList();
    }

    public static Cell createCell(Row row, int column, String value) {
        final Cell cell = row.createCell(column);
        cell.setCellValue(value);
        return cell;
    }

}