package ru.obukhov.investor.model.transform;

import org.mapstruct.Mapper;
import ru.obukhov.investor.web.model.SimulateResponse;
import ru.obukhov.investor.web.model.SimulationResult;

/**
 * Maps {@link SimulationResult} to {@link SimulateResponse}
 */
@Mapper
public interface SimulationResultMapper {

    SimulateResponse map(SimulationResult source);

}