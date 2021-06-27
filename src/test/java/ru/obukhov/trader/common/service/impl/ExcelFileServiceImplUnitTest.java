package ru.obukhov.trader.common.service.impl;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Rule;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.rules.TemporaryFolder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.obukhov.trader.config.properties.ReportProperties;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.test.utils.TestDataHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

@ExtendWith(MockitoExtension.class)
class ExcelFileServiceImplUnitTest {

    @Rule
    public TemporaryFolder temporaryFolder = TemporaryFolder.builder().assureDeletion().build();

    @Mock
    private ReportProperties reportProperties;
    @Mock
    private Runtime runtime;

    @InjectMocks
    private ExcelFileServiceImpl service;

    @BeforeEach
    void setUp() throws IOException {
        temporaryFolder.create();

        Mockito.when(reportProperties.getSaveDirectory()).thenReturn(temporaryFolder.getRoot().getAbsolutePath());
    }

    @AfterEach
    void after() {
        temporaryFolder.delete();
    }

    @Test
    @SuppressWarnings("unused")
    void saveToFile_throwsIllegalArgumentException_whenFailsToCreateFile() throws IOException {
        final String fileName = "file.xlsx";
        final String absolutePath = reportProperties.getSaveDirectory() + "\\" + fileName;
        final Workbook workbook = Mockito.mock(Workbook.class);

        MockedConstruction.MockInitializer<File> fileMockInitializer =
                (mock, context) -> {
                    Mockito.when(mock.createNewFile()).thenReturn(false);
                    Mockito.when(mock.getAbsolutePath()).thenReturn(absolutePath);
                };

        try (MockedConstruction<File> fileConstruction = Mockito.mockConstruction(File.class, fileMockInitializer)) {
            final Throwable throwable = Assertions.assertThrows(
                    IllegalStateException.class,
                    () -> service.saveToFile(workbook, fileName)
            );
            Assertions.assertEquals("Failed to create file " + absolutePath, throwable.getMessage());

            Mockito.verify(workbook, Mockito.never()).write(Mockito.any(FileOutputStream.class));
            Mockito.verify(workbook, Mockito.never()).close();
        }
    }

    @Test
    @SuppressWarnings("unused")
    void saveToFile_writesWorkbookToFile() throws IOException {
        final String fileName = "file.xlsx";
        final String sheetName = "sheet";

        final Workbook inputWorkbook = new XSSFWorkbook();
        inputWorkbook.createSheet(sheetName);

        try (final MockedStatic<Runtime> runtimeStaticMock = TestDataHelper.mockRuntime(runtime)) {

            service.saveToFile(inputWorkbook, fileName);

            final File file = new File(reportProperties.getSaveDirectory(), fileName);
            try (final Workbook outputWorkbook = readWorkbook(file)) {
                AssertUtils.assertEqualSheetNames(inputWorkbook, outputWorkbook);
            }

            Mockito.verify(runtime, Mockito.times(1))
                    .exec(new String[]{"explorer", file.getAbsolutePath()});
        }
    }

    @Test
    @SuppressWarnings("unused")
    void saveToFile_createsFileWithUniqueName_whenFileAlreadyExists() throws IOException {
        final String fileName = "file.xlsx";
        final String fileName1 = "file (1).xlsx";
        final String fileName2 = "file (2).xlsx";
        final String sheetName = "sheet";

        final Workbook inputWorkbook = new XSSFWorkbook();
        inputWorkbook.createSheet(sheetName);

        try (final MockedStatic<Runtime> runtimeStaticMock = TestDataHelper.mockRuntime(runtime)) {
            createFile(fileName);
            createFile(fileName1);

            service.saveToFile(inputWorkbook, fileName);

            final File file = new File(reportProperties.getSaveDirectory(), fileName2);
            try (final Workbook outputWorkbook = readWorkbook(file)) {
                AssertUtils.assertEqualSheetNames(inputWorkbook, outputWorkbook);
            }

            Mockito.verify(runtime, Mockito.times(1))
                    .exec(new String[]{"explorer", file.getAbsolutePath()});
        }
    }

    private Workbook readWorkbook(final File file) throws IOException {
        final FileInputStream inputStream = new FileInputStream(file);
        return new XSSFWorkbook(inputStream);
    }

    private void createFile(String fileName) throws IOException {
        final File file = new File(reportProperties.getSaveDirectory(), fileName);
        Assertions.assertTrue(file.createNewFile());
    }

}