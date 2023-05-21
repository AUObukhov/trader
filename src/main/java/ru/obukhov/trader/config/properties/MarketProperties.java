package ru.obukhov.trader.config.properties;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;
import ru.obukhov.trader.config.model.WorkSchedule;

@Data
@AllArgsConstructor
@ConfigurationProperties(prefix = "market")
@Validated
public class MarketProperties {

    @NotNull(message = "workSchedule is mandatory")
    private final WorkSchedule workSchedule;

}