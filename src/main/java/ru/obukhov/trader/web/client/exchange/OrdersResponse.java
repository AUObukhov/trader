package ru.obukhov.trader.web.client.exchange;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import ru.obukhov.trader.market.model.Order;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class OrdersResponse {
    @JsonProperty("trackingId")
    private String trackingId = null;

    @JsonProperty("status")
    private String status = "Ok";

    @JsonProperty("payload")
    private List<Order> payload = new ArrayList<>();

    public OrdersResponse trackingId(String trackingId) {
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

    public OrdersResponse status(String status) {
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

    public OrdersResponse payload(List<Order> payload) {
        this.payload = payload;
        return this;
    }

    public OrdersResponse addPayloadItem(Order payloadItem) {
        this.payload.add(payloadItem);
        return this;
    }

    /**
     * Get payload
     *
     * @return payload
     **/
    @Schema(required = true, description = "")
    public List<Order> getPayload() {
        return payload;
    }

    public void setPayload(List<Order> payload) {
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
        OrdersResponse ordersResponse = (OrdersResponse) o;
        return Objects.equals(this.trackingId, ordersResponse.trackingId) &&
                Objects.equals(this.status, ordersResponse.status) &&
                Objects.equals(this.payload, ordersResponse.payload);
    }

    @Override
    public int hashCode() {
        return Objects.hash(trackingId, status, payload);
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class OrdersResponse {\n");

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
