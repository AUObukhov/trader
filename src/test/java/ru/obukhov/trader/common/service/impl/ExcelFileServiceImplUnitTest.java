package ru.obukhov.trader.common.service.impl;

import org.apache.poi.ss.usermodel.Workbook;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import ru.obukhov.trader.BaseMockedTest;
import ru.obukhov.trader.config.ReportProperties;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

class ExcelFileServiceImplUnitTest extends BaseMockedTest {

    @Mock
    private ReportProperties reportProperties;
    @Mock
    private Workbook workbook;

    @Test
    @SuppressWarnings("unused")
    void saveToFile_throwsIllegalArgumentException_whenFailsToCreateFile() throws IOException {

        String saveDirectory = "save directory";
        String fileName = "file";
        String absolutePath = "absolute path";

        Mockito.when(reportProperties.getSaveDirectory()).thenReturn(saveDirectory);

        ExcelFileServiceImpl service = new ExcelFileServiceImpl(reportProperties);

        MockedConstruction.MockInitializer<File> fileMockInitializer =
                (mock, context) -> {
                    Mockito.when(mock.createNewFile()).thenReturn(false);
                    Mockito.when(mock.getAbsolutePath()).thenReturn(absolutePath);
                };

        try (MockedConstruction<File> fileConstruction = Mockito.mockConstruction(File.class, fileMockInitializer)) {
            Throwable throwable = Assertions.assertThrows(
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

        String saveDirectory = "save directory";
        String fileName = "file";

        Mockito.when(reportProperties.getSaveDirectory()).thenReturn(saveDirectory);

        ExcelFileServiceImpl service = new ExcelFileServiceImpl(reportProperties);

        MockedConstruction.MockInitializer<File> fileMockInitializer =
                (mock, context) -> Mockito.when(mock.createNewFile()).thenReturn(true);

        try (
                MockedConstruction<File> fileConstruction = Mockito.mockConstruction(File.class, fileMockInitializer);
                MockedConstruction<FileOutputStream> fileOutputStreamConstruction =
                        Mockito.mockConstruction(FileOutputStream.class)
        ) {
            service.saveToFile(workbook, fileName);

            FileOutputStream fileOutputStream = fileOutputStreamConstruction.constructed().get(0);

            Mockito.verify(workbook, Mockito.times(1)).write(fileOutputStream);
            Mockito.verify(workbook, Mockito.times(1)).close();
        }

    }

}