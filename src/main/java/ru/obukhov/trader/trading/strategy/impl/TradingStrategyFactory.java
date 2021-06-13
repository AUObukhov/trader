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
            case GOLDEN_CROSS: {
                final Map<String, Object> params = strategyConfig.getParams();
                final GoldenCrossStrategyParams goldenCrossStrategyParams =
                        mapper.convertValue(params, GoldenCrossStrategyParams.class);
                validate(goldenCrossStrategyParams);

                return new GoldenCrossStrategy(
                        strategyConfig.getMinimumProfit(),
                        tradingProperties,
                        goldenCrossStrategyParams
                );
            }
            default:
                throw new IllegalArgumentException("Unknown strategy type " + strategyConfig.getType());
        }
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