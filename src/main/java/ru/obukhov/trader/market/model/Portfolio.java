package ru.obukhov.trader.market.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Portfolio {
  @JsonProperty("positions")
  private List<PortfolioPosition> positions = new ArrayList<>();

  public Portfolio positions(List<PortfolioPosition> positions) {
    this.positions = positions;
    return this;
  }

  public Portfolio addPositionsItem(PortfolioPosition positionsItem) {
    this.positions.add(positionsItem);
    return this;
  }

  /**
   * Get positions
   *
   * @return positions
   **/
  @Schema(required = true, description = "")
  public List<PortfolioPosition> getPositions() {
    return positions;
  }

  public void setPositions(List<PortfolioPosition> positions) {
    this.positions = positions;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Portfolio portfolio = (Portfolio) o;
    return Objects.equals(this.positions, portfolio.positions);
  }

  @Override
  public int hashCode() {
    return Objects.hash(positions);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Portfolio {\n");

    sb.append("    positions: ").append(toIndentedString(positions)).append("\n");
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
