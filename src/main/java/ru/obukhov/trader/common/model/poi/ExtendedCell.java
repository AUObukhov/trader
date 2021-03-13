package ru.obukhov.trader.common.model.poi;

import lombok.Getter;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.Hyperlink;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.ss.util.CellRangeAddress;
import org.springframework.util.Assert;

import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;

/**
 * Proxy class, implementing {@link Cell} and having additional handy methods
 */
public class ExtendedCell implements Cell {

    private final ExtendedRow row;

    @Getter
    private final Cell delegate;

    public ExtendedCell(ExtendedRow row, Cell delegate) {
        Assert.isTrue(row != null, "row can't be null");
        Assert.isTrue(delegate != null, "delegate can't be null");
        Assert.isTrue(!(delegate instanceof ExtendedCell), "delegate can't be ExtendedCell");

        this.row = row;
        this.delegate = delegate;
    }

    // region additional methods

    public ExtendedWorkbook getWorkbook() {
        return row.getWorkbook();
    }

    // endregion

    // region Cell implementation

    @Override
    public int getColumnIndex() {
        return delegate.getColumnIndex();
    }

    @Override
    public int getRowIndex() {
        return delegate.getRowIndex();
    }

    @Override
    public Sheet getSheet() {
        return row.getSheet();
    }

    @Override
    public Row getRow() {
        return row;
    }

    @Override
    public void setBlank() {
        delegate.setBlank();
    }

    @Override
    public CellType getCellType() {
        return delegate.getCellType();
    }

    @Override
    @SuppressWarnings("deprecation")
    public void setCellType(CellType cellType) {
        delegate.setCellType(cellType);
    }

    @Override
    @SuppressWarnings("deprecation")
    public CellType getCellTypeEnum() {
        return delegate.getCellTypeEnum();
    }

    @Override
    public CellType getCachedFormulaResultType() {
        return delegate.getCachedFormulaResultType();
    }

    @Override
    @SuppressWarnings("deprecation")
    public CellType getCachedFormulaResultTypeEnum() {
        return delegate.getCachedFormulaResultTypeEnum();
    }

    @Override
    public void setCellValue(double value) {
        delegate.setCellValue(value);
    }

    @Override
    public void setCellValue(Date value) {
        delegate.setCellValue(value);
    }

    @Override
    public void setCellValue(LocalDateTime value) {
        delegate.setCellValue(value);
    }

    @Override
    public void setCellValue(Calendar value) {
        delegate.setCellValue(value);
    }

    @Override
    public void setCellValue(RichTextString value) {
        delegate.setCellValue(value);
    }

    @Override
    public void setCellValue(String value) {
        delegate.setCellValue(value);
    }

    @Override
    public void removeFormula() {
        delegate.removeFormula();
    }

    @Override
    public String getCellFormula() {
        return delegate.getCellFormula();
    }

    @Override
    public void setCellFormula(String formula) {
        delegate.setCellFormula(formula);
    }

    @Override
    public double getNumericCellValue() {
        return delegate.getNumericCellValue();
    }

    @Override
    public Date getDateCellValue() {
        return delegate.getDateCellValue();
    }

    @Override
    public LocalDateTime getLocalDateTimeCellValue() {
        return delegate.getLocalDateTimeCellValue();
    }

    @Override
    public RichTextString getRichStringCellValue() {
        return delegate.getRichStringCellValue();
    }

    @Override
    public String getStringCellValue() {
        return delegate.getStringCellValue();
    }

    @Override
    public void setCellValue(boolean value) {
        delegate.setCellValue(value);
    }

    @Override
    public void setCellErrorValue(byte value) {
        delegate.setCellErrorValue(value);
    }

    @Override
    public boolean getBooleanCellValue() {
        return delegate.getBooleanCellValue();
    }

    @Override
    public byte getErrorCellValue() {
        return delegate.getErrorCellValue();
    }

    @Override
    public CellStyle getCellStyle() {
        return delegate.getCellStyle();
    }

    @Override
    public void setCellStyle(CellStyle style) {
        delegate.setCellStyle(style);
    }

    @Override
    public void setAsActiveCell() {
        delegate.setAsActiveCell();
    }

    @Override
    public CellAddress getAddress() {
        return delegate.getAddress();
    }

    @Override
    public Comment getCellComment() {
        return delegate.getCellComment();
    }

    @Override
    public void setCellComment(Comment comment) {
        delegate.setCellComment(comment);
    }

    @Override
    public void removeCellComment() {
        delegate.removeCellComment();
    }

    @Override
    public Hyperlink getHyperlink() {
        return delegate.getHyperlink();
    }

    @Override
    public void setHyperlink(Hyperlink link) {
        delegate.setHyperlink(link);
    }

    @Override
    public void removeHyperlink() {
        delegate.removeHyperlink();
    }

    @Override
    public CellRangeAddress getArrayFormulaRange() {
        return delegate.getArrayFormulaRange();
    }

    @Override
    public boolean isPartOfArrayFormulaGroup() {
        return delegate.isPartOfArrayFormulaGroup();
    }

    // endregion

}