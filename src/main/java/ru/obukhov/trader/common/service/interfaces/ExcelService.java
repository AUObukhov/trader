package ru.obukhov.trader.common.service.interfaces;

import ru.obukhov.trader.web.model.pojo.SimulationResult;

import java.util.Collection;

public interface ExcelService {

    void saveSimulationResults(String ticker, Collection<SimulationResult> results);

}