package ru.obukhov.trader.config;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.util.StringUtils;

@ConstructorBinding
@ConfigurationProperties(prefix = "report")
public class ReportProperties {

    @Getter
    private final String saveDirectory;

    public ReportProperties(final String saveDirectory) {
        this.saveDirectory = !StringUtils.hasLength(saveDirectory)
                ? System.getProperty("user.home")
                : saveDirectory;
    }

}