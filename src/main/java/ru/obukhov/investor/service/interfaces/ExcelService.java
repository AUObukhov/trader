package ru.obukhov.investor.service.interfaces;

import ru.obukhov.investor.web.model.SimulationResult;

public interface ExcelService {
    void saveSimulationResult(SimulationResult result);
}