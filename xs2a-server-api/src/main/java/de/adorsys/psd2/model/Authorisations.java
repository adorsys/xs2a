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
import de.adorsys.psd2.model.AuthorisationsList;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import javax.validation.constraints.*;

/**
 * An array of all authorisationIds
 */
@ApiModel(description = "An array of all authorisationIds")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2019-01-11T12:48:04.675377+02:00[Europe/Kiev]")

public class Authorisations   {
  @JsonProperty("authorisationIds")
  private AuthorisationsList authorisationIds = null;

  public Authorisations authorisationIds(AuthorisationsList authorisationIds) {
    this.authorisationIds = authorisationIds;
    return this;
  }

  /**
   * Get authorisationIds
   * @return authorisationIds
  **/
  @ApiModelProperty(required = true, value = "")
  @NotNull

  @Valid

  public AuthorisationsList getAuthorisationIds() {
    return authorisationIds;
  }

  public void setAuthorisationIds(AuthorisationsList authorisationIds) {
    this.authorisationIds = authorisationIds;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Authorisations authorisations = (Authorisations) o;
    return Objects.equals(this.authorisationIds, authorisations.authorisationIds);
  }

  @Override
  public int hashCode() {
    return Objects.hash(authorisationIds);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Authorisations {\n");
    
    sb.append("    authorisationIds: ").append(toIndentedString(authorisationIds)).append("\n");
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

