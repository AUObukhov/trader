package ru.obukhov.trader.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;
import ru.obukhov.trader.common.util.ThrottledCounter;
import ru.obukhov.trader.config.model.UrlLimit;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@Data
@ConfigurationProperties(prefix = "query.throttle")
@Validated
public class QueryThrottleProperties {

    /**
     * Delay before decrement after increment of counter (in milliseconds).
     * To put into {@link ThrottledCounter} constructor
     */
    @Min(value = 1L, message = "interval must be positive")
    private long interval;

    /**
     * containing URLs segments and query limits per {@link QueryThrottleProperties#interval} for corresponding URLs
     */
    @NotNull(message = "limits must not be null")
    @Size(min = 1, message = "limits must not be empty")
    private List<UrlLimit> limits;

    /**
     * Interval between reattempts to perform request (in milliseconds)
     */
    @Min(value = 1L, message = "retryInterval must be positive")
    private long retryInterval;

    /**
     * Limit of attempts to perform request
     */
    @Min(value = 1L, message = "attemptsCount must be positive")
    private int attemptsCount;

    @Min(value = 1L, message = "defaultLimit must be positive")
    private int defaultLimit;

}