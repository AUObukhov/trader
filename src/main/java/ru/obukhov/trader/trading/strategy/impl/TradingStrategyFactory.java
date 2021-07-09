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
        switch (strategyType) {
            case CONSERVATIVE:
                return createConservativeStrategy(strategyParams);
            case SIMPLE_GOLDEN_CROSS:
                return createSimpleGoldenCrossStrategy(strategyParams);
            case LINEAR_GOLDEN_CROSS:
                return createLinearGoldenCrossStrategy(strategyParams);
            case EXPONENTIAL_GOLDEN_CROSS:
                return createExponentialGoldenCrossStrategy(strategyParams);
            default:
                throw new IllegalArgumentException("Unknown strategy type " + strategyType);
        }
    }

    @NotNull
    private ConservativeStrategy createConservativeStrategy(final Map<String, Object> params) {
        final TradingStrategyParams strategyParams = getStrategyParams(params, TradingStrategyParams.class);

        return new ConservativeStrategy(strategyParams, tradingProperties);
    }

    private SimpleGoldenCrossStrategy createSimpleGoldenCrossStrategy(final Map<String, Object> params) {
        final GoldenCrossStrategyParams strategyParams = getStrategyParams(params, GoldenCrossStrategyParams.class);

        return new SimpleGoldenCrossStrategy(strategyParams, tradingProperties);
    }

    private LinearGoldenCrossStrategy createLinearGoldenCrossStrategy(final Map<String, Object> params) {
        final GoldenCrossStrategyParams strategyParams = getStrategyParams(params, GoldenCrossStrategyParams.class);

        return new LinearGoldenCrossStrategy(strategyParams, tradingProperties);
    }

    private ExponentialGoldenCrossStrategy createExponentialGoldenCrossStrategy(final Map<String, Object> params) {
        final GoldenCrossStrategyParams strategyParams = getStrategyParams(params, GoldenCrossStrategyParams.class);

        return new ExponentialGoldenCrossStrategy(strategyParams, tradingProperties);
    }

    private <T extends TradingStrategyParams> T getStrategyParams(
            final Map<String, Object> params,
            final Class<T> type
    ) {
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