package ru.obukhov.investor.common.service.interfaces;

import ru.obukhov.investor.web.model.pojo.SimulationResult;

import java.util.Collection;

public interface ExcelService {

    void saveSimulationResults(String ticker, Collection<SimulationResult> results);

}