package ru.obukhov.trader.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import ru.obukhov.trader.common.model.transform.BigDecimalDeserializer;
import ru.obukhov.trader.common.model.transform.BigDecimalSerializer;
import ru.obukhov.trader.common.util.DateUtils;
import ru.obukhov.trader.config.properties.TradingProperties;
import ru.tinkoff.piapi.core.InstrumentsService;
import ru.tinkoff.piapi.core.InvestApi;
import ru.tinkoff.piapi.core.MarketDataService;
import ru.tinkoff.piapi.core.OperationsService;
import ru.tinkoff.piapi.core.OrdersService;
import ru.tinkoff.piapi.core.UsersService;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;

@Configuration
@SuppressWarnings("unused")
public class BeanConfiguration {

    @Bean
    public InvestApi investApi(final TradingProperties tradingProperties) {
        return InvestApi.create(tradingProperties.getToken());
    }

    @Bean
    public InstrumentsService instrumentsService(final InvestApi investApi) {
        return investApi.getInstrumentsService();
    }

    @Bean
    public MarketDataService marketDataService(final InvestApi investApi) {
        return investApi.getMarketDataService();
    }

    @Bean
    public OperationsService operationsService(final InvestApi investApi) {
        return investApi.getOperationsService();
    }

    @Bean
    public OrdersService ordersService(final InvestApi investApi) {
        return investApi.getOrdersService();
    }

    @Bean
    public UsersService usersService(final InvestApi investApi) {
        return investApi.getUserService();
    }

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        final SimpleModule module = new SimpleModule()
                .addSerializer(new BigDecimalSerializer())
                .addDeserializer(BigDecimal.class, new BigDecimalDeserializer());
        return new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .registerModule(module)
                .setDateFormat(new SimpleDateFormat(DateUtils.OFFSET_DATE_TIME_FORMAT))
                .configure(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN, true)
                .configure(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE, false);
    }

}