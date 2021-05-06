package ru.obukhov.trader.common.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.stereotype.Service;
import ru.obukhov.trader.common.service.interfaces.ExcelFileService;
import ru.obukhov.trader.config.ReportProperties;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Service for saving data to excel files
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExcelFileServiceImpl implements ExcelFileService {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd--HH-mm-ss");

    private final ReportProperties reportProperties;

    @Override
    public void saveToFile(Workbook book, String fileName) throws IOException {
        final File file = createFile(fileName);
        try (FileOutputStream outputStream = new FileOutputStream(file)) {
            book.write(outputStream);
            book.close();
        }

        openFile(file);
    }

    private void openFile(File file) throws IOException {
        final String path = file.getAbsolutePath();
        log.debug("Opening file {}", path);
        Runtime.getRuntime().exec(new String[]{"explorer", path});
    }

    private File createFile(String fileName) throws IOException {
        String extendedFileName = getFileName(fileName);
        final File file = new File(reportProperties.getSaveDirectory(), extendedFileName);
        if (!file.createNewFile()) {
            throw new IllegalStateException("Failed to create file " + file.getAbsolutePath());
        }
        return file;
    }

    private String getFileName(String fileName) {
        return fileName + " " + LocalDateTime.now().format(FORMATTER) + ".xlsx";
    }

}