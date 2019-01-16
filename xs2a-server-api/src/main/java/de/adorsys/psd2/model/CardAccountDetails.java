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
import com.fasterxml.jackson.annotation.JsonValue;
import de.adorsys.psd2.model.AccountStatus;
import de.adorsys.psd2.model.Amount;
import de.adorsys.psd2.model.BalanceList;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.Map;
import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import javax.validation.constraints.*;

/**
 * Card account details 
 */
@ApiModel(description = "Card account details ")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2019-01-11T12:48:04.675377+02:00[Europe/Kiev]")

public class CardAccountDetails   {
  @JsonProperty("resourceId")
  private String resourceId = null;

  @JsonProperty("maskedPan")
  private String maskedPan = null;

  @JsonProperty("currency")
  private String currency = null;

  @JsonProperty("name")
  private String name = null;

  @JsonProperty("product")
  private String product = null;

  @JsonProperty("status")
  private AccountStatus status = null;

  /**
   * Specifies the usage of the account   * PRIV: private personal account   * ORGA: professional account 
   */
  public enum UsageEnum {
    PRIV("PRIV"),
    
    ORGA("ORGA");

    private String value;

    UsageEnum(String value) {
      this.value = value;
    }

    @Override
    @JsonValue
    public String toString() {
      return String.valueOf(value);
    }

    @JsonCreator
    public static UsageEnum fromValue(String text) {
      for (UsageEnum b : UsageEnum.values()) {
        if (String.valueOf(b.value).equals(text)) {
          return b;
        }
      }
      return null;
    }
  }

  @JsonProperty("usage")
  private UsageEnum usage = null;

  @JsonProperty("details")
  private String details = null;

  @JsonProperty("creditLimit")
  private Amount creditLimit = null;

  @JsonProperty("balances")
  private BalanceList balances = null;

  @JsonProperty("_links")
  private Map _links = null;

  public CardAccountDetails resourceId(String resourceId) {
    this.resourceId = resourceId;
    return this;
  }

  /**
   * This is the data element to be used in the path when retrieving data from a dedicated account. This shall be filled, if addressable resource are created by the ASPSP on the /card-accounts endpoint. 
   * @return resourceId
  **/
  @ApiModelProperty(value = "This is the data element to be used in the path when retrieving data from a dedicated account. This shall be filled, if addressable resource are created by the ASPSP on the /card-accounts endpoint. ")


  public String getResourceId() {
    return resourceId;
  }

  public void setResourceId(String resourceId) {
    this.resourceId = resourceId;
  }

  public CardAccountDetails maskedPan(String maskedPan) {
    this.maskedPan = maskedPan;
    return this;
  }

  /**
   * Get maskedPan
   * @return maskedPan
  **/
  @ApiModelProperty(required = true, value = "")
  @NotNull

@Size(max=35) 
  public String getMaskedPan() {
    return maskedPan;
  }

  public void setMaskedPan(String maskedPan) {
    this.maskedPan = maskedPan;
  }

  public CardAccountDetails currency(String currency) {
    this.currency = currency;
    return this;
  }

  /**
   * Get currency
   * @return currency
  **/
  @ApiModelProperty(required = true, value = "")
  @NotNull

@Pattern(regexp="[A-Z]{3}") 
  public String getCurrency() {
    return currency;
  }

  public void setCurrency(String currency) {
    this.currency = currency;
  }

  public CardAccountDetails name(String name) {
    this.name = name;
    return this;
  }

  /**
   * Name of the account given by the bank or the PSU in online-banking.
   * @return name
  **/
  @ApiModelProperty(value = "Name of the account given by the bank or the PSU in online-banking.")

@Size(max=35) 
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public CardAccountDetails product(String product) {
    this.product = product;
    return this;
  }

  /**
   * Product name of the bank for this account, proprietary definition.
   * @return product
  **/
  @ApiModelProperty(value = "Product name of the bank for this account, proprietary definition.")

@Size(max=35) 
  public String getProduct() {
    return product;
  }

  public void setProduct(String product) {
    this.product = product;
  }

  public CardAccountDetails status(AccountStatus status) {
    this.status = status;
    return this;
  }

  /**
   * Get status
   * @return status
  **/
  @ApiModelProperty(value = "")

  @Valid

  public AccountStatus getStatus() {
    return status;
  }

  public void setStatus(AccountStatus status) {
    this.status = status;
  }

  public CardAccountDetails usage(UsageEnum usage) {
    this.usage = usage;
    return this;
  }

  /**
   * Specifies the usage of the account   * PRIV: private personal account   * ORGA: professional account 
   * @return usage
  **/
  @ApiModelProperty(value = "Specifies the usage of the account   * PRIV: private personal account   * ORGA: professional account ")

@Size(max=4) 
  public UsageEnum getUsage() {
    return usage;
  }

  public void setUsage(UsageEnum usage) {
    this.usage = usage;
  }

  public CardAccountDetails details(String details) {
    this.details = details;
    return this;
  }

  /**
   * Specifications that might be provided by the ASPSP   - characteristics of the account   - characteristics of the relevant card 
   * @return details
  **/
  @ApiModelProperty(value = "Specifications that might be provided by the ASPSP   - characteristics of the account   - characteristics of the relevant card ")

@Size(max=140) 
  public String getDetails() {
    return details;
  }

  public void setDetails(String details) {
    this.details = details;
  }

  public CardAccountDetails creditLimit(Amount creditLimit) {
    this.creditLimit = creditLimit;
    return this;
  }

  /**
   * Get creditLimit
   * @return creditLimit
  **/
  @ApiModelProperty(value = "")

  @Valid

  public Amount getCreditLimit() {
    return creditLimit;
  }

  public void setCreditLimit(Amount creditLimit) {
    this.creditLimit = creditLimit;
  }

  public CardAccountDetails balances(BalanceList balances) {
    this.balances = balances;
    return this;
  }

  /**
   * Get balances
   * @return balances
  **/
  @ApiModelProperty(value = "")

  @Valid

  public BalanceList getBalances() {
    return balances;
  }

  public void setBalances(BalanceList balances) {
    this.balances = balances;
  }

  public CardAccountDetails _links(Map _links) {
    this._links = _links;
    return this;
  }

  /**
   * Get _links
   * @return _links
  **/
  @ApiModelProperty(value = "")

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
    CardAccountDetails cardAccountDetails = (CardAccountDetails) o;
    return Objects.equals(this.resourceId, cardAccountDetails.resourceId) &&
        Objects.equals(this.maskedPan, cardAccountDetails.maskedPan) &&
        Objects.equals(this.currency, cardAccountDetails.currency) &&
        Objects.equals(this.name, cardAccountDetails.name) &&
        Objects.equals(this.product, cardAccountDetails.product) &&
        Objects.equals(this.status, cardAccountDetails.status) &&
        Objects.equals(this.usage, cardAccountDetails.usage) &&
        Objects.equals(this.details, cardAccountDetails.details) &&
        Objects.equals(this.creditLimit, cardAccountDetails.creditLimit) &&
        Objects.equals(this.balances, cardAccountDetails.balances) &&
        Objects.equals(this._links, cardAccountDetails._links);
  }

  @Override
  public int hashCode() {
    return Objects.hash(resourceId, maskedPan, currency, name, product, status, usage, details, creditLimit, balances, _links);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class CardAccountDetails {\n");
    
    sb.append("    resourceId: ").append(toIndentedString(resourceId)).append("\n");
    sb.append("    maskedPan: ").append(toIndentedString(maskedPan)).append("\n");
    sb.append("    currency: ").append(toIndentedString(currency)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    product: ").append(toIndentedString(product)).append("\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
    sb.append("    usage: ").append(toIndentedString(usage)).append("\n");
    sb.append("    details: ").append(toIndentedString(details)).append("\n");
    sb.append("    creditLimit: ").append(toIndentedString(creditLimit)).append("\n");
    sb.append("    balances: ").append(toIndentedString(balances)).append("\n");
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

