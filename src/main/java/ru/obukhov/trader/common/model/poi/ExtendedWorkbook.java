package ru.obukhov.trader.common.model.poi;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.formula.EvaluationWorkbook;
import org.apache.poi.ss.formula.udf.UDFFinder;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Name;
import org.apache.poi.ss.usermodel.PictureData;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.SheetVisibility;
import org.apache.poi.ss.usermodel.Workbook;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.util.Assert;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Proxy class, implementing {@link Workbook} and having additional handy methods
 */
public class ExtendedWorkbook implements Workbook {

    @Getter
    private final Workbook delegate;

    private final List<ExtendedSheet> sheets;
    private final Map<String, CellStyle> cellStyles;

    // region constructor

    public ExtendedWorkbook(Workbook delegate) {
        Assert.isTrue(delegate != null, "delegate can't be null");
        Assert.isTrue(!(delegate instanceof ExtendedWorkbook), "delegate can't be ExtendedWorkbook");

        this.delegate = delegate;
        this.sheets = initSheets(delegate);
        this.cellStyles = initCellStyles(delegate);
    }

    private static Map<String, CellStyle> initCellStyles(Workbook workbook) {
        final Map<String, CellStyle> cellStyles = new HashMap<>();
        for (int i = 0; i < workbook.getNumCellStyles(); i++) {
            CellStyle cellStyle = workbook.getCellStyleAt(i);
            cellStyles.put(getNewUniqueCellStyleName(cellStyles), cellStyle);
        }
        return cellStyles;
    }

    private static String getNewUniqueCellStyleName(Map<String, CellStyle> cellStyles) {
        int noNameCellStylesCount = 0;
        String cellStyleName;

        do {
            noNameCellStylesCount++;
            cellStyleName = CellStylesNames.NO_NAME_PREFIX + noNameCellStylesCount;
        } while (cellStyles.containsKey(cellStyleName));

        return cellStyleName;
    }

    private List<ExtendedSheet> initSheets(Workbook delegate) {
        return StreamSupport.stream(delegate.spliterator(), false)
                .map(this::createSheet)
                .collect(Collectors.toList());
    }

    private ExtendedSheet createSheet(Sheet sheet) {
        return new ExtendedSheet(this, sheet);
    }

    // endregion

    // region additional methods

    /**
     * Create new cell style with given {@code name}
     *
     * @return created style
     * @throws IllegalArgumentException when style with given {@code name} already exists
     */
    public CellStyle createCellStyle(String name) {
        if (cellStyles.containsKey(name)) {
            throw new IllegalArgumentException("Cell style '" + name + "' already exists");
        }

        CellStyle cellStyle = delegate.createCellStyle();
        cellStyles.put(name, cellStyle);

        return cellStyle;
    }

    /**
     * @return cell style with given {@code name} or null if it does not exists
     */
    @Nullable
    public CellStyle getCellStyle(String name) {
        return cellStyles.get(name);
    }

    /**
     * if cell style with given {@code name} missing in the workbook, then creates new one
     *
     * @return created or existing style
     */
    public CellStyle getOrCreateCellStyle(String name) {
        return cellStyles.computeIfAbsent(name, key -> delegate.createCellStyle());
    }

    // endregion

    // region Workbook implementation

    @Override
    public int getActiveSheetIndex() {
        return delegate.getActiveSheetIndex();
    }

    @Override
    public void setActiveSheet(int sheetIndex) {
        delegate.setActiveSheet(sheetIndex);
    }

    @Override
    public int getFirstVisibleTab() {
        return delegate.getFirstVisibleTab();
    }

    @Override
    public void setFirstVisibleTab(int sheetIndex) {
        delegate.setFirstVisibleTab(sheetIndex);
    }

    @Override
    public void setSheetOrder(String sheetname, int pos) {
        int index = getSheetIndex(sheetname);
        sheets.add(pos, sheets.remove(index));
        delegate.setSheetOrder(sheetname, pos);
    }

    @Override
    public void setSelectedTab(int index) {
        delegate.setSelectedTab(index);
    }

    @Override
    public void setSheetName(int sheet, String name) {
        delegate.setSheetName(sheet, name);
    }

    @Override
    public String getSheetName(int sheet) {
        return delegate.getSheetName(sheet);
    }

    @Override
    public int getSheetIndex(String name) {
        return delegate.getSheetIndex(name);
    }

    @Override
    public int getSheetIndex(Sheet sheet) {
        return delegate.getSheetIndex(sheet);
    }

    @Override
    public Sheet createSheet() {
        ExtendedSheet sheet = new ExtendedSheet(this, delegate.createSheet());
        sheets.add(sheet);
        return sheet;
    }

    @Override
    public Sheet createSheet(String sheetname) {
        ExtendedSheet sheet = new ExtendedSheet(this, delegate.createSheet(sheetname));
        sheets.add(sheet);
        return sheet;
    }

    @Override
    public Sheet cloneSheet(int sheetNum) {
        ExtendedSheet sheet = new ExtendedSheet(this, delegate.cloneSheet(sheetNum));
        sheets.add(sheet);
        return sheet;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Iterator<Sheet> sheetIterator() {
        return (Iterator<Sheet>) (Iterator<? extends Sheet>) sheets.iterator();
    }

    @Override
    public int getNumberOfSheets() {
        return delegate.getNumberOfSheets();
    }

    @Override
    public Sheet getSheetAt(int index) {
        return sheets.get(index);
    }

    @Override
    public Sheet getSheet(String name) {
        return sheets.stream()
                .filter(sheet -> sheet.getSheetName().equals(name))
                .findFirst()
                .orElse(null);
    }

    @Override
    public void removeSheetAt(int index) {
        delegate.removeSheetAt(index);
        sheets.remove(index);
    }

    @Override
    public Font createFont() {
        return delegate.createFont();
    }

    @Override
    public Font findFont(boolean bold,
                         short color,
                         short fontHeight,
                         String name,
                         boolean italic,
                         boolean strikeout,
                         short typeOffset,
                         byte underline) {
        return delegate.findFont(bold, color, fontHeight, name, italic, strikeout, typeOffset, underline);
    }

    @Override
    public int getNumberOfFonts() {
        return delegate.getNumberOfFonts();
    }

    @Override
    @SuppressWarnings("deprecation")
    public int getNumberOfFontsAsInt() {
        return delegate.getNumberOfFontsAsInt();
    }

    @Override
    public Font getFontAt(int idx) {
        return delegate.getFontAt(idx);
    }

    @Override
    public CellStyle createCellStyle() {
        String cellStyleName = getNewUniqueCellStyleName(cellStyles);
        CellStyle cellStyle = delegate.createCellStyle();
        cellStyles.put(cellStyleName, cellStyle);
        return cellStyle;
    }

    @Override
    public int getNumCellStyles() {
        return delegate.getNumCellStyles();
    }

    @Override
    public CellStyle getCellStyleAt(int idx) {
        return delegate.getCellStyleAt(idx);
    }

    @Override
    public void write(OutputStream stream) throws IOException {
        delegate.write(stream);
    }

    @Override
    public void close() throws IOException {
        delegate.close();
    }

    @Override
    public int getNumberOfNames() {
        return delegate.getNumberOfNames();
    }

    @Override
    public Name getName(String name) {
        return delegate.getName(name);
    }

    @Override
    public List<? extends Name> getNames(String name) {
        return delegate.getNames(name);
    }

    @Override
    public List<? extends Name> getAllNames() {
        return delegate.getAllNames();
    }

    @Override
    public Name createName() {
        return delegate.createName();
    }

    @Override
    public void removeName(Name name) {
        delegate.removeName(name);
    }

    @Override
    public int linkExternalWorkbook(String name, Workbook workbook) {
        return delegate.linkExternalWorkbook(name, workbook);
    }

    @Override
    public void setPrintArea(int sheetIndex, String reference) {
        delegate.setPrintArea(sheetIndex, reference);
    }

    @Override
    public void setPrintArea(int sheetIndex, int startColumn, int endColumn, int startRow, int endRow) {
        delegate.setPrintArea(sheetIndex, startColumn, endColumn, startRow, endRow);
    }

    @Override
    public String getPrintArea(int sheetIndex) {
        return delegate.getPrintArea(sheetIndex);
    }

    @Override
    public void removePrintArea(int sheetIndex) {
        delegate.removePrintArea(sheetIndex);
    }

    @Override
    public Row.MissingCellPolicy getMissingCellPolicy() {
        return delegate.getMissingCellPolicy();
    }

    @Override
    public void setMissingCellPolicy(Row.MissingCellPolicy missingCellPolicy) {
        delegate.setMissingCellPolicy(missingCellPolicy);
    }

    @Override
    public DataFormat createDataFormat() {
        return delegate.createDataFormat();
    }

    @Override
    public int addPicture(byte[] pictureData, int format) {
        return delegate.addPicture(pictureData, format);
    }

    @Override
    public List<? extends PictureData> getAllPictures() {
        return delegate.getAllPictures();
    }

    @Override
    public CreationHelper getCreationHelper() {
        return delegate.getCreationHelper();
    }

    @Override
    public boolean isHidden() {
        return delegate.isHidden();
    }

    @Override
    public void setHidden(boolean hiddenFlag) {
        delegate.setHidden(hiddenFlag);
    }

    @Override
    public boolean isSheetHidden(int sheetIx) {
        return delegate.isSheetHidden(sheetIx);
    }

    @Override
    public boolean isSheetVeryHidden(int sheetIx) {
        return delegate.isSheetVeryHidden(sheetIx);
    }

    @Override
    public void setSheetHidden(int sheetIx, boolean hidden) {
        delegate.setSheetHidden(sheetIx, hidden);
    }

    @Override
    public SheetVisibility getSheetVisibility(int sheetIx) {
        return delegate.getSheetVisibility(sheetIx);
    }

    @Override
    public void setSheetVisibility(int sheetIx, SheetVisibility visibility) {
        delegate.setSheetVisibility(sheetIx, visibility);
    }

    @Override
    public void addToolPack(UDFFinder toopack) {
        delegate.addToolPack(toopack);
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
    public SpreadsheetVersion getSpreadsheetVersion() {
        return delegate.getSpreadsheetVersion();
    }

    @Override
    public int addOlePackage(byte[] oleData, String label, String fileName, String command) throws IOException {
        return delegate.addOlePackage(oleData, label, fileName, command);
    }

    @Override
    public EvaluationWorkbook createEvaluationWorkbook() {
        return delegate.createEvaluationWorkbook();
    }

    // endregion

    // region Iterable implementation

    @NotNull
    @Override
    public Iterator<Sheet> iterator() {
        return sheetIterator();
    }

    @Override
    public void forEach(Consumer<? super Sheet> action) {
        delegate.forEach(action);
    }

    @Override
    public Spliterator<Sheet> spliterator() {
        return delegate.spliterator();
    }

    // endregion

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class CellStylesNames {
        public static final String NO_NAME_PREFIX = "noName";
        public static final String NUMERIC = "numeric";
        public static final String STRING = "string";
        public static final String DATE_TIME = "dateTime";
        public static final String PERCENT = "percent";
    }

}