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
import de.adorsys.psd2.model.PeriodicPaymentInitiationXmlPart2StandingorderTypeJson;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import javax.validation.constraints.*;

/**
 * The multipart message definition for the initiation of a periodic payment initiation  where the information of the payment is contained in an pain.001 message (Part 1) and  the additional informations related to the periodic payment is an additional JSON message (Part 2). 
 */
@ApiModel(description = "The multipart message definition for the initiation of a periodic payment initiation  where the information of the payment is contained in an pain.001 message (Part 1) and  the additional informations related to the periodic payment is an additional JSON message (Part 2). ")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2019-01-11T12:48:04.675377+02:00[Europe/Kiev]")

public class PeriodicPaymentInitiationMultipartBody   {
  @JsonProperty("xml_sct")
  private Object xmlSct = null;

  @JsonProperty("json_standingorderType")
  private PeriodicPaymentInitiationXmlPart2StandingorderTypeJson jsonStandingorderType = null;

  public PeriodicPaymentInitiationMultipartBody xmlSct(Object xmlSct) {
    this.xmlSct = xmlSct;
    return this;
  }

  /**
   * Get xmlSct
   * @return xmlSct
  **/
  @ApiModelProperty(value = "")


  public Object getXmlSct() {
    return xmlSct;
  }

  public void setXmlSct(Object xmlSct) {
    this.xmlSct = xmlSct;
  }

  public PeriodicPaymentInitiationMultipartBody jsonStandingorderType(PeriodicPaymentInitiationXmlPart2StandingorderTypeJson jsonStandingorderType) {
    this.jsonStandingorderType = jsonStandingorderType;
    return this;
  }

  /**
   * Get jsonStandingorderType
   * @return jsonStandingorderType
  **/
  @ApiModelProperty(value = "")

  @Valid

  public PeriodicPaymentInitiationXmlPart2StandingorderTypeJson getJsonStandingorderType() {
    return jsonStandingorderType;
  }

  public void setJsonStandingorderType(PeriodicPaymentInitiationXmlPart2StandingorderTypeJson jsonStandingorderType) {
    this.jsonStandingorderType = jsonStandingorderType;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PeriodicPaymentInitiationMultipartBody periodicPaymentInitiationMultipartBody = (PeriodicPaymentInitiationMultipartBody) o;
    return Objects.equals(this.xmlSct, periodicPaymentInitiationMultipartBody.xmlSct) &&
        Objects.equals(this.jsonStandingorderType, periodicPaymentInitiationMultipartBody.jsonStandingorderType);
  }

  @Override
  public int hashCode() {
    return Objects.hash(xmlSct, jsonStandingorderType);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class PeriodicPaymentInitiationMultipartBody {\n");
    
    sb.append("    xmlSct: ").append(toIndentedString(xmlSct)).append("\n");
    sb.append("    jsonStandingorderType: ").append(toIndentedString(jsonStandingorderType)).append("\n");
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

