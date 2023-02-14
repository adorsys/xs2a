/*
 * Copyright 2018-2023 adorsys GmbH & Co KG
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
 * contact us at psd2@adorsys.com.
 */

package de.adorsys.psd2.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Objects;

/**
 * LinksTransactionDetails
 */
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2022-05-06T13:00:42.214155+03:00[Europe/Kiev]")


public class LinksTransactionDetails extends HashMap<String, HrefType>  {
  @JsonProperty("transactionDetails")
  private HrefType transactionDetails = null;

  public LinksTransactionDetails transactionDetails(HrefType transactionDetails) {
    this.transactionDetails = transactionDetails;
    return this;
  }

  /**
   * Get transactionDetails
   * @return transactionDetails
   **/
  @Schema(required = true, description = "")
      @NotNull

    @Valid
    public HrefType getTransactionDetails() {
    return transactionDetails;
  }

  public void setTransactionDetails(HrefType transactionDetails) {
    this.transactionDetails = transactionDetails;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    LinksTransactionDetails _linksTransactionDetails = (LinksTransactionDetails) o;
    return Objects.equals(this.transactionDetails, _linksTransactionDetails.transactionDetails) &&
        super.equals(o);
  }

  @Override
  public int hashCode() {
    return Objects.hash(transactionDetails, super.hashCode());
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class LinksTransactionDetails {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
    sb.append("    transactionDetails: ").append(toIndentedString(transactionDetails)).append("\n");
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
