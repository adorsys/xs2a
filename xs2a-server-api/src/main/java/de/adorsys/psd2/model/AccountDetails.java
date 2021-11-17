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
 * The ASPSP shall give at least one of the account reference identifiers:   - iban   - bban   - pan   - maskedPan   - msisdn If the account is a multicurrency account currency code in \&quot;currency\&quot; is set to \&quot;XXX\&quot;.
 */
@ApiModel(description = "The ASPSP shall give at least one of the account reference identifiers:   - iban   - bban   - pan   - maskedPan   - msisdn If the account is a multicurrency account currency code in \"currency\" is set to \"XXX\". ")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2021-11-05T12:22:49.487689+02:00[Europe/Kiev]")

public class AccountDetails   {
  @JsonProperty("resourceId")
  private String resourceId = null;

  @JsonProperty("iban")
  private String iban = null;

  @JsonProperty("bban")
  private String bban = null;

  @JsonProperty("msisdn")
  private String msisdn = null;

  @JsonProperty("currency")
  private String currency = null;

  @JsonProperty("name")
  private String name = null;

  @JsonProperty("displayName")
  private String displayName = null;

  @JsonProperty("product")
  private String product = null;

  @JsonProperty("cashAccountType")
  private String cashAccountType = null;

  @JsonProperty("status")
  private AccountStatus status = null;

  @JsonProperty("bic")
  private String bic = null;

  @JsonProperty("linkedAccounts")
  private String linkedAccounts = null;

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

  @JsonProperty("balances")
  private BalanceList balances = null;

  @JsonProperty("_links")
  private Map _links = null;

  @JsonProperty("ownerName")
  private String ownerName = null;

  public AccountDetails resourceId(String resourceId) {
    this.resourceId = resourceId;
    return this;
  }

  /**
   * This shall be filled, if addressable resource are created by the ASPSP on the /accounts or /card-accounts endpoint.
   * @return resourceId
  **/
  @ApiModelProperty(value = "This shall be filled, if addressable resource are created by the ASPSP on the /accounts or /card-accounts endpoint.")



  @JsonProperty("resourceId")
  public String getResourceId() {
    return resourceId;
  }

  public void setResourceId(String resourceId) {
    this.resourceId = resourceId;
  }

  public AccountDetails iban(String iban) {
    this.iban = iban;
    return this;
  }

  /**
   * Get iban
   * @return iban
  **/
  @ApiModelProperty(value = "")

@Pattern(regexp="[A-Z]{2,2}[0-9]{2,2}[a-zA-Z0-9]{1,30}")

  @JsonProperty("iban")
  public String getIban() {
    return iban;
  }

  public void setIban(String iban) {
    this.iban = iban;
  }

  public AccountDetails bban(String bban) {
    this.bban = bban;
    return this;
  }

  /**
   * Get bban
   * @return bban
  **/
  @ApiModelProperty(value = "")

@Pattern(regexp="[a-zA-Z0-9]{1,30}")

  @JsonProperty("bban")
  public String getBban() {
    return bban;
  }

  public void setBban(String bban) {
    this.bban = bban;
  }

  public AccountDetails msisdn(String msisdn) {
    this.msisdn = msisdn;
    return this;
  }

  /**
   * Get msisdn
   * @return msisdn
  **/
  @ApiModelProperty(value = "")

@Size(max=35)

  @JsonProperty("msisdn")
  public String getMsisdn() {
    return msisdn;
  }

  public void setMsisdn(String msisdn) {
    this.msisdn = msisdn;
  }

  public AccountDetails currency(String currency) {
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

  public AccountDetails name(String name) {
    this.name = name;
    return this;
  }

  /**
   * Name of the account, as assigned by the ASPSP, in agreement with the account owner in order to provide an additional means of identification of the account.
   * @return name
  **/
  @ApiModelProperty(value = "Name of the account, as assigned by the ASPSP, in agreement with the account owner in order to provide an additional means of identification of the account.")

@Size(max=70)

  @JsonProperty("name")
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public AccountDetails displayName(String displayName) {
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

  public AccountDetails product(String product) {
    this.product = product;
    return this;
  }

  /**
   * Product name of the bank for this account, proprietary definition.
   * @return product
  **/
  @ApiModelProperty(value = "Product name of the bank for this account, proprietary definition.")

@Size(max=35)

  @JsonProperty("product")
  public String getProduct() {
    return product;
  }

  public void setProduct(String product) {
    this.product = product;
  }

  public AccountDetails cashAccountType(String cashAccountType) {
    this.cashAccountType = cashAccountType;
    return this;
  }

  /**
   * Get cashAccountType
   * @return cashAccountType
  **/
  @ApiModelProperty(value = "")



  @JsonProperty("cashAccountType")
  public String getCashAccountType() {
    return cashAccountType;
  }

  public void setCashAccountType(String cashAccountType) {
    this.cashAccountType = cashAccountType;
  }

  public AccountDetails status(AccountStatus status) {
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

  public AccountDetails bic(String bic) {
    this.bic = bic;
    return this;
  }

  /**
   * Get bic
   * @return bic
  **/
  @ApiModelProperty(value = "")

@Pattern(regexp="[A-Z]{6,6}[A-Z2-9][A-NP-Z0-9]([A-Z0-9]{3,3}){0,1}")

  @JsonProperty("bic")
  public String getBic() {
    return bic;
  }

  public void setBic(String bic) {
    this.bic = bic;
  }

  public AccountDetails linkedAccounts(String linkedAccounts) {
    this.linkedAccounts = linkedAccounts;
    return this;
  }

  /**
   * Case of a set of pending card transactions, the APSP will provide the relevant cash account the card is set up on.
   * @return linkedAccounts
  **/
  @ApiModelProperty(value = "Case of a set of pending card transactions, the APSP will provide the relevant cash account the card is set up on.")

@Size(max=70)

  @JsonProperty("linkedAccounts")
  public String getLinkedAccounts() {
    return linkedAccounts;
  }

  public void setLinkedAccounts(String linkedAccounts) {
    this.linkedAccounts = linkedAccounts;
  }

  public AccountDetails usage(UsageEnum usage) {
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

  public AccountDetails details(String details) {
    this.details = details;
    return this;
  }

  /**
   * Specifications that might be provided by the ASPSP:   - characteristics of the account   - characteristics of the relevant card
   * @return details
  **/
  @ApiModelProperty(value = "Specifications that might be provided by the ASPSP:   - characteristics of the account   - characteristics of the relevant card ")

@Size(max=500)

  @JsonProperty("details")
  public String getDetails() {
    return details;
  }

  public void setDetails(String details) {
    this.details = details;
  }

  public AccountDetails balances(BalanceList balances) {
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

  public AccountDetails _links(Map _links) {
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

  public AccountDetails ownerName(String ownerName) {
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


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
}    AccountDetails accountDetails = (AccountDetails) o;
    return Objects.equals(this.resourceId, accountDetails.resourceId) &&
    Objects.equals(this.iban, accountDetails.iban) &&
    Objects.equals(this.bban, accountDetails.bban) &&
    Objects.equals(this.msisdn, accountDetails.msisdn) &&
    Objects.equals(this.currency, accountDetails.currency) &&
    Objects.equals(this.name, accountDetails.name) &&
    Objects.equals(this.displayName, accountDetails.displayName) &&
    Objects.equals(this.product, accountDetails.product) &&
    Objects.equals(this.cashAccountType, accountDetails.cashAccountType) &&
    Objects.equals(this.status, accountDetails.status) &&
    Objects.equals(this.bic, accountDetails.bic) &&
    Objects.equals(this.linkedAccounts, accountDetails.linkedAccounts) &&
    Objects.equals(this.usage, accountDetails.usage) &&
    Objects.equals(this.details, accountDetails.details) &&
    Objects.equals(this.balances, accountDetails.balances) &&
    Objects.equals(this._links, accountDetails._links) &&
    Objects.equals(this.ownerName, accountDetails.ownerName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(resourceId, iban, bban, msisdn, currency, name, displayName, product, cashAccountType, status, bic, linkedAccounts, usage, details, balances, _links, ownerName);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AccountDetails {\n");

    sb.append("    resourceId: ").append(toIndentedString(resourceId)).append("\n");
    sb.append("    iban: ").append(toIndentedString(iban)).append("\n");
    sb.append("    bban: ").append(toIndentedString(bban)).append("\n");
    sb.append("    msisdn: ").append(toIndentedString(msisdn)).append("\n");
    sb.append("    currency: ").append(toIndentedString(currency)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    displayName: ").append(toIndentedString(displayName)).append("\n");
    sb.append("    product: ").append(toIndentedString(product)).append("\n");
    sb.append("    cashAccountType: ").append(toIndentedString(cashAccountType)).append("\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
    sb.append("    bic: ").append(toIndentedString(bic)).append("\n");
    sb.append("    linkedAccounts: ").append(toIndentedString(linkedAccounts)).append("\n");
    sb.append("    usage: ").append(toIndentedString(usage)).append("\n");
    sb.append("    details: ").append(toIndentedString(details)).append("\n");
    sb.append("    balances: ").append(toIndentedString(balances)).append("\n");
    sb.append("    _links: ").append(toIndentedString(_links)).append("\n");
    sb.append("    ownerName: ").append(toIndentedString(ownerName)).append("\n");
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

