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
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import javax.validation.constraints.*;

/**
 * Equals \&quot;true\&quot; if sufficient funds are available at the time of the request,  \&quot;false\&quot; otherwise. 
 */
@ApiModel(description = "Equals \"true\" if sufficient funds are available at the time of the request,  \"false\" otherwise. ")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2019-01-11T12:48:04.675377+02:00[Europe/Kiev]")

public class InlineResponse200   {
  @JsonProperty("fundsAvailable")
  private Boolean fundsAvailable = null;

  public InlineResponse200 fundsAvailable(Boolean fundsAvailable) {
    this.fundsAvailable = fundsAvailable;
    return this;
  }

  /**
   * Get fundsAvailable
   * @return fundsAvailable
  **/
  @ApiModelProperty(value = "")


  public Boolean isFundsAvailable() {
    return fundsAvailable;
  }

  public void setFundsAvailable(Boolean fundsAvailable) {
    this.fundsAvailable = fundsAvailable;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    InlineResponse200 inlineResponse200 = (InlineResponse200) o;
    return Objects.equals(this.fundsAvailable, inlineResponse200.fundsAvailable);
  }

  @Override
  public int hashCode() {
    return Objects.hash(fundsAvailable);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class InlineResponse200 {\n");
    
    sb.append("    fundsAvailable: ").append(toIndentedString(fundsAvailable)).append("\n");
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

