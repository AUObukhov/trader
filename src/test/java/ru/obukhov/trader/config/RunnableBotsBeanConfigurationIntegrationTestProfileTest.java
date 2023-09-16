package ru.obukhov.trader.config;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.test.context.ActiveProfiles;
import ru.obukhov.trader.IntegrationTest;
import ru.obukhov.trader.trading.bots.RunnableBot;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ScheduledThreadPoolExecutor;

@SpringBootTest
@ActiveProfiles("test")
class RunnableBotsBeanConfigurationIntegrationTestProfileTest extends IntegrationTest {

    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private ThreadPoolTaskScheduler taskScheduler;

    @Test
    void testScheduledBotsCreation() {
        final Map<String, RunnableBot> scheduledBots = applicationContext.getBeansOfType(RunnableBot.class);

        Assertions.assertEquals(2, scheduledBots.size());
        Assertions.assertNotNull(scheduledBots.get("scheduledBot1"));
        Assertions.assertNotNull(scheduledBots.get("scheduledBot2"));
    }

    @Test
    void testEmptyScheduledQueueCreation() {
        final ScheduledThreadPoolExecutor scheduledExecutor = (ScheduledThreadPoolExecutor) taskScheduler.getScheduledExecutor();
        final Queue<Runnable> queue = scheduledExecutor.getQueue();
        Assertions.assertTrue(queue.isEmpty());
    }

}