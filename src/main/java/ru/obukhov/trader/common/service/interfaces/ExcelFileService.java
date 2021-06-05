package ru.obukhov.trader.common.service.interfaces;

import org.apache.poi.ss.usermodel.Workbook;

import java.io.IOException;

public interface ExcelFileService {

    void saveToFile(final Workbook book, final String fileName) throws IOException;

}