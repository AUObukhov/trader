package ru.obukhov.investor.service.impl;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.factory.Mappers;
import org.springframework.util.Assert;
import ru.obukhov.investor.bot.interfaces.TinkoffService;
import ru.obukhov.investor.config.TradingProperties;
import ru.obukhov.investor.model.Candle;
import ru.obukhov.investor.model.transform.OperationTypeMapper;
import ru.obukhov.investor.service.interfaces.MarketService;
import ru.obukhov.investor.util.DateUtils;
import ru.obukhov.investor.util.MathUtils;
import ru.tinkoff.invest.openapi.models.Currency;
import ru.tinkoff.invest.openapi.models.MoneyAmount;
import ru.tinkoff.invest.openapi.models.market.CandleInterval;
import ru.tinkoff.invest.openapi.models.market.Instrument;
import ru.tinkoff.invest.openapi.models.market.Orderbook;
import ru.tinkoff.invest.openapi.models.operations.Operation;
import ru.tinkoff.invest.openapi.models.operations.OperationStatus;
import ru.tinkoff.invest.openapi.models.operations.OperationType;
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
    private final List<Operation> operations;
    private final Multimap<String, Portfolio.PortfolioPosition> tickersToPositions;
    private final OperationTypeMapper operationTypeMapper = Mappers.getMapper(OperationTypeMapper.class);
    @Getter
    private OffsetDateTime currentDateTime;
    @Getter
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

    /**
     * sets current dateTime, but moves it to nearest work time
     */
    public void init(OffsetDateTime currentDateTime, BigDecimal balance) {
        this.operations.clear();
        this.tickersToPositions.clear();

        this.currentDateTime = DateUtils.getNearestWorkTime(currentDateTime,
                tradingProperties.getWorkStartTime(),
                tradingProperties.getWorkDuration());
        this.balance = balance;
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
    public Orderbook getMarketOrderbook(String figi, int depth) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Candle> getMarketCandles(String figi, OffsetDateTime from, OffsetDateTime to, CandleInterval interval) {
        return realTinkoffService.getMarketCandles(figi, from, to, interval);
    }

    @Override
    public Instrument searchMarketInstrumentByTicker(String ticker) {
        return realTinkoffService.searchMarketInstrumentByTicker(ticker);
    }

    @Override
    public Instrument searchMarketInstrumentByFigi(String figi) {
        throw new UnsupportedOperationException();
    }

    // endregion

    // region OperationsContext proxy

    @Override
    public List<Operation> getOperations(OffsetDateTime from, OffsetDateTime to, String figi) {
        Stream<Operation> operationsStream = operations.stream()
                .filter(operation -> DateUtils.isBetween(operation.date, from, to));
        if (figi != null) {
            operationsStream = operationsStream.filter(operation -> figi.equals(operation.figi));
        }

        return operationsStream.sorted(Comparator.comparing(operation -> operation.date))
                .collect(Collectors.toList());
    }

    // endregion

    // region OrdersContext proxy

    @Override
    public List<Order> getOrders() {
        return Collections.emptyList();
    }

    @Override
    public PlacedOrder placeLimitOrder(String figi, LimitOrder limitOrder) {
        throw new UnsupportedOperationException();
    }

    @Override
    public PlacedOrder placeMarketOrder(String figi, MarketOrder marketOrder) {
        BigDecimal currentPrice = getCurrentPrice(figi);
        BigDecimal totalPrice = MathUtils.multiply(currentPrice, marketOrder.lots);
        BigDecimal commissionAmount = MathUtils.getFraction(totalPrice, tradingProperties.getCommission());
        MoneyAmount commission = new MoneyAmount(Currency.RUB, commissionAmount);
        String ticker = realTinkoffService.searchMarketInstrumentByFigi(figi).ticker;

        if (marketOrder.operation == ru.tinkoff.invest.openapi.models.orders.Operation.Buy) {
            addPosition(ticker, currentPrice, marketOrder.lots, totalPrice);
            updateBalance(totalPrice.negate(), commissionAmount);
        } else {
            removePositions(ticker, marketOrder.lots);
            updateBalance(totalPrice, commissionAmount);
        }

        addOperation(figi, marketOrder, totalPrice, currentPrice, commission);
        return createOrder(marketOrder, commission);
    }

    private void addOperation(String figi,
                              MarketOrder marketOrder,
                              BigDecimal totalPrice,
                              BigDecimal currentPrice,
                              MoneyAmount commission) {

        final OperationType operationType = operationTypeMapper.map(marketOrder.operation);
        Operation operation = new Operation(StringUtils.EMPTY,
                OperationStatus.Done,
                null,
                commission,
                Currency.RUB,
                totalPrice,
                currentPrice,
                marketOrder.lots,
                figi,
                InstrumentType.Stock,
                false,
                this.currentDateTime,
                operationType);
        operations.add(operation);
    }

    private BigDecimal getCurrentPrice(String figi) {
        return marketService.getLastCandleByFigi(figi, currentDateTime).getClosePrice();
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