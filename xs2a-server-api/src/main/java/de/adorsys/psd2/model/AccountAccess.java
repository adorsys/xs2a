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
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2019-01-11T12:48:04.675377+02:00[Europe/Kiev]")

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

  /**
   * Optional if supported by API provider.  Only the values \"allAccounts\" or \"allAccountsWithBalances\" is admitted. 
   */
  public enum AvailableAccountsEnum {
    ALLACCOUNTS("allAccounts"),
    
    ALLACCOUNTSWITHBALANCES("allAccountsWithBalances");

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
   * Optional if supported by API provider.  Only the value \"allAccounts\" is admitted. 
   */
  public enum AllPsd2Enum {
    ALLACCOUNTS("allAccounts");

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
   * Is asking for detailed account information.   If the array is empty, the TPP is asking for an accessible account list.  This may be restricted in a PSU/ASPSP authorization dialogue. If the array is empty, also the arrays for balances or transactions shall be empty, if used. 
   * @return accounts
  **/
  @ApiModelProperty(value = "Is asking for detailed account information.   If the array is empty, the TPP is asking for an accessible account list.  This may be restricted in a PSU/ASPSP authorization dialogue. If the array is empty, also the arrays for balances or transactions shall be empty, if used. ")

  @Valid

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
   * Is asking for balances of the addressed accounts.  If the array is empty, the TPP is asking for the balances of all accessible account lists.  This may be restricted in a PSU/ASPSP authorization dialogue. If the array is empty, also the arrays for accounts or transactions shall be empty, if used. 
   * @return balances
  **/
  @ApiModelProperty(value = "Is asking for balances of the addressed accounts.  If the array is empty, the TPP is asking for the balances of all accessible account lists.  This may be restricted in a PSU/ASPSP authorization dialogue. If the array is empty, also the arrays for accounts or transactions shall be empty, if used. ")

  @Valid

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
   * Is asking for transactions of the addressed accounts.   If the array is empty, the TPP is asking for the transactions of all accessible account lists.  This may be restricted in a PSU/ASPSP authorization dialogue. If the array is empty, also the arrays for accounts or balances shall be empty, if used. 
   * @return transactions
  **/
  @ApiModelProperty(value = "Is asking for transactions of the addressed accounts.   If the array is empty, the TPP is asking for the transactions of all accessible account lists.  This may be restricted in a PSU/ASPSP authorization dialogue. If the array is empty, also the arrays for accounts or balances shall be empty, if used. ")

  @Valid

  public List<AccountReference> getTransactions() {
    return transactions;
  }

  public void setTransactions(List<AccountReference> transactions) {
    this.transactions = transactions;
  }

  public AccountAccess availableAccounts(AvailableAccountsEnum availableAccounts) {
    this.availableAccounts = availableAccounts;
    return this;
  }

  /**
   * Optional if supported by API provider.  Only the values \"allAccounts\" or \"allAccountsWithBalances\" is admitted. 
   * @return availableAccounts
  **/
  @ApiModelProperty(value = "Optional if supported by API provider.  Only the values \"allAccounts\" or \"allAccountsWithBalances\" is admitted. ")


  public AvailableAccountsEnum getAvailableAccounts() {
    return availableAccounts;
  }

  public void setAvailableAccounts(AvailableAccountsEnum availableAccounts) {
    this.availableAccounts = availableAccounts;
  }

  public AccountAccess allPsd2(AllPsd2Enum allPsd2) {
    this.allPsd2 = allPsd2;
    return this;
  }

  /**
   * Optional if supported by API provider.  Only the value \"allAccounts\" is admitted. 
   * @return allPsd2
  **/
  @ApiModelProperty(value = "Optional if supported by API provider.  Only the value \"allAccounts\" is admitted. ")


  public AllPsd2Enum getAllPsd2() {
    return allPsd2;
  }

  public void setAllPsd2(AllPsd2Enum allPsd2) {
    this.allPsd2 = allPsd2;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AccountAccess accountAccess = (AccountAccess) o;
    return Objects.equals(this.accounts, accountAccess.accounts) &&
        Objects.equals(this.balances, accountAccess.balances) &&
        Objects.equals(this.transactions, accountAccess.transactions) &&
        Objects.equals(this.availableAccounts, accountAccess.availableAccounts) &&
        Objects.equals(this.allPsd2, accountAccess.allPsd2);
  }

  @Override
  public int hashCode() {
    return Objects.hash(accounts, balances, transactions, availableAccounts, allPsd2);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AccountAccess {\n");
    
    sb.append("    accounts: ").append(toIndentedString(accounts)).append("\n");
    sb.append("    balances: ").append(toIndentedString(balances)).append("\n");
    sb.append("    transactions: ").append(toIndentedString(transactions)).append("\n");
    sb.append("    availableAccounts: ").append(toIndentedString(availableAccounts)).append("\n");
    sb.append("    allPsd2: ").append(toIndentedString(allPsd2)).append("\n");
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

