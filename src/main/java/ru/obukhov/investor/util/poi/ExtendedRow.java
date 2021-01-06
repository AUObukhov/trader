package ru.obukhov.investor.util.poi;

import com.google.common.collect.Streams;
import lombok.Getter;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.util.Assert;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Proxy class, implementing {@link Row} and having additional handy methods
 */
public class ExtendedRow implements Row {

    private final ExtendedSheet sheet;

    @Getter
    private final Row delegate;

    private final TreeMap<Integer, ExtendedCell> cells;

    // region constructor

    public ExtendedRow(ExtendedSheet sheet, Row delegate) {
        Assert.isTrue(sheet != null, "sheet can't be null");
        Assert.isTrue(delegate != null, "delegate can't be null");
        Assert.isTrue(!(delegate instanceof ExtendedRow), "delegate can't be ExtendedRow");

        this.sheet = sheet;
        this.delegate = delegate;
        this.cells = initCells(delegate);
    }

    @SuppressWarnings("UnstableApiUsage")
    private TreeMap<Integer, ExtendedCell> initCells(Row delegate) {
        Map<Integer, ExtendedCell> map = Streams.stream(delegate.cellIterator())
                .collect(Collectors.toMap(Cell::getColumnIndex, this::initCell));
        return new TreeMap<>(map);
    }

    private ExtendedCell initCell(Cell cell) {
        return new ExtendedCell(this, cell);
    }

    // endregion

    // region additional methods

    /**
     * Creates cells with given {@code values} beginning from 0
     *
     * @return list of created cells
     */
    public List<ExtendedCell> createCells(Object... values) {
        return createCells(0, values);
    }

    /**
     * Creates cells with given {@code values} beginning from given {@code column}
     *
     * @return list of created cells
     */
    public List<ExtendedCell> createCells(int column, Object... values) {
        List<ExtendedCell> extendedCells = new ArrayList<>(values.length);
        int currentColumn = column;
        for (Object value : values) {
            extendedCells.add(createCell(currentColumn, value));
            currentColumn++;
        }
        return extendedCells;
    }

    /**
     * Creates cell with given {@code value} in given {@code column}.<br/>
     * <p>
     * Differences by type of {@code value}:<br/>
     * <p>
     * {@link String}: Created cell gets {@link CellType#STRING} type and
     * style from workbook named {@value ExtendedWorkbook.CellStylesNames#STRING}.
     * If such a style does not exist yet, then it is pre-created.<br/>
     * <p>
     * {@link BigDecimal}: Created cell gets {@link CellType#NUMERIC} type and
     * style from workbook named {@value ExtendedWorkbook.CellStylesNames#NUMERIC}.
     * * If such a style does not exist yet, then it is pre-created.<br/>
     * {@link Integer}: Created cell gets {@link CellType#NUMERIC} type and no style.<br/>
     * <p>
     * {@link OffsetDateTime}: Created cell gets {@link CellType#STRING} type and
     * style from workbook named {@value ExtendedWorkbook.CellStylesNames#DATE_TIME}.
     * * If such a style does not exist yet, then it is pre-created.<br/>
     * <p>
     * If {@code value} is null than created cell gets {@link CellType#BLANK} type and no style.
     *
     * @return created cell
     * @throws IllegalArgumentException {@code value} have invalid type
     */
    public ExtendedCell createCell(int column, @Nullable Object value) {
        if (value == null) {
            return (ExtendedCell) createCell(column);
        } else if (value instanceof String) {
            return createCell(column, (String) value);
        } else if (value instanceof BigDecimal) {
            return createCell(column, (BigDecimal) value);
        } else if (value instanceof Integer) {
            return createCell(column, (Integer) value);
        } else if (value instanceof LocalDateTime) {
            return createCell(column, (LocalDateTime) value);
        } else if (value instanceof OffsetDateTime) {
            return createCell(column, (OffsetDateTime) value);
        } else {
            throw new IllegalArgumentException("Unexpected type of value: " + value.getClass());
        }
    }

    /**
     * Create string cell with given {@code value} in given {@code column}.<br/>
     * Created cell gets cellStyle named {@value ExtendedWorkbook.CellStylesNames#STRING} from workbook.
     * If such a style does not exist yet, then it is pre-created.
     *
     * @return created cell
     */
    public ExtendedCell createCell(int column, String value) {
        ExtendedCell cell = (ExtendedCell) createCell(column, CellType.STRING);
        cell.setCellStyle(getWorkbook().getOrCreateCellStyle(ExtendedWorkbook.CellStylesNames.STRING));
        if (value != null) {
            cell.setCellValue(value);
        }
        return cell;
    }

    /**
     * Create numeric cell with given {@code value} in given {@code column}.<br/>
     * Created cell gets cellStyle named {@value ExtendedWorkbook.CellStylesNames#NUMERIC} from workbook.
     * If such a style does not exist yet, then it is pre-created.
     *
     * @return created cell
     */
    public ExtendedCell createCell(int column, BigDecimal value) {
        ExtendedCell cell = (ExtendedCell) createCell(column, CellType.NUMERIC);
        cell.setCellStyle(getWorkbook().getOrCreateCellStyle(ExtendedWorkbook.CellStylesNames.NUMERIC));
        if (value != null) {
            cell.setCellValue(value.doubleValue());
        }
        return cell;
    }

    /**
     * Create numeric cell with given {@code value} in given {@code column}.<br/>
     *
     * @return created cell
     */
    public ExtendedCell createCell(int column, Integer value) {
        ExtendedCell cell = (ExtendedCell) createCell(column, CellType.NUMERIC);
        cell.setCellStyle(getWorkbook().getOrCreateCellStyle(ExtendedWorkbook.CellStylesNames.NUMERIC));
        if (value != null) {
            cell.setCellValue(value);
        }
        return cell;
    }

    /**
     * Create string cell with given {@code value} in given {@code column}.<br/>
     * Created cell gets cellStyle named {@value ExtendedWorkbook.CellStylesNames#DATE_TIME} from workbook.
     * If such a style does not exist yet, then it is pre-created.
     *
     * @return created cell
     */
    public ExtendedCell createCell(int column, LocalDateTime value) {
        ExtendedCell cell = (ExtendedCell) createCell(column, CellType.NUMERIC);
        cell.setCellStyle(getWorkbook().getOrCreateCellStyle(ExtendedWorkbook.CellStylesNames.DATE_TIME));
        if (value != null) {
            cell.setCellValue(value);
        }
        return cell;
    }

    /**
     * Create string cell with given {@code value} in given {@code column}.<br/>
     * Created cell gets cellStyle named {@value ExtendedWorkbook.CellStylesNames#DATE_TIME} from workbook.
     * If such a style does not exist yet, then it is pre-created.
     *
     * @return created cell
     */
    public ExtendedCell createCell(int column, OffsetDateTime value) {
        LocalDateTime localDateTime = value == null ? null : value.toLocalDateTime();
        return createCell(column, localDateTime);
    }

    public ExtendedWorkbook getWorkbook() {
        return (ExtendedWorkbook) sheet.getWorkbook();
    }

    // endregion

    // region Row implementation

    @Override
    public Cell createCell(int column) {
        return createCell(column, CellType.BLANK);
    }

    @Override
    public Cell createCell(int column, CellType type) {
        ExtendedCell cell = new ExtendedCell(this, delegate.createCell(column, type));
        cells.put(column, cell);
        return cell;
    }

    @Override
    public void removeCell(Cell cell) {
        delegate.removeCell(cell);
        cells.remove(cell.getColumnIndex());
    }

    @Override
    public int getRowNum() {
        return delegate.getRowNum();
    }

    @Override
    public void setRowNum(int rowNum) {
        delegate.setRowNum(rowNum);
    }

    @Override
    public Cell getCell(int cellnum) {
        return getCell(cellnum, getWorkbook().getMissingCellPolicy());
    }

    @Override
    public Cell getCell(int cellnum, MissingCellPolicy policy) {
        Cell cell = cells.get(cellnum);
        switch (policy) {
            case RETURN_NULL_AND_BLANK:
                return cell;
            case RETURN_BLANK_AS_NULL:
                return cell != null && cell.getCellType() == CellType.BLANK ? null : cell;
            case CREATE_NULL_AS_BLANK:
                return cell == null ? createCell(cellnum) : cell;
            default:
                throw new IllegalArgumentException("Illegal policy " + policy);
        }
    }

    @Override
    public short getFirstCellNum() {
        return delegate.getFirstCellNum();
    }

    @Override
    public short getLastCellNum() {
        return delegate.getLastCellNum();
    }

    @Override
    public int getPhysicalNumberOfCells() {
        return delegate.getPhysicalNumberOfCells();
    }

    @Override
    public boolean getZeroHeight() {
        return delegate.getZeroHeight();
    }

    @Override
    public void setZeroHeight(boolean zHeight) {
        delegate.setZeroHeight(zHeight);
    }

    @Override
    public short getHeight() {
        return delegate.getHeight();
    }

    @Override
    public void setHeight(short height) {
        delegate.setHeight(height);
    }

    @Override
    public float getHeightInPoints() {
        return delegate.getHeightInPoints();
    }

    @Override
    public void setHeightInPoints(float height) {
        delegate.setHeightInPoints(height);
    }

    @Override
    public boolean isFormatted() {
        return delegate.isFormatted();
    }

    @Override
    public CellStyle getRowStyle() {
        return delegate.getRowStyle();
    }

    @Override
    public void setRowStyle(CellStyle style) {
        delegate.setRowStyle(style);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Iterator<Cell> cellIterator() {
        return (Iterator<Cell>) (Iterator<? extends Cell>) cells.values().iterator();
    }

    @Override
    public Sheet getSheet() {
        return this.sheet;
    }

    @Override
    public int getOutlineLevel() {
        return delegate.getOutlineLevel();
    }

    @Override
    public void shiftCellsRight(int firstShiftColumnIndex, int lastShiftColumnIndex, int step) {
        delegate.shiftCellsRight(firstShiftColumnIndex, lastShiftColumnIndex, step);
    }

    @Override
    public void shiftCellsLeft(int firstShiftColumnIndex, int lastShiftColumnIndex, int step) {
        delegate.shiftCellsLeft(firstShiftColumnIndex, lastShiftColumnIndex, step);
    }

    @NotNull
    @Override
    public Iterator<Cell> iterator() {
        return cellIterator();
    }

    // endregion

}