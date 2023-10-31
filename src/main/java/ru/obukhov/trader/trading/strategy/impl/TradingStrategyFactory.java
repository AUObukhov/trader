package ru.obukhov.trader.trading.strategy.impl;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import lombok.AllArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import ru.obukhov.trader.common.service.impl.MovingAverager;
import ru.obukhov.trader.common.util.Asserter;
import ru.obukhov.trader.market.impl.ExtMarketDataService;
import ru.obukhov.trader.market.model.MovingAverageType;
import ru.obukhov.trader.trading.model.CrossStrategyParams;
import ru.obukhov.trader.trading.model.StrategyType;
import ru.obukhov.trader.trading.model.TradingStrategyParams;
import ru.obukhov.trader.web.model.BotConfig;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class TradingStrategyFactory {
    private final ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    private final ApplicationContext applicationContext;
    private final ExtMarketDataService extMarketDataService;

    public AbstractTradingStrategy createStrategy(final BotConfig botConfig) {
        final StrategyType strategyType = botConfig.strategyType();
        return switch (strategyType) {
            case CONSERVATIVE -> new ConservativeStrategy(strategyType.name(), extMarketDataService);
            case CROSS -> createCrossStrategy(strategyType.name(), botConfig.strategyParams());
        };
    }

    private CrossStrategy createCrossStrategy(final String name, final Map<String, Object> strategyParams) {
        final CrossStrategyParams crossStrategyParams = getStrategyParams(strategyParams, CrossStrategyParams.class);
        final MovingAverageType movingAverageType = getMovingAverageType(strategyParams);
        final MovingAverager averager = applicationContext.getBean(movingAverageType.getAveragerName(), MovingAverager.class);
        final String fullName = name + " " + movingAverageType;
        return new CrossStrategy(fullName, crossStrategyParams, extMarketDataService, averager);
    }

    private MovingAverageType getMovingAverageType(final Map<String, Object> strategyParams) {
        final String movingAverageTypeString = (String) strategyParams.get("movingAverageType");
        Assert.notNull(movingAverageTypeString, "movingAverageType is mandatory");

        final MovingAverageType movingAverageType = MovingAverageType.from(movingAverageTypeString);
        Asserter.notNull(movingAverageType, () -> new IllegalArgumentException("MovingAverageType '" + movingAverageTypeString + "' not found"));

        return movingAverageType;
    }

    @SuppressWarnings("SameParameterValue")
    private <T extends TradingStrategyParams> T getStrategyParams(final Map<String, Object> params, final Class<T> type) {
        final T strategyParams = mapper.convertValue(params, type);
        validate(strategyParams);
        return strategyParams;
    }

    private void validate(final Object object) {
        try (final ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory()) {
            final Set<ConstraintViolation<Object>> violations = validatorFactory.getValidator().validate(object);
            if (!violations.isEmpty()) {
                String message = violations.stream()
                        .map(ConstraintViolation::getMessage)
                        .collect(Collectors.joining(System.lineSeparator()));
                throw new IllegalArgumentException(message);
            }
        }
    }

}