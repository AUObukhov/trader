package ru.obukhov.trader.config.properties;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class ReportPropertiesContextTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(ReportPropertiesContextTest.EnableConfigurationPropertiesConfiguration.class);

    @Test
    void saveDirectoryRead_whenSaveDirectoryFilled() {
        this.contextRunner
                .withInitializer(applicationContext -> applicationContext.getEnvironment().setActiveProfiles("test"))
                .withInitializer(new ConfigDataApplicationContextInitializer())
                .run(context -> {
                    Assertions.assertNull(context.getStartupFailure());

                    final ReportProperties reportProperties = context.getBean(ReportProperties.class);

                    Assertions.assertEquals("D:\\test", reportProperties.getSaveDirectory());
                });
    }

    @Test
    void saveDirectorySetDefault_whenSaveDirectoryNotFilled() {
        this.contextRunner
                .run(context -> {
                    Assertions.assertNull(context.getStartupFailure());

                    final ReportProperties reportProperties = context.getBean(ReportProperties.class);

                    Assertions.assertEquals(System.getProperty("user.home"), reportProperties.getSaveDirectory());
                });
    }

    @EnableConfigurationProperties(ReportProperties.class)
    private static class EnableConfigurationPropertiesConfiguration {
    }

}