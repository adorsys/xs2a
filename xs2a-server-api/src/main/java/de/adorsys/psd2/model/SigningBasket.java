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
import de.adorsys.psd2.model.ConsentIdList;
import de.adorsys.psd2.model.PaymentIdList;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import javax.validation.constraints.*;

/**
 * JSON Body of a establish signing basket request. The body shall contain at least one entry. 
 */
@ApiModel(description = "JSON Body of a establish signing basket request. The body shall contain at least one entry. ")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2019-01-11T12:48:04.675377+02:00[Europe/Kiev]")

public class SigningBasket   {
  @JsonProperty("paymentIds")
  private PaymentIdList paymentIds = null;

  @JsonProperty("consentIds")
  private ConsentIdList consentIds = null;

  public SigningBasket paymentIds(PaymentIdList paymentIds) {
    this.paymentIds = paymentIds;
    return this;
  }

  /**
   * Get paymentIds
   * @return paymentIds
  **/
  @ApiModelProperty(value = "")

  @Valid

  public PaymentIdList getPaymentIds() {
    return paymentIds;
  }

  public void setPaymentIds(PaymentIdList paymentIds) {
    this.paymentIds = paymentIds;
  }

  public SigningBasket consentIds(ConsentIdList consentIds) {
    this.consentIds = consentIds;
    return this;
  }

  /**
   * Get consentIds
   * @return consentIds
  **/
  @ApiModelProperty(value = "")

  @Valid

  public ConsentIdList getConsentIds() {
    return consentIds;
  }

  public void setConsentIds(ConsentIdList consentIds) {
    this.consentIds = consentIds;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SigningBasket signingBasket = (SigningBasket) o;
    return Objects.equals(this.paymentIds, signingBasket.paymentIds) &&
        Objects.equals(this.consentIds, signingBasket.consentIds);
  }

  @Override
  public int hashCode() {
    return Objects.hash(paymentIds, consentIds);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SigningBasket {\n");
    
    sb.append("    paymentIds: ").append(toIndentedString(paymentIds)).append("\n");
    sb.append("    consentIds: ").append(toIndentedString(consentIds)).append("\n");
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

