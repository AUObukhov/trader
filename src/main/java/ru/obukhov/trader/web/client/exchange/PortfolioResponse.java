package ru.obukhov.trader.web.client.exchange;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import ru.obukhov.trader.market.model.Portfolio;

import java.util.Objects;

public class PortfolioResponse {
    @JsonProperty("trackingId")
    private String trackingId = null;

    @JsonProperty("status")
    private String status = "Ok";

    @JsonProperty("payload")
    private Portfolio payload = null;

    public PortfolioResponse trackingId(String trackingId) {
        this.trackingId = trackingId;
        return this;
    }

    /**
     * Get trackingId
     *
     * @return trackingId
     **/
    @Schema(required = true, description = "")
    public String getTrackingId() {
        return trackingId;
    }

    public void setTrackingId(String trackingId) {
        this.trackingId = trackingId;
    }

    public PortfolioResponse status(String status) {
        this.status = status;
        return this;
    }

    /**
     * Get status
     *
     * @return status
     **/
    @Schema(required = true, description = "")
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public PortfolioResponse payload(Portfolio payload) {
        this.payload = payload;
        return this;
    }

    /**
     * Get payload
     *
     * @return payload
     **/
    @Schema(required = true, description = "")
    public Portfolio getPayload() {
        return payload;
    }

    public void setPayload(Portfolio payload) {
        this.payload = payload;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PortfolioResponse portfolioResponse = (PortfolioResponse) o;
        return Objects.equals(this.trackingId, portfolioResponse.trackingId) &&
                Objects.equals(this.status, portfolioResponse.status) &&
                Objects.equals(this.payload, portfolioResponse.payload);
    }

    @Override
    public int hashCode() {
        return Objects.hash(trackingId, status, payload);
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class PortfolioResponse {\n");

        sb.append("    trackingId: ").append(toIndentedString(trackingId)).append("\n");
        sb.append("    status: ").append(toIndentedString(status)).append("\n");
        sb.append("    payload: ").append(toIndentedString(payload)).append("\n");
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
