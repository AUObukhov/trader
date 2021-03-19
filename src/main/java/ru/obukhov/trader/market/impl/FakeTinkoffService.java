package ru.obukhov.trader.market.impl;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.mapstruct.factory.Mappers;
import org.springframework.util.Assert;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.common.util.DateUtils;
import ru.obukhov.trader.common.util.MathUtils;
import ru.obukhov.trader.config.TradingProperties;
import ru.obukhov.trader.market.interfaces.MarketService;
import ru.obukhov.trader.market.interfaces.TinkoffService;
import ru.obukhov.trader.market.model.Candle;
import ru.obukhov.trader.market.model.FakeContext;
import ru.obukhov.trader.market.model.MoneyAmount;
import ru.obukhov.trader.market.model.PortfolioPosition;
import ru.obukhov.trader.market.model.transform.MoneyAmountMapper;
import ru.obukhov.trader.market.model.transform.OperationMapper;
import ru.obukhov.trader.market.model.transform.OperationTypeMapper;
import ru.obukhov.trader.web.model.pojo.SimulatedOperation;
import ru.tinkoff.invest.openapi.models.Currency;
import ru.tinkoff.invest.openapi.models.market.CandleInterval;
import ru.tinkoff.invest.openapi.models.market.Instrument;
import ru.tinkoff.invest.openapi.models.market.Orderbook;
import ru.tinkoff.invest.openapi.models.operations.Operation;
import ru.tinkoff.invest.openapi.models.orders.LimitOrder;
import ru.tinkoff.invest.openapi.models.orders.MarketOrder;
import ru.tinkoff.invest.openapi.models.orders.Order;
import ru.tinkoff.invest.openapi.models.orders.PlacedOrder;
import ru.tinkoff.invest.openapi.models.orders.Status;
import ru.tinkoff.invest.openapi.models.portfolio.PortfolioCurrencies;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.SortedMap;
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

    private final OperationMapper operationMapper = Mappers.getMapper(OperationMapper.class);
    private final OperationTypeMapper operationTypeMapper = Mappers.getMapper(OperationTypeMapper.class);
    private final MoneyAmountMapper moneyAmountMapper = Mappers.getMapper(MoneyAmountMapper.class);

    private FakeContext fakeContext;

    public FakeTinkoffService(TradingProperties tradingProperties,
                              MarketService marketService,
                              RealTinkoffService realTinkoffService) {

        this.tradingProperties = tradingProperties;
        this.marketService = marketService;
        this.realTinkoffService = realTinkoffService;

    }

    /**
     * sets current dateTime, but moves it to nearest work time
     */
    public void init(OffsetDateTime currentDateTime, BigDecimal balance) {
        OffsetDateTime shiftedCurrentDateTime = DateUtils.getNearestWorkTime(currentDateTime,
                tradingProperties.getWorkStartTime(),
                tradingProperties.getWorkDuration());

        this.fakeContext = new FakeContext(shiftedCurrentDateTime, balance);
    }

    /**
     * changes currentDateTime to nearest work time after it
     *
     * @return new value of currentDateTime
     */
    public OffsetDateTime nextMinute() {

        final OffsetDateTime nextWorkMinute = DateUtils.getNextWorkMinute(
                fakeContext.getCurrentDateTime(),
                tradingProperties.getWorkStartTime(),
                tradingProperties.getWorkDuration());
        fakeContext.setCurrentDateTime(nextWorkMinute);

        return nextWorkMinute;

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

    /**
     * Searches operations which satisfy given conditions
     *
     * @param interval interval of dateTime of searchable operations, including extreme values
     * @param ticker   ticker of searchable operations, if operations with any ticker are satisfying
     * @return found operations in natural order of their dateTime
     */
    @Override
    public List<Operation> getOperations(Interval interval, @Nullable String ticker) {
        Stream<SimulatedOperation> operationsStream = fakeContext.getOperations().stream()
                .filter(operation -> interval.contains(operation.getDateTime()));
        if (ticker != null) {
            operationsStream = operationsStream.filter(operation -> ticker.equals(operation.getTicker()));
        }

        return operationsStream
                .sorted(Comparator.comparing(SimulatedOperation::getDateTime))
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
        throw new UnsupportedOperationException("Only market orders supported in simulation");
    }

    /**
     * Performs market order with fake portfolio
     *
     * @param ticker      ticker of executed order
     * @param marketOrder model of executed order
     * @return result of order execution
     */
    @Override
    public PlacedOrder placeMarketOrder(String ticker, MarketOrder marketOrder) {
        BigDecimal currentPrice = getCurrentPrice(ticker);
        BigDecimal totalPrice = MathUtils.multiply(currentPrice, marketOrder.lots);
        BigDecimal commissionAmount = MathUtils.getFraction(totalPrice, tradingProperties.getCommission());
        MoneyAmount commission = new MoneyAmount(Currency.RUB, commissionAmount);

        if (marketOrder.operation == ru.tinkoff.invest.openapi.models.orders.Operation.Buy) {
            buyPosition(ticker, currentPrice, marketOrder.lots, totalPrice, commissionAmount);
        } else {
            sellPosition(ticker, marketOrder.lots, totalPrice, commissionAmount);
        }

        addOperation(ticker, currentPrice, commission, marketOrder);
        return createOrder(marketOrder, commission);
    }

    private BigDecimal getCurrentPrice(String ticker) {
        return marketService.getLastCandle(ticker, fakeContext.getCurrentDateTime()).getOpenPrice();
    }

    private void buyPosition(String ticker,
                             BigDecimal currentPrice,
                             int lotsCount,
                             BigDecimal totalPrice,
                             BigDecimal commissionAmount) {

        updateBalance(totalPrice.negate().subtract(commissionAmount));
        Instrument instrument = realTinkoffService.searchMarketInstrument(ticker);
        PortfolioPosition existingPosition = fakeContext.getPosition(ticker);
        PortfolioPosition position;
        if (existingPosition == null) {
            position = createNewPosition(ticker, totalPrice, instrument.currency, currentPrice, lotsCount);
        } else {
            position = addValuesToPosition(existingPosition, lotsCount, totalPrice);
        }

        fakeContext.addPosition(ticker, position);
    }

    private PortfolioPosition createNewPosition(String ticker,
                                                BigDecimal balance,
                                                Currency currency,
                                                BigDecimal averagePositionPrice,
                                                int lotsCount) {
        return new PortfolioPosition(
                ticker,
                balance,
                null,
                currency,
                null,
                lotsCount,
                averagePositionPrice,
                null,
                StringUtils.EMPTY);
    }

    private PortfolioPosition addValuesToPosition(PortfolioPosition existingPosition,
                                                  int lotsCount,
                                                  BigDecimal totalPrice) {
        BigDecimal newBalance = existingPosition.getBalance().add(totalPrice);
        int newLotsCount = existingPosition.getLotsCount() + lotsCount;
        BigDecimal newAveragePrice = MathUtils.divide(newBalance, newLotsCount);
        return clonePositionWithNewValues(existingPosition, newBalance, newLotsCount, newAveragePrice);
    }

    private PortfolioPosition clonePositionWithNewValues(PortfolioPosition position,
                                                         BigDecimal balance,
                                                         int lotsCount,
                                                         BigDecimal averagePositionPrice) {
        return new PortfolioPosition(
                position.getTicker(),
                balance,
                position.getBlocked(),
                position.getCurrency(),
                position.getExpectedYield(),
                lotsCount,
                averagePositionPrice,
                position.getAveragePositionPriceNoNkd(),
                position.getName());
    }

    private void updateBalance(BigDecimal increment) {
        BigDecimal newBalance = fakeContext.getCurrentBalance().add(increment);
        Assert.isTrue(newBalance.signum() >= 0, "balance can't be negative");

        fakeContext.setCurrentBalance(newBalance);
    }

    private void sellPosition(String ticker, int lotsCount, BigDecimal totalPrice, BigDecimal commissionAmount) {
        PortfolioPosition existingPosition = fakeContext.getPosition(ticker);
        int newLotsCount = existingPosition.getLotsCount() - lotsCount;
        if (newLotsCount < 0) {
            final String message = "lotsCount " + lotsCount + " can't be greater than existing position lots count "
                    + existingPosition.getLotsCount();
            throw new IllegalArgumentException(message);
        }

        updateBalance(totalPrice.subtract(commissionAmount));
        if (newLotsCount == 0) {
            fakeContext.removePosition(ticker);
        } else {
            PortfolioPosition newPosition = clonePositionWithNewLotsCount(existingPosition, newLotsCount);
            fakeContext.addPosition(ticker, newPosition);
        }

    }

    private PortfolioPosition clonePositionWithNewLotsCount(PortfolioPosition position, int lotsCount) {
        return new PortfolioPosition(
                position.getTicker(),
                position.getBalance(),
                position.getBlocked(),
                position.getCurrency(),
                position.getExpectedYield(),
                lotsCount,
                position.getAveragePositionPrice(),
                position.getAveragePositionPriceNoNkd(),
                position.getName());
    }

    private void addOperation(String ticker,
                              BigDecimal price,
                              MoneyAmount commission,
                              MarketOrder marketOrder) {

        final SimulatedOperation operation = SimulatedOperation.builder()
                .ticker(ticker)
                .price(price)
                .quantity(marketOrder.lots)
                .commission(commission.getValue())
                .dateTime(fakeContext.getCurrentDateTime())
                .operationType(operationTypeMapper.map(marketOrder.operation))
                .build();
        fakeContext.addOperation(operation);
    }

    private PlacedOrder createOrder(MarketOrder marketOrder, MoneyAmount commission) {
        return new PlacedOrder(StringUtils.EMPTY,
                marketOrder.operation,
                Status.New,
                StringUtils.EMPTY,
                StringUtils.EMPTY,
                marketOrder.lots,
                marketOrder.lots,
                moneyAmountMapper.map(commission));
    }

    @Override
    public void cancelOrder(String orderId) {
        throw new UnsupportedOperationException();
    }

    // endregion

    // region PortfolioContext proxy

    @Override
    public Collection<PortfolioPosition> getPortfolioPositions() {
        return fakeContext.getPositions();
    }

    @Override
    public List<PortfolioCurrencies.PortfolioCurrency> getPortfolioCurrencies() {
        PortfolioCurrencies.PortfolioCurrency currency
                = new PortfolioCurrencies.PortfolioCurrency(Currency.RUB, fakeContext.getCurrentBalance(), null);
        return Collections.singletonList(currency);
    }

    // endregion

    // region methods for simulation

    @Override
    public OffsetDateTime getCurrentDateTime() {
        return fakeContext.getCurrentDateTime();
    }

    public BigDecimal getCurrentBalance() {
        return fakeContext.getCurrentBalance();
    }

    public SortedMap<OffsetDateTime, BigDecimal> getInvestments() {
        return fakeContext.getInvestments();
    }

    public void incrementBalance(BigDecimal increment) {
        fakeContext.addInvestment(increment);
    }

    // endregion
}