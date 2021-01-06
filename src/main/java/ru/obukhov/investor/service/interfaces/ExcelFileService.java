package ru.obukhov.investor.service.interfaces;

import org.apache.poi.ss.usermodel.Workbook;

public interface ExcelFileService {

    void saveToFile(Workbook book, String fileName);

}