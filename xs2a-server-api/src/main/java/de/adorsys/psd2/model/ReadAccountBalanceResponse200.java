package de.adorsys.psd2.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Objects;

/**
 * Body of the response for a successful read balance for an account request.
 */
@Schema(description = "Body of the response for a successful read balance for an account request.")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2022-05-06T13:00:42.214155+03:00[Europe/Kiev]")


public class ReadAccountBalanceResponse200   {
  @JsonProperty("account")
  private AccountReference account = null;

  @JsonProperty("balances")
  private BalanceList balances = null;

  public ReadAccountBalanceResponse200 account(AccountReference account) {
    this.account = account;
    return this;
  }

  /**
   * Get account
   * @return account
   **/
  @Schema(description = "")

    @Valid
    public AccountReference getAccount() {
    return account;
  }

  public void setAccount(AccountReference account) {
    this.account = account;
  }

  public ReadAccountBalanceResponse200 balances(BalanceList balances) {
    this.balances = balances;
    return this;
  }

  /**
   * Get balances
   * @return balances
   **/
  @Schema(required = true, description = "")
      @NotNull

    @Valid
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
    }
    ReadAccountBalanceResponse200 readAccountBalanceResponse200 = (ReadAccountBalanceResponse200) o;
    return Objects.equals(this.account, readAccountBalanceResponse200.account) &&
        Objects.equals(this.balances, readAccountBalanceResponse200.balances);
  }

  @Override
  public int hashCode() {
    return Objects.hash(account, balances);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ReadAccountBalanceResponse200 {\n");

    sb.append("    account: ").append(toIndentedString(account)).append("\n");
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
