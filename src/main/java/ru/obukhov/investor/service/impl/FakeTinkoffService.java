package ru.obukhov.investor.service.impl;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.factory.Mappers;
import org.springframework.util.Assert;
import ru.obukhov.investor.bot.interfaces.TinkoffService;
import ru.obukhov.investor.config.TradingProperties;
import ru.obukhov.investor.model.Candle;
import ru.obukhov.investor.model.Interval;
import ru.obukhov.investor.model.transform.OperationMapper;
import ru.obukhov.investor.model.transform.OperationTypeMapper;
import ru.obukhov.investor.service.interfaces.MarketService;
import ru.obukhov.investor.util.DateUtils;
import ru.obukhov.investor.util.MathUtils;
import ru.obukhov.investor.web.model.SimulatedOperation;
import ru.tinkoff.invest.openapi.models.Currency;
import ru.tinkoff.invest.openapi.models.MoneyAmount;
import ru.tinkoff.invest.openapi.models.market.CandleInterval;
import ru.tinkoff.invest.openapi.models.market.Instrument;
import ru.tinkoff.invest.openapi.models.market.Orderbook;
import ru.tinkoff.invest.openapi.models.orders.LimitOrder;
import ru.tinkoff.invest.openapi.models.orders.MarketOrder;
import ru.tinkoff.invest.openapi.models.orders.Order;
import ru.tinkoff.invest.openapi.models.orders.PlacedOrder;
import ru.tinkoff.invest.openapi.models.orders.Status;
import ru.tinkoff.invest.openapi.models.portfolio.InstrumentType;
import ru.tinkoff.invest.openapi.models.portfolio.Portfolio;
import ru.tinkoff.invest.openapi.models.portfolio.PortfolioCurrencies;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Prices are loaded from real market, but any operations do not affect the real portfolio - all data is stored in
 * memory.
 */
public class FakeTinkoffService implements TinkoffService {

    private final TradingProperties tradingProperties;
    private final MarketService marketService;
    private final RealTinkoffService realTinkoffService;
    private final List<SimulatedOperation> operations;
    private final Multimap<String, Portfolio.PortfolioPosition> tickersToPositions;

    private final OperationMapper operationMapper = Mappers.getMapper(OperationMapper.class);
    private final OperationTypeMapper operationTypeMapper = Mappers.getMapper(OperationTypeMapper.class);

    @Getter
    private OffsetDateTime currentDateTime;
    @Getter
    @Setter
    private BigDecimal balance;

    public FakeTinkoffService(TradingProperties tradingProperties,
                              MarketService marketService,
                              RealTinkoffService realTinkoffService) {

        this.tradingProperties = tradingProperties;
        this.marketService = marketService;
        this.realTinkoffService = realTinkoffService;

        this.operations = new ArrayList<>();
        this.tickersToPositions = ArrayListMultimap.create();

    }

    public void clear() {
        this.operations.clear();
        this.tickersToPositions.clear();
        this.currentDateTime = OffsetDateTime.now();
        this.balance = BigDecimal.ZERO;
    }

    /**
     * sets current dateTime, but moves it to nearest work time
     */
    public void initCurrentDateTime(OffsetDateTime currentDateTime) {
        this.currentDateTime = DateUtils.getNearestWorkTime(currentDateTime,
                tradingProperties.getWorkStartTime(),
                tradingProperties.getWorkDuration());
    }

    /**
     * Adds one minute to current dataTime
     */
    public void nextMinute() {

        this.currentDateTime = DateUtils.getNextWorkMinute(
                this.currentDateTime,
                tradingProperties.getWorkStartTime(),
                tradingProperties.getWorkDuration());

    }

    // region MarketContext proxy

    @Override
    public List<Instrument> getMarketStocks() {
        return realTinkoffService.getMarketStocks();
    }

    @Override
    public List<Instrument> getMarketBonds() {
        return realTinkoffService.getMarketBonds();
    }

    @Override
    public List<Instrument> getMarketEtfs() {
        return realTinkoffService.getMarketEtfs();
    }

    @Override
    public List<Instrument> getMarketCurrencies() {
        return realTinkoffService.getMarketCurrencies();
    }

    @Override
    public Orderbook getMarketOrderbook(String ticker, int depth) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Candle> getMarketCandles(String ticker, Interval interval, CandleInterval candleInterval) {
        return realTinkoffService.getMarketCandles(ticker, interval, candleInterval);
    }

    @Override
    public Instrument searchMarketInstrument(String ticker) {
        return realTinkoffService.searchMarketInstrument(ticker);
    }

    // endregion

    // region OperationsContext proxy

    @Override
    public List<ru.tinkoff.invest.openapi.models.operations.Operation> getOperations(Interval interval, String ticker) {
        Stream<SimulatedOperation> operationsStream = operations.stream()
                .filter(operation -> interval.contains(operation.getDateTime()));
        if (ticker != null) {
            operationsStream = operationsStream.filter(operation -> ticker.equals(operation.getTicker()));
        }

        return operationsStream.sorted(Comparator.comparing(SimulatedOperation::getDateTime))
                .map(operationMapper::map)
                .collect(Collectors.toList());
    }

    // endregion

    // region OrdersContext proxy

    @Override
    public List<Order> getOrders() {
        return Collections.emptyList();
    }

    @Override
    public PlacedOrder placeLimitOrder(String ticker, LimitOrder limitOrder) {
        throw new UnsupportedOperationException();
    }

    @Override
    public PlacedOrder placeMarketOrder(String ticker, MarketOrder marketOrder) {
        BigDecimal currentPrice = getCurrentPrice(ticker);
        BigDecimal totalPrice = MathUtils.multiply(currentPrice, marketOrder.lots);
        BigDecimal commissionAmount = MathUtils.getFraction(totalPrice, tradingProperties.getCommission());
        MoneyAmount commission = new MoneyAmount(Currency.RUB, commissionAmount);

        if (marketOrder.operation == ru.tinkoff.invest.openapi.models.orders.Operation.Buy) {
            addPosition(ticker, currentPrice, marketOrder.lots, totalPrice);
            updateBalance(totalPrice.negate(), commissionAmount);
        } else {
            removePositions(ticker, marketOrder.lots);
            updateBalance(totalPrice, commissionAmount);
        }

        addOperation(ticker, currentPrice, commission, marketOrder);
        return createOrder(marketOrder, commission);
    }

    private void addOperation(String ticker,
                              BigDecimal price,
                              MoneyAmount commission,
                              MarketOrder marketOrder) {

        final SimulatedOperation operation = SimulatedOperation.builder()
                .ticker(ticker)
                .price(price)
                .quantity(marketOrder.lots)
                .commission(commission.value)
                .dateTime(this.currentDateTime)
                .operationType(operationTypeMapper.map(marketOrder.operation))
                .build();
        operations.add(operation);
    }

    public BigDecimal getCurrentPrice(String ticker) {
        return marketService.getLastCandle(ticker, currentDateTime).getClosePrice();
    }

    private void addPosition(String ticker, BigDecimal currentPrice, int lots, BigDecimal balance) {
        MoneyAmount averagePositionPrice = new MoneyAmount(Currency.RUB, currentPrice);
        Portfolio.PortfolioPosition position = new Portfolio.PortfolioPosition(StringUtils.EMPTY,
                ticker,
                null,
                InstrumentType.Stock,
                balance,
                null,
                null,
                lots,
                averagePositionPrice,
                null,
                StringUtils.EMPTY);

        this.tickersToPositions.put(ticker, position);
    }

    private void removePositions(String ticker, int count) {
        List<Portfolio.PortfolioPosition> tickerPositions = new ArrayList<>(this.tickersToPositions.get(ticker));
        Assert.isTrue(tickerPositions.size() >= count,
                "marketOrder.lots can't be greater than existing positions count");
        this.tickersToPositions.replaceValues(ticker, tickerPositions.subList(0, tickerPositions.size() - count));
    }

    private void updateBalance(BigDecimal totalPrice, BigDecimal commissionAmount) {
        BigDecimal newBalance = this.balance.add(totalPrice).subtract(commissionAmount);
        Assert.isTrue(newBalance.signum() >= 0, "balance can't be negative");

        this.balance = newBalance;
    }

    private PlacedOrder createOrder(MarketOrder marketOrder, MoneyAmount commission) {
        return new PlacedOrder(StringUtils.EMPTY,
                marketOrder.operation,
                Status.New,
                StringUtils.EMPTY,
                StringUtils.EMPTY,
                marketOrder.lots,
                marketOrder.lots,
                commission);
    }

    @Override
    public void cancelOrder(String orderId) {
        throw new UnsupportedOperationException();
    }

    // endregion

    // region PortfolioContext proxy

    @Override
    public List<Portfolio.PortfolioPosition> getPortfolioPositions() {
        return new ArrayList<>(this.tickersToPositions.values());
    }

    @Override
    public List<PortfolioCurrencies.PortfolioCurrency> getPortfolioCurrencies() {
        PortfolioCurrencies.PortfolioCurrency currency
                = new PortfolioCurrencies.PortfolioCurrency(Currency.RUB, this.balance, null);
        return Collections.singletonList(currency);
    }

    // endregion

}