package ru.obukhov.trader.market.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MarketInstrumentList {
    @JsonProperty("total")
    private BigDecimal total = null;

    @JsonProperty("instruments")
    private List<MarketInstrument> instruments = new ArrayList<>();

    public MarketInstrumentList total(BigDecimal total) {
        this.total = total;
        return this;
    }

    /**
     * Get total
     *
     * @return total
     **/
    @Schema(required = true, description = "")
    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public MarketInstrumentList instruments(List<MarketInstrument> instruments) {
        this.instruments = instruments;
        return this;
    }

    public MarketInstrumentList addInstrumentsItem(MarketInstrument instrumentsItem) {
        this.instruments.add(instrumentsItem);
        return this;
    }

    /**
     * Get instruments
     *
     * @return instruments
     **/
    @Schema(required = true, description = "")
    public List<MarketInstrument> getInstruments() {
        return instruments;
    }

    public void setInstruments(List<MarketInstrument> instruments) {
        this.instruments = instruments;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MarketInstrumentList marketInstrumentList = (MarketInstrumentList) o;
        return Objects.equals(this.total, marketInstrumentList.total) &&
                Objects.equals(this.instruments, marketInstrumentList.instruments);
    }

    @Override
    public int hashCode() {
        return Objects.hash(total, instruments);
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class MarketInstrumentList {\n");

        sb.append("    total: ").append(toIndentedString(total)).append("\n");
        sb.append("    instruments: ").append(toIndentedString(instruments)).append("\n");
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
