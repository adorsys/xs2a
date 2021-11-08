package de.adorsys.psd2.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
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
@ApiModel(description = "Card account details. ")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2021-11-05T12:22:49.487689+02:00[Europe/Kiev]")

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
   * @return resourceId
  **/
  @ApiModelProperty(value = "This is the data element to be used in the path when retrieving data from a dedicated account. This shall be filled, if addressable resource are created by the ASPSP on the /card-accounts endpoint. ")



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
   * Get maskedPan
   * @return maskedPan
  **/
  @ApiModelProperty(required = true, value = "")
  @NotNull

@Size(max=35)

  @JsonProperty("maskedPan")
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

  @JsonProperty("currency")
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
   * Get ownerName
   * @return ownerName
  **/
  @ApiModelProperty(value = "")

@Size(max=140)

  @JsonProperty("ownerName")
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
   * @return name
  **/
  @ApiModelProperty(value = "Name of the account, as assigned by the ASPSP,  in agreement with the account owner in order to provide an additional means of identification of the account. ")

@Size(max=70)

  @JsonProperty("name")
  public String getName() {
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
   * Get displayName
   * @return displayName
  **/
  @ApiModelProperty(value = "")

@Size(max=70)

  @JsonProperty("displayName")
  public String getDisplayName() {
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
   * @return product
  **/
  @ApiModelProperty(value = "Product Name of the Bank for this account, proprietary definition. ")

@Size(max=35)

  @JsonProperty("product")
  public String getProduct() {
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
   * Get debitAccounting
   * @return debitAccounting
  **/
  @ApiModelProperty(value = "")



  @JsonProperty("debitAccounting")
  public Boolean getDebitAccounting() {
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
   * @return status
  **/
  @ApiModelProperty(value = "")

  @Valid


  @JsonProperty("status")
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
   * @return usage
  **/
  @ApiModelProperty(value = "Specifies the usage of the account:   * PRIV: private personal account   * ORGA: professional account ")

@Size(max=4)

  @JsonProperty("usage")
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
   * Specifications that might be provided by the ASPSP:   - characteristics of the account   - characteristics of the relevant card
   * @return details
  **/
  @ApiModelProperty(value = "Specifications that might be provided by the ASPSP:   - characteristics of the account   - characteristics of the relevant card ")

@Size(max=1000)

  @JsonProperty("details")
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


  @JsonProperty("creditLimit")
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


  @JsonProperty("balances")
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


  @JsonProperty("_links")
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
}    CardAccountDetails cardAccountDetails = (CardAccountDetails) o;
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

