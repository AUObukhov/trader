package ru.obukhov.investor.web.model.exchange;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.obukhov.investor.web.model.pojo.SimulationResult;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
public class SimulateResponse {

    private Map<String, List<SimulationResult>> results;

}