package ru.obukhov.investor.service.interfaces;

import ru.obukhov.investor.web.model.SimulationResult;

import java.util.Collection;

public interface ExcelService {

    void saveSimulationResults(Collection<SimulationResult> results);

}