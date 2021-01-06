package ru.obukhov.investor.service.impl;

import lombok.SneakyThrows;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.stereotype.Service;
import ru.obukhov.investor.service.interfaces.ExcelFileService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Service for saving data to excel files
 */
@Service
public class ExcelFileServiceImpl implements ExcelFileService {

    private static final String USER_HOME_FOLDER = System.getProperty("user.home");
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-dd-M--HH-mm-ss");

    @Override
    @SneakyThrows
    public void saveToFile(Workbook book, String fileName) {
        try (FileOutputStream outputStream = createFileOutputStream(fileName)) {
            book.write(outputStream);
            book.close();
        }
    }

    private FileOutputStream createFileOutputStream(String fileName) throws IOException {
        String extendedFileName = getFileName(fileName);
        final File file = new File(USER_HOME_FOLDER, extendedFileName);
        if (!file.createNewFile()) {
            throw new IllegalStateException("Failed to create file " + USER_HOME_FOLDER + "\\" + extendedFileName);
        }

        return new FileOutputStream(file);
    }

    private String getFileName(String fileName) {
        return fileName + " " + LocalDateTime.now().format(FORMATTER) + ".xlsx";
    }

}