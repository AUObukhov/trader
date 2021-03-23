package ru.obukhov.trader.web.model.validation.validator;

import org.springframework.util.CollectionUtils;
import ru.obukhov.trader.web.model.pojo.SimulationUnit;
import ru.obukhov.trader.web.model.validation.constraint.SimulationUnitsAreDistinct;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Validates that collection of {@link SimulationUnit} contains unique tickers
 */
public class SimulationUnitsAreDistinctValidator
        implements ConstraintValidator<SimulationUnitsAreDistinct, Collection<SimulationUnit>> {

    @Override
    public boolean isValid(Collection<SimulationUnit> simulationUnits, ConstraintValidatorContext context) {
        if (CollectionUtils.isEmpty(simulationUnits)) {
            return true;
        }

        List<String> uniqueTickers = simulationUnits.stream()
                .map(SimulationUnit::getTicker)
                .distinct()
                .collect(Collectors.toList());
        return simulationUnits.size() == uniqueTickers.size();
    }

}