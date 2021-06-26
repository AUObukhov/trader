package ru.obukhov.trader.common.service.impl;

import org.apache.poi.ss.usermodel.Workbook;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.obukhov.trader.config.properties.ReportProperties;
import ru.obukhov.trader.test.utils.TestDataHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

@ExtendWith(MockitoExtension.class)
class ExcelFileServiceImplUnitTest {

    @Mock
    private ReportProperties reportProperties;
    @Mock
    private Workbook workbook;
    @Mock
    private Runtime runtime;

    @InjectMocks
    private ExcelFileServiceImpl service;

    @BeforeEach
    void setUp() {
        final String saveDirectory = "save directory";

        Mockito.when(reportProperties.getSaveDirectory()).thenReturn(saveDirectory);
    }

    @Test
    @SuppressWarnings("unused")
    void saveToFile_throwsIllegalArgumentException_whenFailsToCreateFile() throws IOException {
        final String fileName = "file";
        final String absolutePath = "absolute path";

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
        final String fileName = "file";

        MockedConstruction.MockInitializer<File> fileMockInitializer =
                (mock, context) -> {
                    Mockito.when(mock.createNewFile()).thenReturn(true);
                    Mockito.when(mock.getAbsolutePath()).thenReturn(fileName);
                };

        try (
                final MockedStatic<Runtime> runtimeStaticMock = TestDataHelper.mockRuntime(runtime);
                final MockedConstruction<File> fileConstruction = Mockito.mockConstruction(File.class, fileMockInitializer);
                final MockedConstruction<FileOutputStream> fileOutputStreamConstruction =
                        Mockito.mockConstruction(FileOutputStream.class)
        ) {
            service.saveToFile(workbook, fileName);

            final FileOutputStream fileOutputStream = fileOutputStreamConstruction.constructed().get(0);

            Mockito.verify(workbook, Mockito.times(1)).write(fileOutputStream);
            Mockito.verify(workbook, Mockito.times(1)).close();
            Mockito.verify(runtime, Mockito.times(1)).exec(new String[]{"explorer", fileName});
        }
    }

}