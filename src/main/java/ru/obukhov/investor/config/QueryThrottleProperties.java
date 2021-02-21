package ru.obukhov.investor.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "query.throttle")
@Validated
public class QueryThrottleProperties {

    @NotNull
    private Long interval;

    private List<UrlLimit> limits;

}