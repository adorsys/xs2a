package de.adorsys.psd2.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Objects;

/**
 * JSON Request body for the \&quot;Confirmation of funds service\&quot;.  &lt;table&gt;  &lt;tr&gt;    &lt;td&gt;cardNumber&lt;/td&gt;    &lt;td&gt;String &lt;/td&gt;   &lt;td&gt;Optional&lt;/td&gt;   &lt;td&gt;Card Number of the card issued by the PIISP. Should be delivered if available.&lt;/td&gt; &lt;/tr&gt;  &lt;tr&gt;   &lt;td&gt;account&lt;/td&gt;   &lt;td&gt; Account Reference&lt;/td&gt;   &lt;td&gt;Mandatory&lt;/td&gt;   &lt;td&gt;PSU&#39;s account number.&lt;/td&gt; &lt;/tr&gt;  &lt;tr&gt;    &lt;td&gt;payee&lt;/td&gt;   &lt;td&gt;Max70Text&lt;/td&gt;   &lt;td&gt;Optional&lt;/td&gt;   &lt;td&gt;The merchant where the card is accepted as an information to the PSU.&lt;/td&gt; &lt;/tr&gt;  &lt;tr&gt;   &lt;td&gt;instructedAmount&lt;/td&gt;   &lt;td&gt;Amount&lt;/td&gt;   &lt;td&gt;Mandatory&lt;/td&gt;   &lt;td&gt;Transaction amount to be checked within the funds check mechanism.&lt;/td&gt; &lt;/tr&gt;  &lt;/table&gt;
 */
@ApiModel(description = "JSON Request body for the \"Confirmation of funds service\".  <table>  <tr>    <td>cardNumber</td>    <td>String </td>   <td>Optional</td>   <td>Card Number of the card issued by the PIISP. Should be delivered if available.</td> </tr>  <tr>   <td>account</td>   <td> Account Reference</td>   <td>Mandatory</td>   <td>PSU's account number.</td> </tr>  <tr>    <td>payee</td>   <td>Max70Text</td>   <td>Optional</td>   <td>The merchant where the card is accepted as an information to the PSU.</td> </tr>  <tr>   <td>instructedAmount</td>   <td>Amount</td>   <td>Mandatory</td>   <td>Transaction amount to be checked within the funds check mechanism.</td> </tr>  </table> ")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2021-11-05T12:22:49.487689+02:00[Europe/Kiev]")

public class ConfirmationOfFunds   {
  @JsonProperty("cardNumber")
  private String cardNumber = null;

  @JsonProperty("account")
  private AccountReference account = null;

  @JsonProperty("payee")
  private String payee = null;

  @JsonProperty("instructedAmount")
  private Amount instructedAmount = null;

  public ConfirmationOfFunds cardNumber(String cardNumber) {
    this.cardNumber = cardNumber;
    return this;
  }

  /**
   * Card Number of the card issued by the PIISP.  Should be delivered if available.
   * @return cardNumber
  **/
  @ApiModelProperty(value = "Card Number of the card issued by the PIISP.  Should be delivered if available. ")

@Size(max=35)

  @JsonProperty("cardNumber")
  public String getCardNumber() {
    return cardNumber;
  }

  public void setCardNumber(String cardNumber) {
    this.cardNumber = cardNumber;
  }

  public ConfirmationOfFunds account(AccountReference account) {
    this.account = account;
    return this;
  }

  /**
   * Get account
   * @return account
  **/
  @ApiModelProperty(required = true, value = "")
  @NotNull

  @Valid


  @JsonProperty("account")
  public AccountReference getAccount() {
    return account;
  }

  public void setAccount(AccountReference account) {
    this.account = account;
  }

  public ConfirmationOfFunds payee(String payee) {
    this.payee = payee;
    return this;
  }

  /**
   * Name payee.
   * @return payee
  **/
  @ApiModelProperty(value = "Name payee.")

@Size(max=70)

  @JsonProperty("payee")
  public String getPayee() {
    return payee;
  }

  public void setPayee(String payee) {
    this.payee = payee;
  }

  public ConfirmationOfFunds instructedAmount(Amount instructedAmount) {
    this.instructedAmount = instructedAmount;
    return this;
  }

  /**
   * Get instructedAmount
   * @return instructedAmount
  **/
  @ApiModelProperty(required = true, value = "")
  @NotNull

  @Valid


  @JsonProperty("instructedAmount")
  public Amount getInstructedAmount() {
    return instructedAmount;
  }

  public void setInstructedAmount(Amount instructedAmount) {
    this.instructedAmount = instructedAmount;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
}    ConfirmationOfFunds confirmationOfFunds = (ConfirmationOfFunds) o;
    return Objects.equals(this.cardNumber, confirmationOfFunds.cardNumber) &&
    Objects.equals(this.account, confirmationOfFunds.account) &&
    Objects.equals(this.payee, confirmationOfFunds.payee) &&
    Objects.equals(this.instructedAmount, confirmationOfFunds.instructedAmount);
  }

  @Override
  public int hashCode() {
    return Objects.hash(cardNumber, account, payee, instructedAmount);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ConfirmationOfFunds {\n");

    sb.append("    cardNumber: ").append(toIndentedString(cardNumber)).append("\n");
    sb.append("    account: ").append(toIndentedString(account)).append("\n");
    sb.append("    payee: ").append(toIndentedString(payee)).append("\n");
    sb.append("    instructedAmount: ").append(toIndentedString(instructedAmount)).append("\n");
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

