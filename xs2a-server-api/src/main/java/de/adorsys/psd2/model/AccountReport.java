/*
 * Copyright 2018-2019 adorsys GmbH & Co KG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.adorsys.psd2.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import de.adorsys.psd2.model.TransactionList;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.Map;
import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import javax.validation.constraints.*;

/**
 * JSON based account report. This account report contains transactions resulting from the query parameters.  &#39;booked&#39; shall be contained if bookingStatus parameter is set to \&quot;booked\&quot; or \&quot;both\&quot;.  &#39;pending&#39; is not contained if the bookingStatus parameter is set to \&quot;booked\&quot;. 
 */
@ApiModel(description = "JSON based account report. This account report contains transactions resulting from the query parameters.  'booked' shall be contained if bookingStatus parameter is set to \"booked\" or \"both\".  'pending' is not contained if the bookingStatus parameter is set to \"booked\". ")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2019-01-11T12:48:04.675377+02:00[Europe/Kiev]")

public class AccountReport   {
  @JsonProperty("booked")
  private TransactionList booked = null;

  @JsonProperty("pending")
  private TransactionList pending = null;

  @JsonProperty("_links")
  private Map _links = null;

  public AccountReport booked(TransactionList booked) {
    this.booked = booked;
    return this;
  }

  /**
   * Get booked
   * @return booked
  **/
  @ApiModelProperty(value = "")

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
   * @return pending
  **/
  @ApiModelProperty(value = "")

  @Valid

  public TransactionList getPending() {
    return pending;
  }

  public void setPending(TransactionList pending) {
    this.pending = pending;
  }

  public AccountReport _links(Map _links) {
    this._links = _links;
    return this;
  }

  /**
   * Get _links
   * @return _links
  **/
  @ApiModelProperty(required = true, value = "")
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
        Objects.equals(this._links, accountReport._links);
  }

  @Override
  public int hashCode() {
    return Objects.hash(booked, pending, _links);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AccountReport {\n");
    
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

