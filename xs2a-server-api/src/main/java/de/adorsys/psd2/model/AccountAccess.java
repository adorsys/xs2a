package de.adorsys.psd2.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Requested access services for a consent.
 */
@ApiModel(description = "Requested access services for a consent. ")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2021-11-05T12:22:49.487689+02:00[Europe/Kiev]")

public class AccountAccess   {
  @JsonProperty("accounts")
  @Valid
  private List<AccountReference> accounts = null;

  @JsonProperty("balances")
  @Valid
  private List<AccountReference> balances = null;

  @JsonProperty("transactions")
  @Valid
  private List<AccountReference> transactions = null;

  @JsonProperty("additionalInformation")
  private AdditionalInformationAccess additionalInformation = null;

  /**
   * Optional if supported by API provider.  The values \"allAccounts\" and \"allAccountsWithOwnerName\" are admitted.  The support of the \"allAccountsWithOwnerName\" value by the ASPSP is optional.
   */
  public enum AvailableAccountsEnum {
    ALLACCOUNTS("allAccounts"),

    ALLACCOUNTSWITHOWNERNAME("allAccountsWithOwnerName");

    private String value;

    AvailableAccountsEnum(String value) {
      this.value = value;
    }

    @Override
    @JsonValue
    public String toString() {
      return String.valueOf(value);
    }

    @JsonCreator
    public static AvailableAccountsEnum fromValue(String text) {
      for (AvailableAccountsEnum b : AvailableAccountsEnum.values()) {
        if (String.valueOf(b.value).equals(text)) {
          return b;
        }
      }
      return null;
    }
  }

  @JsonProperty("availableAccounts")
  private AvailableAccountsEnum availableAccounts = null;

  /**
   * Optional if supported by API provider.  The values \"allAccounts\" and \"allAccountsWithOwnerName\" are admitted.  The support of the \"allAccountsWithOwnerName\" value by the ASPSP is optional.
   */
  public enum AvailableAccountsWithBalanceEnum {
    ALLACCOUNTS("allAccounts"),

    ALLACCOUNTSWITHOWNERNAME("allAccountsWithOwnerName");

    private String value;

    AvailableAccountsWithBalanceEnum(String value) {
      this.value = value;
    }

    @Override
    @JsonValue
    public String toString() {
      return String.valueOf(value);
    }

    @JsonCreator
    public static AvailableAccountsWithBalanceEnum fromValue(String text) {
      for (AvailableAccountsWithBalanceEnum b : AvailableAccountsWithBalanceEnum.values()) {
        if (String.valueOf(b.value).equals(text)) {
          return b;
        }
      }
      return null;
    }
  }

  @JsonProperty("availableAccountsWithBalance")
  private AvailableAccountsWithBalanceEnum availableAccountsWithBalance = null;

  /**
   * Optional if supported by API provider.  The values \"allAccounts\" and \"allAccountsWithOwnerName\" are admitted.  The support of the \"allAccountsWithOwnerName\" value by the ASPSP is optional.
   */
  public enum AllPsd2Enum {
    ALLACCOUNTS("allAccounts"),

    ALLACCOUNTSWITHOWNERNAME("allAccountsWithOwnerName");

    private String value;

    AllPsd2Enum(String value) {
      this.value = value;
    }

    @Override
    @JsonValue
    public String toString() {
      return String.valueOf(value);
    }

    @JsonCreator
    public static AllPsd2Enum fromValue(String text) {
      for (AllPsd2Enum b : AllPsd2Enum.values()) {
        if (String.valueOf(b.value).equals(text)) {
          return b;
        }
      }
      return null;
    }
  }

  @JsonProperty("allPsd2")
  private AllPsd2Enum allPsd2 = null;

  @JsonProperty("restrictedTo")
  @Valid
  private List<String> restrictedTo = null;

  public AccountAccess accounts(List<AccountReference> accounts) {
    this.accounts = accounts;
    return this;
  }

  public AccountAccess addAccountsItem(AccountReference accountsItem) {
    if (this.accounts == null) {
      this.accounts = new ArrayList<>();
    }
    this.accounts.add(accountsItem);
    return this;
  }

  /**
   * Is asking for detailed account information.   If the array is empty in a request, the TPP is asking for an accessible account list.  This may be restricted in a PSU/ASPSP authorization dialogue.  If the array is empty, also the arrays for balances, additionalInformation sub attributes or transactions shall be empty, if used.
   * @return accounts
  **/
  @ApiModelProperty(value = "Is asking for detailed account information.   If the array is empty in a request, the TPP is asking for an accessible account list.  This may be restricted in a PSU/ASPSP authorization dialogue.  If the array is empty, also the arrays for balances, additionalInformation sub attributes or transactions shall be empty, if used. ")

  @Valid


  @JsonProperty("accounts")
  public List<AccountReference> getAccounts() {
    return accounts;
  }

  public void setAccounts(List<AccountReference> accounts) {
    this.accounts = accounts;
  }

  public AccountAccess balances(List<AccountReference> balances) {
    this.balances = balances;
    return this;
  }

  public AccountAccess addBalancesItem(AccountReference balancesItem) {
    if (this.balances == null) {
      this.balances = new ArrayList<>();
    }
    this.balances.add(balancesItem);
    return this;
  }

  /**
   * Is asking for balances of the addressed accounts.  If the array is empty in the request, the TPP is asking for the balances of all accessible account lists.  This may be restricted in a PSU/ASPSP authorization dialogue.  If the array is empty, also the arrays for accounts, additionalInformation sub attributes or transactions shall be empty, if used.
   * @return balances
  **/
  @ApiModelProperty(value = "Is asking for balances of the addressed accounts.  If the array is empty in the request, the TPP is asking for the balances of all accessible account lists.  This may be restricted in a PSU/ASPSP authorization dialogue.  If the array is empty, also the arrays for accounts, additionalInformation sub attributes or transactions shall be empty, if used. ")

  @Valid


  @JsonProperty("balances")
  public List<AccountReference> getBalances() {
    return balances;
  }

  public void setBalances(List<AccountReference> balances) {
    this.balances = balances;
  }

  public AccountAccess transactions(List<AccountReference> transactions) {
    this.transactions = transactions;
    return this;
  }

  public AccountAccess addTransactionsItem(AccountReference transactionsItem) {
    if (this.transactions == null) {
      this.transactions = new ArrayList<>();
    }
    this.transactions.add(transactionsItem);
    return this;
  }

  /**
   * Is asking for transactions of the addressed accounts.   If the array is empty in the request, the TPP is asking for the transactions of all accessible account lists.  This may be restricted in a PSU/ASPSP authorization dialogue.  If the array is empty, also the arrays for accounts, additionalInformation sub attributes or balances shall be empty, if used.
   * @return transactions
  **/
  @ApiModelProperty(value = "Is asking for transactions of the addressed accounts.   If the array is empty in the request, the TPP is asking for the transactions of all accessible account lists.  This may be restricted in a PSU/ASPSP authorization dialogue.  If the array is empty, also the arrays for accounts, additionalInformation sub attributes or balances shall be empty, if used. ")

  @Valid


  @JsonProperty("transactions")
  public List<AccountReference> getTransactions() {
    return transactions;
  }

  public void setTransactions(List<AccountReference> transactions) {
    this.transactions = transactions;
  }

  public AccountAccess additionalInformation(AdditionalInformationAccess additionalInformation) {
    this.additionalInformation = additionalInformation;
    return this;
  }

  /**
   * Get additionalInformation
   * @return additionalInformation
  **/
  @ApiModelProperty(value = "")

  @Valid


  @JsonProperty("additionalInformation")
  public AdditionalInformationAccess getAdditionalInformation() {
    return additionalInformation;
  }

  public void setAdditionalInformation(AdditionalInformationAccess additionalInformation) {
    this.additionalInformation = additionalInformation;
  }

  public AccountAccess availableAccounts(AvailableAccountsEnum availableAccounts) {
    this.availableAccounts = availableAccounts;
    return this;
  }

  /**
   * Optional if supported by API provider.  The values \"allAccounts\" and \"allAccountsWithOwnerName\" are admitted.  The support of the \"allAccountsWithOwnerName\" value by the ASPSP is optional.
   * @return availableAccounts
  **/
  @ApiModelProperty(value = "Optional if supported by API provider.  The values \"allAccounts\" and \"allAccountsWithOwnerName\" are admitted.  The support of the \"allAccountsWithOwnerName\" value by the ASPSP is optional. ")



  @JsonProperty("availableAccounts")
  public AvailableAccountsEnum getAvailableAccounts() {
    return availableAccounts;
  }

  public void setAvailableAccounts(AvailableAccountsEnum availableAccounts) {
    this.availableAccounts = availableAccounts;
  }

  public AccountAccess availableAccountsWithBalance(AvailableAccountsWithBalanceEnum availableAccountsWithBalance) {
    this.availableAccountsWithBalance = availableAccountsWithBalance;
    return this;
  }

  /**
   * Optional if supported by API provider.  The values \"allAccounts\" and \"allAccountsWithOwnerName\" are admitted.  The support of the \"allAccountsWithOwnerName\" value by the ASPSP is optional.
   * @return availableAccountsWithBalance
  **/
  @ApiModelProperty(value = "Optional if supported by API provider.  The values \"allAccounts\" and \"allAccountsWithOwnerName\" are admitted.  The support of the \"allAccountsWithOwnerName\" value by the ASPSP is optional. ")



  @JsonProperty("availableAccountsWithBalance")
  public AvailableAccountsWithBalanceEnum getAvailableAccountsWithBalance() {
    return availableAccountsWithBalance;
  }

  public void setAvailableAccountsWithBalance(AvailableAccountsWithBalanceEnum availableAccountsWithBalance) {
    this.availableAccountsWithBalance = availableAccountsWithBalance;
  }

  public AccountAccess allPsd2(AllPsd2Enum allPsd2) {
    this.allPsd2 = allPsd2;
    return this;
  }

  /**
   * Optional if supported by API provider.  The values \"allAccounts\" and \"allAccountsWithOwnerName\" are admitted.  The support of the \"allAccountsWithOwnerName\" value by the ASPSP is optional.
   * @return allPsd2
  **/
  @ApiModelProperty(value = "Optional if supported by API provider.  The values \"allAccounts\" and \"allAccountsWithOwnerName\" are admitted.  The support of the \"allAccountsWithOwnerName\" value by the ASPSP is optional. ")



  @JsonProperty("allPsd2")
  public AllPsd2Enum getAllPsd2() {
    return allPsd2;
  }

  public void setAllPsd2(AllPsd2Enum allPsd2) {
    this.allPsd2 = allPsd2;
  }

  public AccountAccess restrictedTo(List<String> restrictedTo) {
    this.restrictedTo = restrictedTo;
    return this;
  }

  public AccountAccess addRestrictedToItem(String restrictedToItem) {
    if (this.restrictedTo == null) {
      this.restrictedTo = new ArrayList<>();
    }
    this.restrictedTo.add(restrictedToItem);
    return this;
  }

  /**
   * If the TPP requests access to accounts via availableAccounts (List of available accounts), global  or bank driven consents, the TPP may include this element to restrict access to the referred  account types. Absence of the element is interpreted as \"no restriction\" (therefore access to  accounts of all types is requested). The element may only occur, if each of the elements    - accounts    - balances    - transactions  is either not present or contains an empty array.
   * @return restrictedTo
  **/
  @ApiModelProperty(value = "If the TPP requests access to accounts via availableAccounts (List of available accounts), global  or bank driven consents, the TPP may include this element to restrict access to the referred  account types. Absence of the element is interpreted as \"no restriction\" (therefore access to  accounts of all types is requested). The element may only occur, if each of the elements    - accounts    - balances    - transactions  is either not present or contains an empty array.  ")



  @JsonProperty("restrictedTo")
  public List<String> getRestrictedTo() {
    return restrictedTo;
  }

  public void setRestrictedTo(List<String> restrictedTo) {
    this.restrictedTo = restrictedTo;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
}    AccountAccess accountAccess = (AccountAccess) o;
    return Objects.equals(this.accounts, accountAccess.accounts) &&
    Objects.equals(this.balances, accountAccess.balances) &&
    Objects.equals(this.transactions, accountAccess.transactions) &&
    Objects.equals(this.additionalInformation, accountAccess.additionalInformation) &&
    Objects.equals(this.availableAccounts, accountAccess.availableAccounts) &&
    Objects.equals(this.availableAccountsWithBalance, accountAccess.availableAccountsWithBalance) &&
    Objects.equals(this.allPsd2, accountAccess.allPsd2) &&
    Objects.equals(this.restrictedTo, accountAccess.restrictedTo);
  }

  @Override
  public int hashCode() {
    return Objects.hash(accounts, balances, transactions, additionalInformation, availableAccounts, availableAccountsWithBalance, allPsd2, restrictedTo);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AccountAccess {\n");

    sb.append("    accounts: ").append(toIndentedString(accounts)).append("\n");
    sb.append("    balances: ").append(toIndentedString(balances)).append("\n");
    sb.append("    transactions: ").append(toIndentedString(transactions)).append("\n");
    sb.append("    additionalInformation: ").append(toIndentedString(additionalInformation)).append("\n");
    sb.append("    availableAccounts: ").append(toIndentedString(availableAccounts)).append("\n");
    sb.append("    availableAccountsWithBalance: ").append(toIndentedString(availableAccountsWithBalance)).append("\n");
    sb.append("    allPsd2: ").append(toIndentedString(allPsd2)).append("\n");
    sb.append("    restrictedTo: ").append(toIndentedString(restrictedTo)).append("\n");
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

