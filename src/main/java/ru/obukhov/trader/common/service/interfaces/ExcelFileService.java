package ru.obukhov.trader.common.service.interfaces;

import org.apache.poi.ss.usermodel.Workbook;

import java.io.IOException;

public interface ExcelFileService {

    void saveToFile(Workbook book, String fileName) throws IOException;

}