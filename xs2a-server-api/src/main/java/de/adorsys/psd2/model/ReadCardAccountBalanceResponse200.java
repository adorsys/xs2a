package de.adorsys.psd2.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Objects;

/**
 * Body of the response for a successful read balance for a card account request.
 */
@ApiModel(description = "Body of the response for a successful read balance for a card account request.")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2021-11-05T12:22:49.487689+02:00[Europe/Kiev]")

public class ReadCardAccountBalanceResponse200   {
  @JsonProperty("cardAccount")
  private AccountReference cardAccount = null;

  @JsonProperty("debitAccounting")
  private Boolean debitAccounting = null;

  @JsonProperty("balances")
  private BalanceList balances = null;

  public ReadCardAccountBalanceResponse200 cardAccount(AccountReference cardAccount) {
    this.cardAccount = cardAccount;
    return this;
  }

  /**
   * Get cardAccount
   * @return cardAccount
  **/
  @ApiModelProperty(value = "")

  @Valid


  @JsonProperty("cardAccount")
  public AccountReference getCardAccount() {
    return cardAccount;
  }

  public void setCardAccount(AccountReference cardAccount) {
    this.cardAccount = cardAccount;
  }

  public ReadCardAccountBalanceResponse200 debitAccounting(Boolean debitAccounting) {
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

  public ReadCardAccountBalanceResponse200 balances(BalanceList balances) {
    this.balances = balances;
    return this;
  }

  /**
   * Get balances
   * @return balances
  **/
  @ApiModelProperty(required = true, value = "")
  @NotNull

  @Valid


  @JsonProperty("balances")
  public BalanceList getBalances() {
    return balances;
  }

  public void setBalances(BalanceList balances) {
    this.balances = balances;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
}    ReadCardAccountBalanceResponse200 readCardAccountBalanceResponse200 = (ReadCardAccountBalanceResponse200) o;
    return Objects.equals(this.cardAccount, readCardAccountBalanceResponse200.cardAccount) &&
    Objects.equals(this.debitAccounting, readCardAccountBalanceResponse200.debitAccounting) &&
    Objects.equals(this.balances, readCardAccountBalanceResponse200.balances);
  }

  @Override
  public int hashCode() {
    return Objects.hash(cardAccount, debitAccounting, balances);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ReadCardAccountBalanceResponse200 {\n");

    sb.append("    cardAccount: ").append(toIndentedString(cardAccount)).append("\n");
    sb.append("    debitAccounting: ").append(toIndentedString(debitAccounting)).append("\n");
    sb.append("    balances: ").append(toIndentedString(balances)).append("\n");
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

