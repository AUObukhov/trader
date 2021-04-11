package ru.obukhov.trader.common.service.interfaces;

import org.apache.poi.ss.usermodel.Workbook;

import java.io.File;

public interface ExcelFileService {

    File saveToFile(Workbook book, String fileName);

}