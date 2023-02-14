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

import javax.validation.constraints.NotNull;
import java.util.Objects;

/**
 * Content of the body of an authorisation confirmation request
 */
@Schema(description = "Content of the body of an authorisation confirmation request ")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2022-10-26T13:16:54.081225+03:00[Europe/Kiev]")


public class AuthorisationConfirmation {
    @JsonProperty("confirmationCode")
    private String confirmationCode = null;

    public AuthorisationConfirmation confirmationCode(String confirmationCode) {
        this.confirmationCode = confirmationCode;
        return this;
    }

    /**
     * Confirmation Code as retrieved by the TPP from the redirect based SCA process.
     *
     * @return confirmationCode
     **/
    @Schema(required = true, description = "Confirmation Code as retrieved by the TPP from the redirect based SCA process.")
    @JsonProperty("confirmationCode")
    @NotNull

    public String getConfirmationCode() {
        return confirmationCode;
    }

  public void setConfirmationCode(String confirmationCode) {
    this.confirmationCode = confirmationCode;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AuthorisationConfirmation authorisationConfirmation = (AuthorisationConfirmation) o;
    return Objects.equals(this.confirmationCode, authorisationConfirmation.confirmationCode);
  }

  @Override
  public int hashCode() {
    return Objects.hash(confirmationCode);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AuthorisationConfirmation {\n");

    sb.append("    confirmationCode: ").append(toIndentedString(confirmationCode)).append("\n");
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
