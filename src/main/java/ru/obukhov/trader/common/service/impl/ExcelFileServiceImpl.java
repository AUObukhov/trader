package ru.obukhov.trader.common.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
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

    private File createFile(final String initialFileName) throws IOException {
        final File file = getFile(initialFileName);
        if (!file.createNewFile()) {
            throw new IllegalStateException("Failed to create file " + file.getAbsolutePath());
        }
        return file;
    }

    private File getFile(final String initialFileName) {
        int count = 0;
        String fileName = initialFileName;
        File file = new File(reportProperties.getSaveDirectory(), fileName);
        final Pair<String, String> fileNameParts = splitFileName(initialFileName);
        while (file.exists()) {
            count++;
            fileName = fileNameParts.getLeft() + " (" + count + ")" + fileNameParts.getRight();
            file = new File(reportProperties.getSaveDirectory(), fileName);
        }
        return file;
    }

    private Pair<String, String> splitFileName(final String fileName) {
        final int dotIndex = fileName.lastIndexOf(".");
        return dotIndex == -1
                ? Pair.of(fileName, StringUtils.EMPTY)
                : Pair.of(fileName.substring(0, dotIndex), fileName.substring(dotIndex));
    }

}