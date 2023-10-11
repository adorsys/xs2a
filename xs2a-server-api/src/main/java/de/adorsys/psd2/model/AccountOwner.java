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
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.validation.annotation.Validated;

import java.util.Objects;

/**
 * Identifies a Person that acts as an account owner
 */
@Schema(description = "Identifies a Person that acts as an account owner ")
@Validated
@jakarta.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2022-10-25T13:15:56.196839+03:00[Europe/Kiev]")


public class AccountOwner   {
  @JsonProperty("name")
  private String name = null;

  @JsonProperty("role")
  private String role = null;

  public AccountOwner name(String name) {
    this.name = name;
    return this;
  }

  /**
   * Account owner name
   * @return name
   **/
  @Schema(required = true, description = "Account owner name")
  @JsonProperty("name")
    @NotNull

@Size(max=70)   public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public AccountOwner role(String role) {
    this.role = role;
    return this;
  }

  /**
   * The following proprietary codes are used:   * \"owner\",    * \"legalRepresentative\",    * \"authorisedUser\"
   * @return role
   **/
  @Schema(description = "The following proprietary codes are used:   * \"owner\",    * \"legalRepresentative\",    * \"authorisedUser\" ")
  @JsonProperty("role")

@Size(max=35)   public String getRole() {
    return role;
  }

  public void setRole(String role) {
    this.role = role;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AccountOwner accountOwner = (AccountOwner) o;
    return Objects.equals(this.name, accountOwner.name) &&
        Objects.equals(this.role, accountOwner.role);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, role);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AccountOwner {\n");

    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    role: ").append(toIndentedString(role)).append("\n");
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
