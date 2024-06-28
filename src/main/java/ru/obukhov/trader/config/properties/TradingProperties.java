package ru.obukhov.trader.config.properties;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;
import ru.obukhov.trader.common.util.DateUtils;

import java.time.OffsetDateTime;

@Data
@ConfigurationProperties(prefix = "trading")
@Validated
@AllArgsConstructor
public class TradingProperties {

    @NotBlank(message = "token is mandatory")
    private final String token;

    private final OffsetDateTime tradesStart = OffsetDateTime.of(1988, 1, 1, 0, 0, 0, 0, DateUtils.DEFAULT_OFFSET);

}