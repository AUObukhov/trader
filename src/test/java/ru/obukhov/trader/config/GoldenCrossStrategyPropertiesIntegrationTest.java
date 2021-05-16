package ru.obukhov.trader.config;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import ru.obukhov.trader.test.utils.AssertUtils;

import java.util.Set;

class GoldenCrossStrategyPropertiesIntegrationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(EnableConfigurationPropertiesConfiguration.class)
            .withInitializer(applicationContext -> applicationContext.getEnvironment().setActiveProfiles("test"))
            .withInitializer(new ConfigDataApplicationContextInitializer());

    @Test
    void beanCreated_whenPropertiesFilled() {
        contextRunner.run(context -> {
            Assertions.assertNull(context.getStartupFailure());

            GoldenCrossStrategyProperties goldenCrossStrategyProperties = context.getBean(GoldenCrossStrategyProperties.class);

            Set<GoldenCrossStrategyProperties.StrategyConfig> configs = goldenCrossStrategyProperties.getConfigs();
            AssertUtils.assertEquals(2, configs.size());

            GoldenCrossStrategyProperties.StrategyConfig expectedConfig1 =
                    new GoldenCrossStrategyProperties.StrategyConfig(50, 200, 0.8f);
            Assertions.assertTrue(configs.contains(expectedConfig1));

            GoldenCrossStrategyProperties.StrategyConfig expectedConfig2 =
                    new GoldenCrossStrategyProperties.StrategyConfig(25, 100, 0.7f);
            Assertions.assertTrue(configs.contains(expectedConfig2));
        });
    }

    @Test
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void beanCreationFails_whenNoConfigs() {
        contextRunner.withPropertyValues("golden-cross-strategy.configs:")
                .run(AssertUtils.createContextFailureAssertConsumer("configs must not be empty"));
    }

    @Test
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void beanCreationFails_whenSmallWindowIsZero() {
        contextRunner.withPropertyValues("golden-cross-strategy.configs[0].small-window=0")
                .withPropertyValues("golden-cross-strategy.configs[0].big-window=100")
                .withPropertyValues("golden-cross-strategy.configs[0].index-coefficient=0.5")
                .run(AssertUtils.createContextFailureAssertConsumer("smallWindow must not be lower than 1"));
    }

    @Test
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void beanCreationFails_whenSmallWindowIsNegative() {
        contextRunner.withPropertyValues("golden-cross-strategy.configs[0].small-window=-1")
                .withPropertyValues("golden-cross-strategy.configs[0].big-window=100")
                .withPropertyValues("golden-cross-strategy.configs[0].index-coefficient=0.5")
                .run(AssertUtils.createContextFailureAssertConsumer("smallWindow must not be lower than 1"));
    }

    @Test
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void beanCreationFails_whenSmallWindowIsGreaterThanBigWindow() {
        contextRunner.withPropertyValues("golden-cross-strategy.configs[0].small-window=100")
                .withPropertyValues("golden-cross-strategy.configs[0].big-window=50")
                .withPropertyValues("golden-cross-strategy.configs[0].index-coefficient=0.5")
                .run(AssertUtils.createContextFailureAssertConsumer("smallWindow must not be greater than bigWindow"));
    }

    @Test
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void beanCreationFails_whenIndexCoefficientIsNegative() {
        contextRunner.withPropertyValues("golden-cross-strategy.configs[0].small-window=50")
                .withPropertyValues("golden-cross-strategy.configs[0].big-window=100")
                .withPropertyValues("golden-cross-strategy.configs[0].index-coefficient=-0.5")
                .run(AssertUtils.createContextFailureAssertConsumer("indexCoefficient must be positive"));
    }

    @Test
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void beanCreationFails_whenIndexCoefficientIsZero() {
        contextRunner.withPropertyValues("golden-cross-strategy.configs[0].small-window=50")
                .withPropertyValues("golden-cross-strategy.configs[0].big-window=100")
                .withPropertyValues("golden-cross-strategy.configs[0].index-coefficient=0.0")
                .run(AssertUtils.createContextFailureAssertConsumer("indexCoefficient must be positive"));
    }

    @Test
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void beanCreationFails_whenIndexCoefficientIsGreaterThanOne() {
        contextRunner.withPropertyValues("golden-cross-strategy.configs[0].small-window=50")
                .withPropertyValues("golden-cross-strategy.configs[0].big-window=100")
                .withPropertyValues("golden-cross-strategy.configs[0].index-coefficient=1.1")
                .run(AssertUtils.createContextFailureAssertConsumer("indexCoefficient must not be greater than 1"));
    }

    @EnableConfigurationProperties(GoldenCrossStrategyProperties.class)
    private static class EnableConfigurationPropertiesConfiguration {
    }

}