package ru.obukhov.trader.trading.strategy.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.obukhov.trader.config.TradingProperties;
import ru.obukhov.trader.trading.model.StrategyConfig;
import ru.obukhov.trader.trading.strategy.interfaces.TradingStrategy;

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
        switch (strategyConfig.getType()) {
            case CONSERVATIVE:
                return new ConservativeStrategy(strategyConfig.getMinimumProfit(), tradingProperties);
            case SIMPLE_GOLDEN_CROSS:
                return createSimpleGoldenCrossStrategy(strategyConfig);
            case LINEAR_GOLDEN_CROSS:
                return createLinearGoldenCrossStrategy(strategyConfig);
            default:
                throw new IllegalArgumentException("Unknown strategy type " + strategyConfig.getType());
        }
    }

    private SimpleGoldenCrossStrategy createSimpleGoldenCrossStrategy(StrategyConfig strategyConfig) {
        final Map<String, Object> params = strategyConfig.getParams();
        final SimpleGoldenCrossStrategyParams strategyParams =
                mapper.convertValue(params, SimpleGoldenCrossStrategyParams.class);
        validate(strategyParams);

        return new SimpleGoldenCrossStrategy(
                strategyConfig.getMinimumProfit(),
                tradingProperties,
                strategyParams
        );
    }

    private LinearGoldenCrossStrategy createLinearGoldenCrossStrategy(StrategyConfig strategyConfig) {
        final Map<String, Object> params = strategyConfig.getParams();
        final LinearGoldenCrossStrategyParams strategyParams =
                mapper.convertValue(params, LinearGoldenCrossStrategyParams.class);
        validate(strategyParams);

        return new LinearGoldenCrossStrategy(
                strategyConfig.getMinimumProfit(),
                tradingProperties,
                strategyParams
        );
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