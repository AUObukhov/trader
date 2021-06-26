package ru.obukhov.trader.common.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.stereotype.Service;
import ru.obukhov.trader.common.service.interfaces.ExcelFileService;
import ru.obukhov.trader.config.properties.ReportProperties;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Service for saving data to excel files
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExcelFileServiceImpl implements ExcelFileService {

    private final ReportProperties reportProperties;

    @Override
    public void saveToFile(final Workbook book, final String fileName) throws IOException {
        final File file = createFile(fileName);
        try (final FileOutputStream outputStream = new FileOutputStream(file)) {
            book.write(outputStream);
            book.close();
        }

        openFile(file);
    }

    private void openFile(final File file) throws IOException {
        final String path = file.getAbsolutePath();
        log.debug("Opening file {}", path);
        Runtime.getRuntime().exec(new String[]{"explorer", path});
    }

    private File createFile(final String fileName) throws IOException {
        final File file = new File(reportProperties.getSaveDirectory(), fileName);
        if (!file.createNewFile()) {
            throw new IllegalStateException("Failed to create file " + file.getAbsolutePath());
        }
        return file;
    }

}