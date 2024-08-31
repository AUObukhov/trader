package ru.obukhov.trader.common.model.poi;

import com.google.protobuf.Timestamp;
import lombok.Getter;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.util.Assert;
import ru.obukhov.trader.common.util.DateUtils;
import ru.obukhov.trader.common.util.DecimalUtils;
import ru.obukhov.trader.common.util.TimestampUtils;
import ru.tinkoff.piapi.contract.v1.MoneyValue;
import ru.tinkoff.piapi.contract.v1.Quotation;
import ru.tinkoff.piapi.core.models.Money;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class ExtendedRow implements Row {

    private final ExtendedSheet sheet;

    @Getter
    private final Row delegate;

    private final TreeMap<Integer, ExtendedCell> cells;

    // region constructor

    public ExtendedRow(final ExtendedSheet sheet, final Row delegate) {
        Assert.isTrue(sheet != null, "sheet can't be null");
        Assert.isTrue(delegate != null, "delegate can't be null");
        Assert.isTrue(!(delegate instanceof ExtendedRow), "delegate can't be ExtendedRow");

        this.sheet = sheet;
        this.delegate = delegate;
        this.cells = initCells(delegate);
    }

    private TreeMap<Integer, ExtendedCell> initCells(final Row delegate) {
        final Map<Integer, ExtendedCell> map = StreamSupport.stream(delegate.spliterator(), false)
                .collect(Collectors.toMap(Cell::getColumnIndex, this::initCell));
        return new TreeMap<>(map);
    }

    private ExtendedCell initCell(final Cell cell) {
        return new ExtendedCell(this, cell);
    }

    // endregion

    // region additional methods

    public List<ExtendedCell> createCells(final Object... values) {
        return createCells(0, values);
    }

    public List<ExtendedCell> createCells(final int column, final Object... values) {
        final List<ExtendedCell> extendedCells = new ArrayList<>(values.length);
        int currentColumn = column;
        for (final Object value : values) {
            extendedCells.add(createCell(currentColumn, value));
            currentColumn++;
        }
        return extendedCells;
    }

    public void createUnitedCell(@Nullable final Object value, final int width) {
        createUnitedCell(0, value, width);
    }

    public ExtendedCell createUnitedCell(final int column, @Nullable final Object value, final int width) {
        Assert.isTrue(width >= 0, "width can't be negative");

        final int rowNum = getRowNum();
        sheet.addMergedRegion(rowNum, rowNum, column, column + width - 1);
        return createCell(column, value);
    }

    public ExtendedCell createCell(final int column, @Nullable final Object value) {
        return switch (value) {
            case null -> (ExtendedCell) createCell(column);
            case String stringValue -> createCell(column, stringValue);
            case Money money -> createCell(column, money);
            case MoneyValue moneyValue -> createCell(column, moneyValue);
            case BigDecimal bigDecimalValue -> createCell(column, bigDecimalValue);
            case Quotation quotationValue -> createCell(column, quotationValue);
            case Double doubleValue -> createCell(column, doubleValue);
            case Integer integerValue -> createCell(column, integerValue);
            case Long longValue -> createCell(column, longValue);
            case LocalDateTime localDateTimeValue -> createCell(column, localDateTimeValue);
            case OffsetDateTime offsetDateTimeValue -> createCell(column, offsetDateTimeValue);
            case Boolean booleanValue -> createCell(column, booleanValue);
            case Timestamp timestamp -> createCell(column, TimestampUtils.toOffsetDateTime(timestamp));
            default -> throw new IllegalArgumentException("Unexpected type of value: " + value.getClass());
        };
    }

    public ExtendedCell createCell(final int column, final String value) {
        final ExtendedCell cell = (ExtendedCell) createCell(column, CellType.STRING);
        cell.setCellStyle(getWorkbook().getOrCreateCellStyle(ExtendedWorkbook.CellStylesNames.STRING));
        if (value != null) {
            cell.setCellValue(value);
        }
        return cell;
    }

    public ExtendedCell createCell(final int column, final BigDecimal value) {
        final Double doubleValue = value == null ? null : value.doubleValue();
        return createCell(column, doubleValue);
    }

    public ExtendedCell createCell(final int column, final Money value) {
        final BigDecimal bigDecimalValue = value == null ? null : value.getValue();
        return createCell(column, bigDecimalValue);
    }

    public ExtendedCell createCell(final int column, final MoneyValue value) {
        final BigDecimal bigDecimalValue = value == null ? null : DecimalUtils.newBigDecimal(value);
        return createCell(column, bigDecimalValue);
    }

    public ExtendedCell createCell(final int column, final Double value) {
        final ExtendedCell cell = (ExtendedCell) createCell(column, CellType.NUMERIC);
        cell.setCellStyle(getWorkbook().getOrCreateCellStyle(ExtendedWorkbook.CellStylesNames.NUMERIC));
        if (value != null) {
            cell.setCellValue(value);
        }
        return cell;
    }

    public ExtendedCell createCell(final int column, final Integer value) {
        final Double doubleValue = value == null ? null : value.doubleValue();
        return createCell(column, doubleValue);
    }

    public ExtendedCell createCell(final int column, final Long value) {
        final Double doubleValue = value == null ? null : value.doubleValue();
        return createCell(column, doubleValue);
    }

    public ExtendedCell createCell(final int column, final LocalDateTime value) {
        final ExtendedCell cell = (ExtendedCell) createCell(column, CellType.NUMERIC);
        cell.setCellStyle(getWorkbook().getOrCreateCellStyle(ExtendedWorkbook.CellStylesNames.DATE_TIME));
        if (value != null) {
            cell.setCellValue(value);
        }
        return cell;
    }

    public ExtendedCell createCell(final int column, final OffsetDateTime value) {
        final LocalDateTime localDateTime = value == null ? null : DateUtils.toLocalDateTime(value);
        return createCell(column, localDateTime);
    }

    public ExtendedCell createCell(final int column, final Boolean value) {
        final ExtendedCell cell = (ExtendedCell) createCell(column, CellType.BOOLEAN);
        cell.setCellStyle(getWorkbook().getOrCreateCellStyle(ExtendedWorkbook.CellStylesNames.BOOLEAN));
        if (value != null) {
            cell.setCellValue(value);

        }
        return cell;
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
    public Cell getCell(int cellNum) {
        return getCell(cellNum, getWorkbook().getMissingCellPolicy());
    }

    @Override
    public Cell getCell(int cellNum, MissingCellPolicy policy) {
        Cell cell = cells.get(cellNum);
        return switch (policy) {
            case RETURN_NULL_AND_BLANK -> cell;
            case RETURN_BLANK_AS_NULL -> cell != null && cell.getCellType() == CellType.BLANK ? null : cell;
            case CREATE_NULL_AS_BLANK -> cell == null ? createCell(cellNum) : cell;
        };
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