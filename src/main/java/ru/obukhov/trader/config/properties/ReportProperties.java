package ru.obukhov.trader.config.properties;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

@ConfigurationProperties(prefix = "report")
public class ReportProperties {

    @Getter
    private final String saveDirectory;

//    @ConstructorBinding
public ReportProperties(final String saveDirectory) {
    this.saveDirectory = !StringUtils.hasLength(saveDirectory)
            ? System.getProperty("user.home")
            : saveDirectory;
}

}