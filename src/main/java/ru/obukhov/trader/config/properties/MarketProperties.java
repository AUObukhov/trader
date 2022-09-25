package ru.obukhov.trader.config.properties;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.validation.annotation.Validated;
import ru.obukhov.trader.config.model.WorkSchedule;

import javax.validation.constraints.NotNull;

@Data
@ConstructorBinding
@AllArgsConstructor
@ConfigurationProperties(prefix = "market")
@Validated
public class MarketProperties {

    @NotNull(message = "workSchedule is mandatory")
    private final WorkSchedule workSchedule;

}