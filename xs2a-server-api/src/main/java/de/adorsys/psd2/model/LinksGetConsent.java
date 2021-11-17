package de.adorsys.psd2.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Objects;

/**
 * A list of hyperlinks to be recognised by the TPP.  Links of type \&quot;account\&quot; and/or \&quot;cardAccount\&quot;, depending on the nature of the consent.
 */
@ApiModel(description = "A list of hyperlinks to be recognised by the TPP.  Links of type \"account\" and/or \"cardAccount\", depending on the nature of the consent. ")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2021-11-05T12:22:49.487689+02:00[Europe/Kiev]")

public class LinksGetConsent extends HashMap<String, HrefType>  {
  @JsonProperty("account")
  private HrefType account = null;

  @JsonProperty("card-account")
  private HrefType cardAccount = null;

  public LinksGetConsent account(HrefType account) {
    this.account = account;
    return this;
  }

  /**
   * Get account
   * @return account
  **/
  @ApiModelProperty(value = "")

  @Valid


  @JsonProperty("account")
  public HrefType getAccount() {
    return account;
  }

  public void setAccount(HrefType account) {
    this.account = account;
  }

  public LinksGetConsent cardAccount(HrefType cardAccount) {
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
  public HrefType getCardAccount() {
    return cardAccount;
  }

  public void setCardAccount(HrefType cardAccount) {
    this.cardAccount = cardAccount;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
}
    if (!super.equals(o)) {
    return false;
    }
    LinksGetConsent _linksGetConsent = (LinksGetConsent) o;
    return Objects.equals(this.account, _linksGetConsent.account) &&
    Objects.equals(this.cardAccount, _linksGetConsent.cardAccount);
  }

  @Override
  public int hashCode() {
    return Objects.hash(account, cardAccount, super.hashCode());
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class LinksGetConsent {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
    sb.append("    account: ").append(toIndentedString(account)).append("\n");
    sb.append("    cardAccount: ").append(toIndentedString(cardAccount)).append("\n");
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

