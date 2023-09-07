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

/**
 * Proxy class, implementing {@link Row} and having additional handy methods
 */
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

    /**
     * Creates cells with given {@code values} beginning from 0
     *
     * @return list of created cells
     */
    public List<ExtendedCell> createCells(final Object... values) {
        return createCells(0, values);
    }

    /**
     * Creates cells with given {@code values} beginning from given {@code column}
     *
     * @return list of created cells
     */
    public List<ExtendedCell> createCells(final int column, final Object... values) {
        final List<ExtendedCell> extendedCells = new ArrayList<>(values.length);
        int currentColumn = column;
        for (final Object value : values) {
            extendedCells.add(createCell(currentColumn, value));
            currentColumn++;
        }
        return extendedCells;
    }

    /**
     * Same as {@link ExtendedRow#createUnitedCell(int, Object, int)} with first parameter = 0
     */
    public void createUnitedCell(@Nullable final Object value, final int width) {
        createUnitedCell(0, value, width);
    }

    /**
     * Same as {@link ExtendedRow#createCell(int, Object)} with additional behaviour:<br/>
     * Cells beginning with given {@code column} are united into single cell.
     *
     * @param width count of united cells, can't be negative
     */
    public ExtendedCell createUnitedCell(final int column, @Nullable final Object value, final int width) {
        Assert.isTrue(width >= 0, "width can't be negative");

        final int rowNum = getRowNum();
        sheet.addMergedRegion(rowNum, rowNum, column, column + width - 1);
        return createCell(column, value);
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
     * {@link Timestamp}: Created cell gets {@link CellType#STRING} type and
     * style from workbook named {@value ExtendedWorkbook.CellStylesNames#DATE_TIME}.
     * * If such a style does not exist yet, then it is pre-created.<br/>
     * <p>
     * {@link Boolean}: Created cell gets {@link CellType#BOOLEAN} type and
     * style from workbook named {@value ExtendedWorkbook.CellStylesNames#BOOLEAN}.
     * If such a style does not exist yet, then it is pre-created.<br/>
     * <p>
     * If {@code value} is null than created cell gets {@link CellType#BLANK} type and no style.
     *
     * @return created cell
     * @throws IllegalArgumentException {@code value} have invalid type
     */
    public ExtendedCell createCell(final int column, @Nullable final Object value) {
        if (value == null) {
            return (ExtendedCell) createCell(column);
        } else if (value instanceof String stringValue) {
            return createCell(column, stringValue);
        } else if (value instanceof Money money) {
            return createCell(column, money);
        } else if (value instanceof MoneyValue moneyValue) {
            return createCell(column, moneyValue);
        } else if (value instanceof BigDecimal bigDecimalValue) {
            return createCell(column, bigDecimalValue);
        } else if (value instanceof Quotation quotationValue) {
            return createCell(column, quotationValue);
        } else if (value instanceof Double doubleValue) {
            return createCell(column, doubleValue);
        } else if (value instanceof Integer integerValue) {
            return createCell(column, integerValue);
        } else if (value instanceof Long longValue) {
            return createCell(column, longValue);
        } else if (value instanceof LocalDateTime localDateTimeValue) {
            return createCell(column, localDateTimeValue);
        } else if (value instanceof OffsetDateTime offsetDateTimeValue) {
            return createCell(column, offsetDateTimeValue);
        } else if (value instanceof Boolean booleanValue) {
            return createCell(column, booleanValue);
        } else if (value instanceof Timestamp timestamp) {
            return createCell(column, TimestampUtils.toOffsetDateTime(timestamp));
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
    public ExtendedCell createCell(final int column, final String value) {
        final ExtendedCell cell = (ExtendedCell) createCell(column, CellType.STRING);
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
    public ExtendedCell createCell(final int column, final BigDecimal value) {
        final Double doubleValue = value == null ? null : value.doubleValue();
        return createCell(column, doubleValue);
    }

    /**
     * Create numeric cell with given {@code value} in given {@code column}.<br/>
     * Created cell gets cellStyle named {@value ExtendedWorkbook.CellStylesNames#NUMERIC} from workbook.
     * If such a style does not exist yet, then it is pre-created.
     *
     * @return created cell
     */
    public ExtendedCell createCell(final int column, final Money value) {
        final BigDecimal bigDecimalValue = value == null ? null : value.getValue();
        return createCell(column, bigDecimalValue);
    }

    /**
     * Create numeric cell with given {@code value} in given {@code column}.<br/>
     * Created cell gets cellStyle named {@value ExtendedWorkbook.CellStylesNames#NUMERIC} from workbook.
     * If such a style does not exist yet, then it is pre-created.
     *
     * @return created cell
     */
    public ExtendedCell createCell(final int column, final MoneyValue value) {
        final BigDecimal bigDecimalValue = value == null ? null : DecimalUtils.newBigDecimal(value);
        return createCell(column, bigDecimalValue);
    }

    /**
     * Create numeric cell with given {@code value} in given {@code column}.<br/>
     * Created cell gets cellStyle named {@value ExtendedWorkbook.CellStylesNames#NUMERIC} from workbook.
     * If such a style does not exist yet, then it is pre-created.
     *
     * @return created cell
     */
    public ExtendedCell createCell(final int column, final Double value) {
        final ExtendedCell cell = (ExtendedCell) createCell(column, CellType.NUMERIC);
        cell.setCellStyle(getWorkbook().getOrCreateCellStyle(ExtendedWorkbook.CellStylesNames.NUMERIC));
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
    public ExtendedCell createCell(final int column, final Integer value) {
        final Double doubleValue = value == null ? null : value.doubleValue();
        return createCell(column, doubleValue);
    }

    /**
     * Create numeric cell with given {@code value} in given {@code column}.<br/>
     * Created cell gets cellStyle named {@value ExtendedWorkbook.CellStylesNames#NUMERIC} from workbook.
     * If such a style does not exist yet, then it is pre-created.
     *
     * @return created cell
     */
    public ExtendedCell createCell(final int column, final Long value) {
        final Double doubleValue = value == null ? null : value.doubleValue();
        return createCell(column, doubleValue);
    }

    /**
     * Create string cell with given {@code value} in given {@code column}.<br/>
     * Created cell gets cellStyle named {@value ExtendedWorkbook.CellStylesNames#DATE_TIME} from workbook.
     * If such a style does not exist yet, then it is pre-created.
     *
     * @return created cell
     */
    public ExtendedCell createCell(final int column, final LocalDateTime value) {
        final ExtendedCell cell = (ExtendedCell) createCell(column, CellType.NUMERIC);
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
    public ExtendedCell createCell(final int column, final OffsetDateTime value) {
        final LocalDateTime localDateTime = value == null ? null : value.toLocalDateTime();
        return createCell(column, localDateTime);
    }

    /**
     * Create boolean cell with given {@code value} in given {@code column}.<br/>
     * Created cell gets cellStyle named {@value ExtendedWorkbook.CellStylesNames#BOOLEAN} from workbook.
     * If such a style does not exist yet, then it is pre-created.
     *
     * @return created cell
     */
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
    public Cell getCell(int cellnum) {
        return getCell(cellnum, getWorkbook().getMissingCellPolicy());
    }

    @Override
    public Cell getCell(int cellnum, MissingCellPolicy policy) {
        Cell cell = cells.get(cellnum);
        return switch (policy) {
            case RETURN_NULL_AND_BLANK -> cell;
            case RETURN_BLANK_AS_NULL -> cell != null && cell.getCellType() == CellType.BLANK ? null : cell;
            case CREATE_NULL_AS_BLANK -> cell == null ? createCell(cellnum) : cell;
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