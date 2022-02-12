package ru.obukhov.trader.market.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Objects;

public class SearchMarketInstrument {
    @JsonProperty("figi")
    private String figi = null;

    @JsonProperty("ticker")
    private String ticker = null;

    @JsonProperty("isin")
    private String isin = null;

    @JsonProperty("minPriceIncrement")
    private java.math.BigDecimal minPriceIncrement = null;

    @JsonProperty("lot")
    private Integer lot = null;

    @JsonProperty("currency")
    private Currency currency = null;

    @JsonProperty("name")
    private String name = null;

    @JsonProperty("type")
    private InstrumentType type = null;

    public SearchMarketInstrument figi(String figi) {
        this.figi = figi;
        return this;
    }

    /**
     * Get figi
     *
     * @return figi
     **/
    @Schema(required = true, description = "")
    public String getFigi() {
        return figi;
    }

    public void setFigi(String figi) {
        this.figi = figi;
    }

    public SearchMarketInstrument ticker(String ticker) {
        this.ticker = ticker;
        return this;
    }

    /**
     * Get ticker
     *
     * @return ticker
     **/
    @Schema(required = true, description = "")
    public String getTicker() {
        return ticker;
    }

    public void setTicker(String ticker) {
        this.ticker = ticker;
    }

    public SearchMarketInstrument isin(String isin) {
        this.isin = isin;
        return this;
    }

    /**
     * Get isin
     *
     * @return isin
     **/
    @Schema(description = "")
    public String getIsin() {
        return isin;
    }

    public void setIsin(String isin) {
        this.isin = isin;
    }

    public SearchMarketInstrument minPriceIncrement(java.math.BigDecimal minPriceIncrement) {
        this.minPriceIncrement = minPriceIncrement;
        return this;
    }

    /**
     * Шаг цены
     *
     * @return minPriceIncrement
     **/
    @Schema(description = "Шаг цены")
    public java.math.BigDecimal getMinPriceIncrement() {
        return minPriceIncrement;
    }

    public void setMinPriceIncrement(java.math.BigDecimal minPriceIncrement) {
        this.minPriceIncrement = minPriceIncrement;
    }

    public SearchMarketInstrument lot(Integer lot) {
        this.lot = lot;
        return this;
    }

    /**
     * Get lot
     *
     * @return lot
     **/
    @Schema(required = true, description = "")
    public Integer getLot() {
        return lot;
    }

    public void setLot(Integer lot) {
        this.lot = lot;
    }

    public SearchMarketInstrument currency(Currency currency) {
        this.currency = currency;
        return this;
    }

    /**
     * Get currency
     *
     * @return currency
     **/
    @Schema(description = "")
    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public SearchMarketInstrument name(String name) {
        this.name = name;
        return this;
    }

    /**
     * Get name
     *
     * @return name
     **/
    @Schema(required = true, description = "")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public SearchMarketInstrument type(InstrumentType type) {
        this.type = type;
        return this;
    }

    /**
     * Get type
     *
     * @return type
     **/
    @Schema(required = true, description = "")
    public InstrumentType getType() {
        return type;
    }

    public void setType(InstrumentType type) {
        this.type = type;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SearchMarketInstrument searchMarketInstrument = (SearchMarketInstrument) o;
        return Objects.equals(this.figi, searchMarketInstrument.figi) &&
                Objects.equals(this.ticker, searchMarketInstrument.ticker) &&
                Objects.equals(this.isin, searchMarketInstrument.isin) &&
                Objects.equals(this.minPriceIncrement, searchMarketInstrument.minPriceIncrement) &&
                Objects.equals(this.lot, searchMarketInstrument.lot) &&
                Objects.equals(this.currency, searchMarketInstrument.currency) &&
                Objects.equals(this.name, searchMarketInstrument.name) &&
                Objects.equals(this.type, searchMarketInstrument.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(figi, ticker, isin, minPriceIncrement, lot, currency, name, type);
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class SearchMarketInstrument {\n");

        sb.append("    figi: ").append(toIndentedString(figi)).append("\n");
        sb.append("    ticker: ").append(toIndentedString(ticker)).append("\n");
        sb.append("    isin: ").append(toIndentedString(isin)).append("\n");
        sb.append("    minPriceIncrement: ").append(toIndentedString(minPriceIncrement)).append("\n");
        sb.append("    lot: ").append(toIndentedString(lot)).append("\n");
        sb.append("    currency: ").append(toIndentedString(currency)).append("\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    type: ").append(toIndentedString(type)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }

}
