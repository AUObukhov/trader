package ru.obukhov.trader.common.model.poi;

import com.google.common.collect.Streams;
import lombok.Getter;
import org.apache.poi.ss.usermodel.AutoFilter;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellRange;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.usermodel.DataValidationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Footer;
import org.apache.poi.ss.usermodel.Header;
import org.apache.poi.ss.usermodel.Hyperlink;
import org.apache.poi.ss.usermodel.PrintSetup;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.SheetConditionalFormatting;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.PaneInformation;
import org.apache.poi.xssf.usermodel.XSSFChart;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.jetbrains.annotations.NotNull;
import org.springframework.util.Assert;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Proxy class, implementing {@link Sheet} and having additional handy methods
 */
public class ExtendedSheet implements Sheet {

    private final ExtendedWorkbook workbook;

    @Getter
    private final Sheet delegate;

    private final TreeMap<Integer, ExtendedRow> rows;

    // region constructor

    public ExtendedSheet(ExtendedWorkbook workbook, Sheet delegate) {
        Assert.isTrue(workbook != null, "workbook can't be null");
        Assert.isTrue(delegate != null, "delegate can't be null");
        Assert.isTrue(!(delegate instanceof ExtendedSheet), "delegate can't be ExtendedSheet");

        this.workbook = workbook;
        this.delegate = delegate;
        this.rows = initRows(delegate);
    }

    @SuppressWarnings("UnstableApiUsage")
    private TreeMap<Integer, ExtendedRow> initRows(Sheet delegate) {
        Map<Integer, ExtendedRow> map = Streams.stream(delegate.rowIterator())
                .collect(Collectors.toMap(Row::getRowNum, this::createRow));
        return new TreeMap<>(map);
    }

    private ExtendedRow createRow(Row row) {
        return new ExtendedRow(this, row);
    }

    // endregion

    // region additional methods

    /**
     * count of rows of the sheet
     */
    public int getRowsCount() {
        return rows.size();
    }

    /**
     * Adjusts all columns width to fit the contents
     */
    public void autoSizeColumns() {
        int columnsCount = getColumnsCount();
        for (int column = 0; column < columnsCount; column++) {
            autoSizeColumn(column);
        }
    }

    /**
     * @return max of Row#getLastCellNum of rows of the sheet
     */
    public int getColumnsCount() {
        return rows.values().stream()
                .map(Row::getLastCellNum)
                .max(Comparator.naturalOrder())
                .orElse((short) 0);
    }

    /**
     * Adds new row in the end of the sheet
     *
     * @return created row
     */
    public ExtendedRow addRow() {
        return (ExtendedRow) createRow(getLastRowNum() + 1);
    }

    public ExtendedChart createChart(int column1, int row1, int column2, int row2) {
        XSSFDrawing drawing = (XSSFDrawing) createDrawingPatriarch();
        ClientAnchor anchor = drawing.createAnchor(0, 0, 0, 0, column1, row1, column2, row2);
        XSSFChart chart = drawing.createChart(anchor);
        return new ExtendedChart(chart);
    }

    /**
     * Same as {@link Sheet#addMergedRegion(CellRangeAddress)}, but with exact values of cell range address fields
     */
    public int addMergedRegion(int firstRow, int lastRow, int firstCol, int lastCol) {
        CellRangeAddress cellRangeAddress = new CellRangeAddress(firstRow, lastRow, firstCol, lastCol);
        return addMergedRegion(cellRangeAddress);
    }

    // endregion

    // region Sheet implementation

    @Override
    public Row createRow(int rownum) {
        ExtendedRow row = new ExtendedRow(this, delegate.createRow(rownum));
        rows.put(rownum, row);
        return row;
    }

    @Override
    public void removeRow(Row row) {
        delegate.removeRow(row);
        rows.remove(row.getRowNum());
    }

    @Override
    public Row getRow(int rownum) {
        return rows.get(rownum);
    }

    @Override
    public int getPhysicalNumberOfRows() {
        return delegate.getPhysicalNumberOfRows();
    }

    @Override
    public int getFirstRowNum() {
        return delegate.getFirstRowNum();
    }

    @Override
    public int getLastRowNum() {
        return delegate.getLastRowNum();
    }

    @Override
    public void setColumnHidden(int columnIndex, boolean hidden) {
        delegate.setColumnHidden(columnIndex, hidden);
    }

    @Override
    public boolean isColumnHidden(int columnIndex) {
        return delegate.isColumnHidden(columnIndex);
    }

    @Override
    public boolean isRightToLeft() {
        return delegate.isRightToLeft();
    }

    @Override
    public void setRightToLeft(boolean value) {
        delegate.setRightToLeft(value);
    }

    @Override
    public void setColumnWidth(int columnIndex, int width) {
        delegate.setColumnWidth(columnIndex, width);
    }

    @Override
    public int getColumnWidth(int columnIndex) {
        return delegate.getColumnWidth(columnIndex);
    }

    @Override
    public float getColumnWidthInPixels(int columnIndex) {
        return delegate.getColumnWidthInPixels(columnIndex);
    }

    @Override
    public int getDefaultColumnWidth() {
        return delegate.getDefaultColumnWidth();
    }

    @Override
    public void setDefaultColumnWidth(int width) {
        delegate.setDefaultColumnWidth(width);
    }

    @Override
    public short getDefaultRowHeight() {
        return delegate.getDefaultRowHeight();
    }

    @Override
    public void setDefaultRowHeight(short height) {
        delegate.setDefaultRowHeight(height);
    }

    @Override
    public float getDefaultRowHeightInPoints() {
        return delegate.getDefaultRowHeightInPoints();
    }

    @Override
    public void setDefaultRowHeightInPoints(float height) {
        delegate.setDefaultRowHeightInPoints(height);
    }

    @Override
    public CellStyle getColumnStyle(int column) {
        return delegate.getColumnStyle(column);
    }

    @Override
    public int addMergedRegion(CellRangeAddress region) {
        return delegate.addMergedRegion(region);
    }

    @Override
    public int addMergedRegionUnsafe(CellRangeAddress region) {
        return delegate.addMergedRegionUnsafe(region);
    }

    @Override
    public void validateMergedRegions() {
        delegate.validateMergedRegions();
    }

    @Override
    public boolean getHorizontallyCenter() {
        return delegate.getHorizontallyCenter();
    }

    @Override
    public void setHorizontallyCenter(boolean value) {
        delegate.setHorizontallyCenter(value);
    }

    @Override
    public boolean getVerticallyCenter() {
        return delegate.getVerticallyCenter();
    }

    @Override
    public void setVerticallyCenter(boolean value) {
        delegate.setVerticallyCenter(value);
    }

    @Override
    public void removeMergedRegion(int index) {
        delegate.removeMergedRegion(index);
    }

    @Override
    public void removeMergedRegions(Collection<Integer> indices) {
        delegate.removeMergedRegions(indices);
    }

    @Override
    public int getNumMergedRegions() {
        return delegate.getNumMergedRegions();
    }

    @Override
    public CellRangeAddress getMergedRegion(int index) {
        return delegate.getMergedRegion(index);
    }

    @Override
    public List<CellRangeAddress> getMergedRegions() {
        return delegate.getMergedRegions();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Iterator<Row> rowIterator() {
        return (Iterator<Row>) (Iterator<? extends Row>) rows.values().iterator();
    }

    @Override
    public boolean getForceFormulaRecalculation() {
        return delegate.getForceFormulaRecalculation();
    }

    @Override
    public void setForceFormulaRecalculation(boolean value) {
        delegate.setForceFormulaRecalculation(value);
    }

    @Override
    public boolean isDisplayZeros() {
        return delegate.isDisplayZeros();
    }

    @Override
    public void setDisplayZeros(boolean value) {
        delegate.setDisplayZeros(value);
    }

    @Override
    public boolean getAutobreaks() {
        return delegate.getAutobreaks();
    }

    @Override
    public void setAutobreaks(boolean value) {
        delegate.setAutobreaks(value);
    }

    @Override
    public boolean getDisplayGuts() {
        return delegate.getDisplayGuts();
    }

    @Override
    public void setDisplayGuts(boolean value) {
        delegate.setDisplayGuts(value);
    }

    @Override
    public boolean getFitToPage() {
        return delegate.getFitToPage();
    }

    @Override
    public void setFitToPage(boolean value) {
        delegate.setFitToPage(value);
    }

    @Override
    public boolean getRowSumsBelow() {
        return delegate.getRowSumsBelow();
    }

    @Override
    public void setRowSumsBelow(boolean value) {
        delegate.setRowSumsBelow(value);
    }

    @Override
    public boolean getRowSumsRight() {
        return delegate.getRowSumsRight();
    }

    @Override
    public void setRowSumsRight(boolean value) {
        delegate.setRowSumsRight(value);
    }

    @Override
    public boolean isPrintGridlines() {
        return delegate.isPrintGridlines();
    }

    @Override
    public void setPrintGridlines(boolean show) {
        delegate.setPrintGridlines(show);
    }

    @Override
    public boolean isPrintRowAndColumnHeadings() {
        return delegate.isPrintRowAndColumnHeadings();
    }

    @Override
    public void setPrintRowAndColumnHeadings(boolean show) {
        delegate.setPrintRowAndColumnHeadings(show);
    }

    @Override
    public PrintSetup getPrintSetup() {
        return delegate.getPrintSetup();
    }

    @Override
    public Header getHeader() {
        return delegate.getHeader();
    }

    @Override
    public Footer getFooter() {
        return delegate.getFooter();
    }

    @Override
    public double getMargin(short margin) {
        return delegate.getMargin(margin);
    }

    @Override
    public void setMargin(short margin, double size) {
        delegate.setMargin(margin, size);
    }

    @Override
    public boolean getProtect() {
        return delegate.getProtect();
    }

    @Override
    public void protectSheet(String password) {
        delegate.protectSheet(password);
    }

    @Override
    public boolean getScenarioProtect() {
        return delegate.getScenarioProtect();
    }

    @Override
    public void setZoom(int scale) {
        delegate.setZoom(scale);
    }

    @Override
    public short getTopRow() {
        return delegate.getTopRow();
    }

    @Override
    public short getLeftCol() {
        return delegate.getLeftCol();
    }

    @Override
    public void showInPane(int topRow, int leftCol) {
        delegate.showInPane(topRow, leftCol);
    }

    @Override
    public void shiftRows(int startRow, int endRow, int n) {
        delegate.shiftRows(startRow, endRow, n);
    }

    @Override
    public void shiftRows(int startRow, int endRow, int n, boolean copyRowHeight, boolean resetOriginalRowHeight) {
        delegate.shiftRows(startRow, endRow, n, copyRowHeight, resetOriginalRowHeight);
    }

    @Override
    public void shiftColumns(int startColumn, int endColumn, int n) {
        delegate.shiftColumns(startColumn, endColumn, n);
    }

    @Override
    public void createFreezePane(int colSplit, int rowSplit, int leftmostColumn, int topRow) {
        delegate.createFreezePane(colSplit, rowSplit, leftmostColumn, topRow);
    }

    @Override
    public void createFreezePane(int colSplit, int rowSplit) {
        delegate.createFreezePane(colSplit, rowSplit);
    }

    @Override
    public void createSplitPane(int xSplitPos, int ySplitPos, int leftmostColumn, int topRow, int activePane) {
        delegate.createSplitPane(xSplitPos, ySplitPos, leftmostColumn, topRow, activePane);
    }

    @Override
    public PaneInformation getPaneInformation() {
        return delegate.getPaneInformation();
    }

    @Override
    public boolean isDisplayGridlines() {
        return delegate.isDisplayGridlines();
    }

    @Override
    public void setDisplayGridlines(boolean show) {
        delegate.setDisplayGridlines(show);
    }

    @Override
    public boolean isDisplayFormulas() {
        return delegate.isDisplayFormulas();
    }

    @Override
    public void setDisplayFormulas(boolean show) {
        delegate.setDisplayFormulas(show);
    }

    @Override
    public boolean isDisplayRowColHeadings() {
        return delegate.isDisplayRowColHeadings();
    }

    @Override
    public void setDisplayRowColHeadings(boolean show) {
        delegate.setDisplayRowColHeadings(show);
    }

    @Override
    public void setRowBreak(int row) {
        delegate.setRowBreak(row);
    }

    @Override
    public boolean isRowBroken(int row) {
        return delegate.isRowBroken(row);
    }

    @Override
    public void removeRowBreak(int row) {
        delegate.removeRowBreak(row);
    }

    @Override
    public int[] getRowBreaks() {
        return delegate.getRowBreaks();
    }

    @Override
    public int[] getColumnBreaks() {
        return delegate.getColumnBreaks();
    }

    @Override
    public void setColumnBreak(int column) {
        delegate.setColumnBreak(column);
    }

    @Override
    public boolean isColumnBroken(int column) {
        return delegate.isColumnBroken(column);
    }

    @Override
    public void removeColumnBreak(int column) {
        delegate.removeColumnBreak(column);
    }

    @Override
    public void setColumnGroupCollapsed(int columnNumber, boolean collapsed) {
        delegate.setColumnGroupCollapsed(columnNumber, collapsed);
    }

    @Override
    public void groupColumn(int fromColumn, int toColumn) {
        delegate.groupColumn(fromColumn, toColumn);
    }

    @Override
    public void ungroupColumn(int fromColumn, int toColumn) {
        delegate.ungroupColumn(fromColumn, toColumn);
    }

    @Override
    public void groupRow(int fromRow, int toRow) {
        delegate.groupRow(fromRow, toRow);
    }

    @Override
    public void ungroupRow(int fromRow, int toRow) {
        delegate.ungroupRow(fromRow, toRow);
    }

    @Override
    public void setRowGroupCollapsed(int row, boolean collapse) {
        delegate.setRowGroupCollapsed(row, collapse);
    }

    @Override
    public void setDefaultColumnStyle(int column, CellStyle style) {
        delegate.setDefaultColumnStyle(column, style);
    }

    @Override
    public void autoSizeColumn(int column) {
        delegate.autoSizeColumn(column);
    }

    @Override
    public void autoSizeColumn(int column, boolean useMergedCells) {
        delegate.autoSizeColumn(column, useMergedCells);
    }

    @Override
    public Comment getCellComment(CellAddress ref) {
        return delegate.getCellComment(ref);
    }

    @Override
    public Map<CellAddress, ? extends Comment> getCellComments() {
        return delegate.getCellComments();
    }

    @Override
    public Drawing<?> getDrawingPatriarch() {
        return delegate.getDrawingPatriarch();
    }

    @Override
    public Drawing<?> createDrawingPatriarch() {
        return delegate.createDrawingPatriarch();
    }

    @Override
    public Workbook getWorkbook() {
        return workbook;
    }

    @Override
    public String getSheetName() {
        return delegate.getSheetName();
    }

    @Override
    public boolean isSelected() {
        return delegate.isSelected();
    }

    @Override
    public void setSelected(boolean value) {
        delegate.setSelected(value);
    }

    @Override
    public CellRange<? extends Cell> setArrayFormula(String formula, CellRangeAddress range) {
        return delegate.setArrayFormula(formula, range);
    }

    @Override
    public CellRange<? extends Cell> removeArrayFormula(Cell cell) {
        return delegate.removeArrayFormula(cell);
    }

    @Override
    public DataValidationHelper getDataValidationHelper() {
        return delegate.getDataValidationHelper();
    }

    @Override
    public List<? extends DataValidation> getDataValidations() {
        return delegate.getDataValidations();
    }

    @Override
    public void addValidationData(DataValidation dataValidation) {
        delegate.addValidationData(dataValidation);
    }

    @Override
    public AutoFilter setAutoFilter(CellRangeAddress range) {
        return delegate.setAutoFilter(range);
    }

    @Override
    public SheetConditionalFormatting getSheetConditionalFormatting() {
        return delegate.getSheetConditionalFormatting();
    }

    @Override
    public CellRangeAddress getRepeatingRows() {
        return delegate.getRepeatingRows();
    }

    @Override
    public void setRepeatingRows(CellRangeAddress rowRangeRef) {
        delegate.setRepeatingRows(rowRangeRef);
    }

    @Override
    public CellRangeAddress getRepeatingColumns() {
        return delegate.getRepeatingColumns();
    }

    @Override
    public void setRepeatingColumns(CellRangeAddress columnRangeRef) {
        delegate.setRepeatingColumns(columnRangeRef);
    }

    @Override
    public int getColumnOutlineLevel(int columnIndex) {
        return delegate.getColumnOutlineLevel(columnIndex);
    }

    @Override
    public Hyperlink getHyperlink(int row, int column) {
        return delegate.getHyperlink(row, column);
    }

    @Override
    public Hyperlink getHyperlink(CellAddress addr) {
        return delegate.getHyperlink(addr);
    }

    @Override
    public List<? extends Hyperlink> getHyperlinkList() {
        return delegate.getHyperlinkList();
    }

    @Override
    public CellAddress getActiveCell() {
        return delegate.getActiveCell();
    }

    @Override
    public void setActiveCell(CellAddress address) {
        delegate.setActiveCell(address);
    }

    @NotNull
    @Override
    public Iterator<Row> iterator() {
        return rowIterator();
    }

    // endregion

}
