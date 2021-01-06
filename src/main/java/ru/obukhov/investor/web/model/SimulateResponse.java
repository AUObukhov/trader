package ru.obukhov.investor.web.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Collection;

@Data
@AllArgsConstructor
public class SimulateResponse {

    private Collection<SimulationResult> results;

}