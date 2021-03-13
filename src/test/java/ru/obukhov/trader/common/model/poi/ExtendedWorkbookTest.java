package ru.obukhov.trader.common.model.poi;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Test;
import ru.obukhov.trader.test.utils.AssertUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

public class ExtendedWorkbookTest {

    // region constructor tests

    @Test
    public void constructor_throwsIllegalArgumentException_whenDelegateIsNull() {
        AssertUtils.assertThrowsWithMessage(() -> new ExtendedWorkbook(null),
                IllegalArgumentException.class,
                "delegate can't be null");
    }

    @Test
    public void constructor_throwsIllegalArgumentException_whenDelegateIsExtendedWorkbook() {
        ExtendedWorkbook extendedWorkbook = ExcelTestDataHelper.createExtendedWorkbook();

        AssertUtils.assertThrowsWithMessage(() -> new ExtendedWorkbook(extendedWorkbook),
                IllegalArgumentException.class,
                "delegate can't be ExtendedWorkbook");
    }

    @Test
    public void constructor_CopiesSheets() {
        Workbook workbook = new XSSFWorkbook();
        String sheetName0 = "sheet0";
        String sheetName1 = "sheet1";
        List<Sheet> sheets = ExcelTestDataHelper.createSheets(workbook, sheetName0, sheetName1);
        ExtendedWorkbook extendedWorkbook = new ExtendedWorkbook(workbook);

        assertEquals(2, extendedWorkbook.getNumberOfSheets());

        ExtendedSheet extendedSheet0 = (ExtendedSheet) extendedWorkbook.getSheet(sheetName0);
        ExtendedSheet extendedSheet1 = (ExtendedSheet) extendedWorkbook.getSheet(sheetName1);
        assertEquals(sheets.get(0), extendedSheet0.getDelegate());
        assertEquals(sheets.get(1), extendedSheet1.getDelegate());
    }

    @Test
    public void constructor_CopiesCellStyles() {
        Workbook workbook = new XSSFWorkbook();
        workbook.createCellStyle();
        workbook.createCellStyle();

        ExtendedWorkbook extendedWorkbook = new ExtendedWorkbook(workbook);

        assertEquals(workbook.getNumCellStyles(), extendedWorkbook.getNumCellStyles());
        for (int i = 0; i < workbook.getNumCellStyles(); i++) {
            assertEquals(workbook.getCellStyleAt(i), extendedWorkbook.getCellStyleAt(i));
        }
    }

    // endregion

    // region createCellStyle with name tests

    @Test
    public void createCellStyle_withName_throwsIllegalArgumentException_whenCellStyleAlreadyExists() {
        ExtendedWorkbook extendedWorkbook = ExcelTestDataHelper.createExtendedWorkbook();
        String cellStyleName = "cellStyle";
        extendedWorkbook.createCellStyle(cellStyleName);

        AssertUtils.assertThrowsWithMessage(() -> extendedWorkbook.createCellStyle(cellStyleName),
                IllegalArgumentException.class,
                "Cell style 'cellStyle' already exists");
    }

    @Test
    public void createCellStyle_withName_createsCellStyle() {
        Workbook workbook = new XSSFWorkbook();
        ExtendedWorkbook extendedWorkbook = new ExtendedWorkbook(workbook);
        int initialNumCellStyles = extendedWorkbook.getNumCellStyles();

        CellStyle cellStyle = extendedWorkbook.createCellStyle("cellStyle");

        assertNotNull(cellStyle);
        assertEquals(initialNumCellStyles + 1, workbook.getNumCellStyles());
        assertEquals(initialNumCellStyles + 1, extendedWorkbook.getNumCellStyles());
    }

    @Test
    public void createCellStyle_withNoName_createsCellStyle() {
        Workbook workbook = new XSSFWorkbook();
        ExtendedWorkbook extendedWorkbook = new ExtendedWorkbook(workbook);
        int initialNumCellStyles = extendedWorkbook.getNumCellStyles();

        CellStyle cellStyle = extendedWorkbook.createCellStyle();

        assertNotNull(cellStyle);
        assertEquals(initialNumCellStyles + 1, workbook.getNumCellStyles());
        assertEquals(initialNumCellStyles + 1, extendedWorkbook.getNumCellStyles());
    }

    // endregion

    // region getCellStyle tests

    @Test
    public void getCellStyle_returnsCreatedCellStyle() {
        ExtendedWorkbook extendedWorkbook = ExcelTestDataHelper.createExtendedWorkbook();

        String cellStyleName = "cellStyle";
        CellStyle createdCellStyle = extendedWorkbook.createCellStyle(cellStyleName);

        CellStyle returnedCellStyle = extendedWorkbook.getCellStyle(cellStyleName);

        assertSame(createdCellStyle, returnedCellStyle);
    }

    // endregion

    // region getOrCreateCellStyle tests

    @Test
    public void getOrCreateCellStyle_returnsExistingCellStyle_whenCellStyleWithGivenNameExists() {
        ExtendedWorkbook extendedWorkbook = ExcelTestDataHelper.createExtendedWorkbook();

        String cellStyleName = "cellStyle";
        CellStyle existingCellStyle = extendedWorkbook.createCellStyle(cellStyleName);

        CellStyle newCellStyle = extendedWorkbook.getOrCreateCellStyle(cellStyleName);

        assertSame(existingCellStyle, newCellStyle);
    }

    @Test
    public void getOrCreateCellStyle_notAddsCellStyle_whenCellStyleWithGivenNameExists() {
        ExtendedWorkbook extendedWorkbook = ExcelTestDataHelper.createExtendedWorkbook();

        String cellStyleName = "cellStyle";
        extendedWorkbook.createCellStyle(cellStyleName);
        int initialNumCellStyles = extendedWorkbook.getNumCellStyles();

        extendedWorkbook.getOrCreateCellStyle(cellStyleName);

        assertEquals(initialNumCellStyles, extendedWorkbook.getNumCellStyles());
    }

    @Test
    public void getOrCreateCellStyle_addsCellStyle_whenCellStyleWithGivenNameNotExists() {
        ExtendedWorkbook extendedWorkbook = ExcelTestDataHelper.createExtendedWorkbook();
        int initialNumCellStyles = extendedWorkbook.getNumCellStyles();

        extendedWorkbook.getOrCreateCellStyle("cellStyle");

        assertEquals(initialNumCellStyles + 1, extendedWorkbook.getNumCellStyles());
    }

    // endregion

    // region setSheetOrder tests

    @Test
    public void setSheetOrder_changesOrder_whenSheetMovedAfterCurrentPosition() {
        Workbook workbook = new XSSFWorkbook();
        ExtendedWorkbook extendedWorkbook = new ExtendedWorkbook(workbook);

        String sheetName1 = "sheet1";
        String sheetName2 = "sheet2";
        String sheetName3 = "sheet3";
        ExcelTestDataHelper.createSheets(extendedWorkbook, sheetName1, sheetName2, sheetName3);

        extendedWorkbook.setSheetOrder(sheetName1, 1);

        assertSame(workbook.getSheetAt(0), workbook.getSheet(sheetName2));
        assertSame(workbook.getSheetAt(1), workbook.getSheet(sheetName1));
        assertSame(workbook.getSheetAt(2), workbook.getSheet(sheetName3));
        assertSame(extendedWorkbook.getSheetAt(0), extendedWorkbook.getSheet(sheetName2));
        assertSame(extendedWorkbook.getSheetAt(1), extendedWorkbook.getSheet(sheetName1));
        assertSame(extendedWorkbook.getSheetAt(2), extendedWorkbook.getSheet(sheetName3));
    }

    @Test
    public void setSheetOrder_changesOrder_whenSheetMovedBeforeCurrentPosition() {
        Workbook workbook = new XSSFWorkbook();
        ExtendedWorkbook extendedWorkbook = new ExtendedWorkbook(workbook);

        String sheetName1 = "sheet1";
        String sheetName2 = "sheet2";
        String sheetName3 = "sheet3";
        ExcelTestDataHelper.createSheets(extendedWorkbook, sheetName1, sheetName2, sheetName3);

        extendedWorkbook.setSheetOrder(sheetName3, 1);

        assertSame(workbook.getSheetAt(0), workbook.getSheet(sheetName1));
        assertSame(workbook.getSheetAt(1), workbook.getSheet(sheetName3));
        assertSame(workbook.getSheetAt(2), workbook.getSheet(sheetName2));
        assertSame(extendedWorkbook.getSheetAt(0), extendedWorkbook.getSheet(sheetName1));
        assertSame(extendedWorkbook.getSheetAt(1), extendedWorkbook.getSheet(sheetName3));
        assertSame(extendedWorkbook.getSheetAt(2), extendedWorkbook.getSheet(sheetName2));
    }

    @Test
    public void setSheetOrder_notChangesOrder_whenSheetMovedOnCurrentPosition() {
        Workbook workbook = new XSSFWorkbook();
        ExtendedWorkbook extendedWorkbook = new ExtendedWorkbook(workbook);

        String sheetName1 = "sheet1";
        String sheetName2 = "sheet2";
        String sheetName3 = "sheet3";
        ExcelTestDataHelper.createSheets(extendedWorkbook, sheetName1, sheetName2, sheetName3);

        extendedWorkbook.setSheetOrder(sheetName2, 1);

        assertSame(workbook.getSheetAt(0), workbook.getSheet(sheetName1));
        assertSame(workbook.getSheetAt(1), workbook.getSheet(sheetName2));
        assertSame(workbook.getSheetAt(2), workbook.getSheet(sheetName3));
        assertSame(extendedWorkbook.getSheetAt(0), extendedWorkbook.getSheet(sheetName1));
        assertSame(extendedWorkbook.getSheetAt(1), extendedWorkbook.getSheet(sheetName2));
        assertSame(extendedWorkbook.getSheetAt(2), extendedWorkbook.getSheet(sheetName3));
    }

    // endregion

    // region createSheet tests

    @Test
    public void createSheet_withNoName_createsSheet() {
        Workbook workbook = new XSSFWorkbook();
        ExtendedWorkbook extendedWorkbook = new ExtendedWorkbook(workbook);

        Sheet sheet = extendedWorkbook.createSheet();

        assertNotNull(sheet);
        assertEquals(1, extendedWorkbook.getNumberOfSheets());
        assertEquals(1, workbook.getNumberOfSheets());
        assertNotNull(extendedWorkbook.getSheetAt(0));
        assertNotNull(workbook.getSheetAt(0));
    }

    @Test
    public void createSheet_withName_createsSheet() {
        Workbook workbook = new XSSFWorkbook();
        ExtendedWorkbook extendedWorkbook = new ExtendedWorkbook(workbook);

        String sheetName = "sheet";
        Sheet sheet = extendedWorkbook.createSheet(sheetName);

        assertNotNull(sheet);
        assertEquals(1, extendedWorkbook.getNumberOfSheets());
        assertEquals(1, workbook.getNumberOfSheets());
        assertNotNull(extendedWorkbook.getSheet(sheetName));
        assertNotNull(workbook.getSheet(sheetName));
    }

    // endregion

    // region cloneSheet tests

    @Test
    public void cloneSheet_createsSheet() {
        Workbook workbook = new XSSFWorkbook();
        ExtendedWorkbook extendedWorkbook = new ExtendedWorkbook(workbook);
        String sheetName = "sheet";
        extendedWorkbook.createSheet(sheetName);

        Sheet clonedSheet = extendedWorkbook.cloneSheet(0);

        assertNotNull(clonedSheet);
        assertEquals(2, extendedWorkbook.getNumberOfSheets());
        assertEquals(2, workbook.getNumberOfSheets());

        String expectedNewSheetName = sheetName + " (2)";
        assertNotNull(extendedWorkbook.getSheet(expectedNewSheetName));
        assertNotNull(workbook.getSheet(expectedNewSheetName));
    }

    // endregion

}