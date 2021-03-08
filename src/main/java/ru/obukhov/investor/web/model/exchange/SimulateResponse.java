package ru.obukhov.investor.web.model.exchange;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.obukhov.investor.web.model.pojo.SimulationResult;

import java.util.Collection;

@Data
@AllArgsConstructor
public class SimulateResponse {

    private Collection<SimulationResult> results;

}