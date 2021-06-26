package ru.obukhov.trader.web.model.exchange;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.obukhov.trader.web.model.SimulationResult;

import java.util.List;

@Data
@AllArgsConstructor
public class SimulateResponse {

    private List<SimulationResult> results;

}