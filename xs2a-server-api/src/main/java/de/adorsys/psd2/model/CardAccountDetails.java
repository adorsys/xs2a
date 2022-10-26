/*
 * Copyright 2018-2022 adorsys GmbH & Co KG
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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.Map;
import java.util.Objects;

/**
 * Card account details.
 */
@Schema(description = "Card account details. ")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2022-10-26T13:16:54.081225+03:00[Europe/Kiev]")


public class CardAccountDetails   {
  @JsonProperty("resourceId")
  private String resourceId = null;

  @JsonProperty("maskedPan")
  private String maskedPan = null;

  @JsonProperty("currency")
  private String currency = null;

  @JsonProperty("ownerName")
  private String ownerName = null;

  @JsonProperty("name")
  private String name = null;

  @JsonProperty("displayName")
  private String displayName = null;

  @JsonProperty("product")
  private String product = null;

  @JsonProperty("debitAccounting")
  private Boolean debitAccounting = null;

  @JsonProperty("status")
  private AccountStatus status = null;

  /**
   * Specifies the usage of the account:   * PRIV: private personal account   * ORGA: professional account
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
     *
     * @return resourceId
     **/
    @Schema(description = "This is the data element to be used in the path when retrieving data from a dedicated account. This shall be filled, if addressable resource are created by the ASPSP on the /card-accounts endpoint. ")
    @JsonProperty("resourceId")

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
     * Masked Primary Account Number.
     *
     * @return maskedPan
     **/
    @Schema(example = "123456xxxxxx1234", required = true, description = "Masked Primary Account Number. ")
    @JsonProperty("maskedPan")
    @NotNull

    @Size(max = 35)
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
     * ISO 4217 Alpha 3 currency code.
     *
     * @return currency
     **/
    @Schema(example = "EUR", required = true, description = "ISO 4217 Alpha 3 currency code. ")
    @JsonProperty("currency")
    @NotNull

    @Pattern(regexp = "[A-Z]{3}")
    public String getCurrency() {
    return currency;
  }

  public void setCurrency(String currency) {
    this.currency = currency;
  }

  public CardAccountDetails ownerName(String ownerName) {
    this.ownerName = ownerName;
      return this;
  }

    /**
     * Name of the legal account owner.  If there is more than one owner, then e.g. two names might be noted here.  For a corporate account, the corporate name is used for this attribute. Even if supported by the ASPSP, the provision of this field might depend on the fact whether an explicit consent to this specific additional account information has been given by the PSU.
     *
     * @return ownerName
     **/
    @Schema(example = "John Doe", description = "Name of the legal account owner.  If there is more than one owner, then e.g. two names might be noted here.  For a corporate account, the corporate name is used for this attribute. Even if supported by the ASPSP, the provision of this field might depend on the fact whether an explicit consent to this specific additional account information has been given by the PSU. ")
    @JsonProperty("ownerName")

    @Size(max = 140)
    public String getOwnerName() {
    return ownerName;
  }

  public void setOwnerName(String ownerName) {
    this.ownerName = ownerName;
  }

  public CardAccountDetails name(String name) {
      this.name = name;
      return this;
  }

    /**
     * Name of the account, as assigned by the ASPSP,  in agreement with the account owner in order to provide an additional means of identification of the account.
     *
     * @return name
     **/
    @Schema(description = "Name of the account, as assigned by the ASPSP,  in agreement with the account owner in order to provide an additional means of identification of the account. ")
    @JsonProperty("name")

    @Size(max =70)   public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public CardAccountDetails displayName(String displayName) {
      this.displayName = displayName;
      return this;
  }

    /**
     * Name of the account as defined by the PSU within online channels.
     *
     * @return displayName
     **/
    @Schema(description = "Name of the account as defined by the PSU within online channels. ")
    @JsonProperty("displayName")

    @Size(max = 70)   public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public CardAccountDetails product(String product) {
      this.product = product;
      return this;
  }

    /**
     * Product Name of the Bank for this account, proprietary definition.
     *
     * @return product
     **/
    @Schema(description = "Product Name of the Bank for this account, proprietary definition. ")
    @JsonProperty("product")

@Size(max=35)   public String getProduct() {
    return product;
  }

  public void setProduct(String product) {
    this.product = product;
  }

  public CardAccountDetails debitAccounting(Boolean debitAccounting) {
      this.debitAccounting = debitAccounting;
      return this;
  }

    /**
     * If true, the amounts of debits on the reports are quoted positive with the related consequence for balances. If false, the amount of debits on the reports are quoted negative.
     *
     * @return debitAccounting
     **/
    @Schema(description = "If true, the amounts of debits on the reports are quoted positive with the related consequence for balances. If false, the amount of debits on the reports are quoted negative. ")
    @JsonProperty("debitAccounting")

  public Boolean isDebitAccounting() {
    return debitAccounting;
  }

  public void setDebitAccounting(Boolean debitAccounting) {
    this.debitAccounting = debitAccounting;
  }

  public CardAccountDetails status(AccountStatus status) {
      this.status = status;
      return this;
  }

    /**
     * Get status
     *
     * @return status
     **/
    @Schema(description = "")
    @JsonProperty("status")

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
     * Specifies the usage of the account:   * PRIV: private personal account   * ORGA: professional account
     *
     * @return usage
     **/
    @Schema(description = "Specifies the usage of the account:   * PRIV: private personal account   * ORGA: professional account ")
  @JsonProperty("usage")

@Size(max=4)   public UsageEnum getUsage() {
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
     * Specifications that might be provided by the ASPSP:   - characteristics of the account   - characteristics of the relevant card
     *
     * @return details
     **/
    @Schema(description = "Specifications that might be provided by the ASPSP:   - characteristics of the account   - characteristics of the relevant card ")
  @JsonProperty("details")

@Size(max=1000)   public String getDetails() {
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
  @Schema(description = "")
  @JsonProperty("creditLimit")

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
  @Schema(description = "")
  @JsonProperty("balances")

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
  @Schema(description = "")
  @JsonProperty("_links")

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
        Objects.equals(this.ownerName, cardAccountDetails.ownerName) &&
        Objects.equals(this.name, cardAccountDetails.name) &&
        Objects.equals(this.displayName, cardAccountDetails.displayName) &&
        Objects.equals(this.product, cardAccountDetails.product) &&
        Objects.equals(this.debitAccounting, cardAccountDetails.debitAccounting) &&
        Objects.equals(this.status, cardAccountDetails.status) &&
        Objects.equals(this.usage, cardAccountDetails.usage) &&
        Objects.equals(this.details, cardAccountDetails.details) &&
        Objects.equals(this.creditLimit, cardAccountDetails.creditLimit) &&
        Objects.equals(this.balances, cardAccountDetails.balances) &&
        Objects.equals(this._links, cardAccountDetails._links);
  }

  @Override
  public int hashCode() {
    return Objects.hash(resourceId, maskedPan, currency, ownerName, name, displayName, product, debitAccounting, status, usage, details, creditLimit, balances, _links);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class CardAccountDetails {\n");

    sb.append("    resourceId: ").append(toIndentedString(resourceId)).append("\n");
    sb.append("    maskedPan: ").append(toIndentedString(maskedPan)).append("\n");
    sb.append("    currency: ").append(toIndentedString(currency)).append("\n");
    sb.append("    ownerName: ").append(toIndentedString(ownerName)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    displayName: ").append(toIndentedString(displayName)).append("\n");
    sb.append("    product: ").append(toIndentedString(product)).append("\n");
    sb.append("    debitAccounting: ").append(toIndentedString(debitAccounting)).append("\n");
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
