package ru.obukhov.trader.common.model.transform;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.obukhov.trader.IntegrationTest;
import ru.obukhov.trader.market.model.MovingAverageType;

@SpringBootTest
@AutoConfigureMockMvc
@Import(MovingAverageTypeConverterIntegrationTest.TestController.class)
class MovingAverageTypeConverterIntegrationTest extends IntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void test() throws Exception {
        final MovingAverageType movingAverageType = MovingAverageType.LINEAR_WEIGHTED;
        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post("/trader/test/getMovingAverageTypeName")
                .param("movingAverageType", movingAverageType.getValue())
                .contentType(MediaType.APPLICATION_JSON);
        mockMvc.perform(requestBuilder)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(movingAverageType.name()));
    }

    @Slf4j
    @RestController
    @RequestMapping("trader/test")
    @RequiredArgsConstructor
    @SuppressWarnings("unused")
    static class TestController {
        @PostMapping("/getMovingAverageTypeName")
        public String getMovingAverageTypeName(final MovingAverageType movingAverageType) {
            return movingAverageType.name();
        }
    }
}