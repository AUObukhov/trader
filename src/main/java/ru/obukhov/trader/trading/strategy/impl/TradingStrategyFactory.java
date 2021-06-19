package ru.obukhov.trader.trading.strategy.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import ru.obukhov.trader.config.TradingProperties;
import ru.obukhov.trader.trading.model.StrategyConfig;
import ru.obukhov.trader.trading.strategy.interfaces.TradingStrategy;
import ru.obukhov.trader.trading.strategy.model.ExponentialGoldenCrossStrategyParams;
import ru.obukhov.trader.trading.strategy.model.LinearGoldenCrossStrategyParams;
import ru.obukhov.trader.trading.strategy.model.SimpleGoldenCrossStrategyParams;
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

    public TradingStrategy createStrategy(final StrategyConfig strategyConfig) {
        switch (strategyConfig.getStrategyType()) {
            case CONSERVATIVE:
                return createConservativeStrategy(strategyConfig);
            case SIMPLE_GOLDEN_CROSS:
                return createSimpleGoldenCrossStrategy(strategyConfig);
            case LINEAR_GOLDEN_CROSS:
                return createLinearGoldenCrossStrategy(strategyConfig);
            case EXPONENTIAL_GOLDEN_CROSS:
                return createExponentialGoldenCrossStrategy(strategyConfig);
            default:
                throw new IllegalArgumentException("Unknown strategy type " + strategyConfig.getStrategyType());
        }
    }

    @NotNull
    private ConservativeStrategy createConservativeStrategy(StrategyConfig strategyConfig) {
        final TradingStrategyParams strategyParams = getStrategyParams(strategyConfig, TradingStrategyParams.class);

        return new ConservativeStrategy(strategyParams, tradingProperties);
    }

    private SimpleGoldenCrossStrategy createSimpleGoldenCrossStrategy(StrategyConfig strategyConfig) {
        final SimpleGoldenCrossStrategyParams strategyParams =
                getStrategyParams(strategyConfig, SimpleGoldenCrossStrategyParams.class);

        return new SimpleGoldenCrossStrategy(strategyParams, tradingProperties);
    }

    private LinearGoldenCrossStrategy createLinearGoldenCrossStrategy(StrategyConfig strategyConfig) {
        final LinearGoldenCrossStrategyParams strategyParams =
                getStrategyParams(strategyConfig, LinearGoldenCrossStrategyParams.class);

        return new LinearGoldenCrossStrategy(strategyParams, tradingProperties);
    }

    private ExponentialGoldenCrossStrategy createExponentialGoldenCrossStrategy(StrategyConfig strategyConfig) {
        final ExponentialGoldenCrossStrategyParams strategyParams =
                getStrategyParams(strategyConfig, ExponentialGoldenCrossStrategyParams.class);

        return new ExponentialGoldenCrossStrategy(strategyParams, tradingProperties);
    }

    private <T extends TradingStrategyParams> T getStrategyParams(StrategyConfig strategyConfig, Class<T> type) {
        final Map<String, Object> params = strategyConfig.getStrategyParams();
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