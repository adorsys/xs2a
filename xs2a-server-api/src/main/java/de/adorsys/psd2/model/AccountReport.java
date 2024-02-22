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
 * JSON based account report. This account report contains transactions resulting from the query parameters.  &#x27;booked&#x27; shall be contained if bookingStatus parameter is set to \&quot;booked\&quot; or \&quot;both\&quot;.  &#x27;pending&#x27; is not contained if the bookingStatus parameter is set to \&quot;booked\&quot; or \&quot;information\&quot;.  &#x27;information&#x27; Only contained if the bookingStatus is set to \&quot;information\&quot; and if supported by ASPSP.
 */
@Schema(description = "JSON based account report. This account report contains transactions resulting from the query parameters.  'booked' shall be contained if bookingStatus parameter is set to \"booked\" or \"both\".  'pending' is not contained if the bookingStatus parameter is set to \"booked\" or \"information\".  'information' Only contained if the bookingStatus is set to \"information\" and if supported by ASPSP. ")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2022-10-26T13:16:54.081225+03:00[Europe/Kiev]")


public class AccountReport   {
  @JsonProperty("booked")
  private TransactionList booked = null;

  @JsonProperty("pending")
  private TransactionList pending = null;

  @JsonProperty("information")
  private TransactionList information = null;

  @JsonProperty("_links")
  private Map _links = null;

  public AccountReport booked(TransactionList booked) {
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
    public TransactionList getBooked() {
        return booked;
    }

  public void setBooked(TransactionList booked) {
    this.booked = booked;
  }

  public AccountReport pending(TransactionList pending) {
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
    public TransactionList getPending() {
        return pending;
  }

  public void setPending(TransactionList pending) {
    this.pending = pending;
  }

  public AccountReport information(TransactionList information) {
    this.information = information;
      return this;
  }

    /**
     * Get information
     *
     * @return information
     **/
    @Schema(description = "")
    @JsonProperty("information")

    @Valid
    public TransactionList getInformation() {
        return information;
  }

  public void setInformation(TransactionList information) {
      this.information = information;
  }

    public AccountReport _links(Map _links) {
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
    AccountReport accountReport = (AccountReport) o;
    return Objects.equals(this.booked, accountReport.booked) &&
        Objects.equals(this.pending, accountReport.pending) &&
        Objects.equals(this.information, accountReport.information) &&
        Objects.equals(this._links, accountReport._links);
  }

  @Override
  public int hashCode() {
    return Objects.hash(booked, pending, information, _links);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AccountReport {\n");

    sb.append("    booked: ").append(toIndentedString(booked)).append("\n");
    sb.append("    pending: ").append(toIndentedString(pending)).append("\n");
    sb.append("    information: ").append(toIndentedString(information)).append("\n");
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
