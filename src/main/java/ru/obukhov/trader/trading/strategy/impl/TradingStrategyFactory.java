package ru.obukhov.trader.trading.strategy.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import ru.obukhov.trader.config.properties.TradingProperties;
import ru.obukhov.trader.trading.model.StrategyType;
import ru.obukhov.trader.trading.strategy.interfaces.TradingStrategy;
import ru.obukhov.trader.trading.strategy.model.GoldenCrossStrategyParams;
import ru.obukhov.trader.trading.strategy.model.TradingStrategyParams;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class TradingStrategyFactory {
    private final TradingProperties tradingProperties;

    private final ObjectMapper mapper = new ObjectMapper();
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    public TradingStrategy createStrategy(final StrategyType strategyType, final Map<String, Object> strategyParams) {
        final String strategyName = strategyType.getValue();
        switch (strategyType) {
            case CONSERVATIVE:
                return createConservativeStrategy(strategyName, strategyParams);
            case GOLDEN_CROSS:
                return createGoldenCrossStrategy(strategyName, strategyParams);
            default:
                throw new IllegalArgumentException("Unknown strategy type " + strategyType);
        }
    }

    @NotNull
    private ConservativeStrategy createConservativeStrategy(final String name, final Map<String, Object> strategyParams) {
        final TradingStrategyParams tradingStrategyParams = getStrategyParams(strategyParams, TradingStrategyParams.class);
        return new ConservativeStrategy(name, tradingStrategyParams, tradingProperties);
    }

    private GoldenCrossStrategy createGoldenCrossStrategy(final String name, final Map<String, Object> strategyParams) {
        final GoldenCrossStrategyParams goldenCrossStrategyParams = getStrategyParams(strategyParams, GoldenCrossStrategyParams.class);
        return new GoldenCrossStrategy(name, goldenCrossStrategyParams, tradingProperties);
    }

    private <T extends TradingStrategyParams> T getStrategyParams(final Map<String, Object> params, final Class<T> type) {
        final T strategyParams = mapper.convertValue(params, type);
        validate(strategyParams);
        return strategyParams;
    }

    private void validate(Object object) {
        Set<ConstraintViolation<Object>> violations = validator.validate(object);
        if (!violations.isEmpty()) {
            String message = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining(System.lineSeparator()));
            throw new IllegalArgumentException(message);
        }
    }

}