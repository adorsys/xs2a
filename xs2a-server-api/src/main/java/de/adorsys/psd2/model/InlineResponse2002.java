package de.adorsys.psd2.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import java.util.Objects;

/**
 * InlineResponse2002
 */
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2019-11-11T13:48:52.194360+02:00[Europe/Kiev]")

public class InlineResponse2002   {
  @JsonProperty("cardAccount")
  private CardAccountDetails cardAccount = null;

  public InlineResponse2002 cardAccount(CardAccountDetails cardAccount) {
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
  public CardAccountDetails getCardAccount() {
    return cardAccount;
  }

  public void setCardAccount(CardAccountDetails cardAccount) {
    this.cardAccount = cardAccount;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    InlineResponse2002 inlineResponse2002 = (InlineResponse2002) o;
    return Objects.equals(this.cardAccount, inlineResponse2002.cardAccount);
  }

  @Override
  public int hashCode() {
    return Objects.hash(cardAccount);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class InlineResponse2002 {\n");

    sb.append("    cardAccount: ").append(toIndentedString(cardAccount)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(java.lang.Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}

