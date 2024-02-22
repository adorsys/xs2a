/*
 * Copyright 2018-2024 adorsys GmbH & Co KG
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version. This program is distributed in the hope that
 * it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 *
 * This project is also available under a separate commercial license. You can
 * contact us at sales@adorsys.com.
 */

package de.adorsys.psd2.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Map;
import java.util.Objects;

/**
 * JSON based card account report.  This card account report contains transactions resulting from the query parameters.
 */
@Schema(description = "JSON based card account report.  This card account report contains transactions resulting from the query parameters. ")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2022-10-26T13:16:54.081225+03:00[Europe/Kiev]")


public class CardAccountReport   {
  @JsonProperty("booked")
  private CardTransactionList booked = null;

  @JsonProperty("pending")
  private CardTransactionList pending = null;

  @JsonProperty("_links")
  private Map _links = null;

  public CardAccountReport booked(CardTransactionList booked) {
    this.booked = booked;
    return this;
  }

    /**
     * Get booked
     *
     * @return booked
     **/
    @Schema(description = "")
    @JsonProperty("booked")

    @Valid
    public CardTransactionList getBooked() {
        return booked;
    }

  public void setBooked(CardTransactionList booked) {
    this.booked = booked;
  }

  public CardAccountReport pending(CardTransactionList pending) {
    this.pending = pending;
    return this;
  }

    /**
     * Get pending
     *
     * @return pending
     **/
    @Schema(description = "")
    @JsonProperty("pending")

    @Valid
    public CardTransactionList getPending() {
        return pending;
  }

  public void setPending(CardTransactionList pending) {
    this.pending = pending;
  }

    public CardAccountReport _links(Map _links) {
        this._links = _links;
        return this;
    }

    /**
     * Get _links
     *
     * @return _links
     **/
    @Schema(required = true, description = "")
    @JsonProperty("_links")
    @NotNull

    @Valid
    public Map getLinks() {
    return _links;
  }

  public void setLinks(Map _links) {
    this._links = _links;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CardAccountReport cardAccountReport = (CardAccountReport) o;
    return Objects.equals(this.booked, cardAccountReport.booked) &&
        Objects.equals(this.pending, cardAccountReport.pending) &&
        Objects.equals(this._links, cardAccountReport._links);
  }

  @Override
  public int hashCode() {
    return Objects.hash(booked, pending, _links);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class CardAccountReport {\n");

    sb.append("    booked: ").append(toIndentedString(booked)).append("\n");
    sb.append("    pending: ").append(toIndentedString(pending)).append("\n");
    sb.append("    _links: ").append(toIndentedString(_links)).append("\n");
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
