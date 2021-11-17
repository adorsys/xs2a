package de.adorsys.psd2.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * List of card accounts with details.
 */
@ApiModel(description = "List of card accounts with details. ")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2021-11-05T12:22:49.487689+02:00[Europe/Kiev]")

public class CardAccountList   {
  @JsonProperty("cardAccounts")
  @Valid
  private List<CardAccountDetails> cardAccounts = new ArrayList<>();

  public CardAccountList cardAccounts(List<CardAccountDetails> cardAccounts) {
    this.cardAccounts = cardAccounts;
    return this;
  }

  public CardAccountList addCardAccountsItem(CardAccountDetails cardAccountsItem) {
    this.cardAccounts.add(cardAccountsItem);
    return this;
  }

  /**
   * Get cardAccounts
   * @return cardAccounts
  **/
  @ApiModelProperty(required = true, value = "")
  @NotNull

  @Valid


  @JsonProperty("cardAccounts")
  public List<CardAccountDetails> getCardAccounts() {
    return cardAccounts;
  }

  public void setCardAccounts(List<CardAccountDetails> cardAccounts) {
    this.cardAccounts = cardAccounts;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
}    CardAccountList cardAccountList = (CardAccountList) o;
    return Objects.equals(this.cardAccounts, cardAccountList.cardAccounts);
  }

  @Override
  public int hashCode() {
    return Objects.hash(cardAccounts);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class CardAccountList {\n");

    sb.append("    cardAccounts: ").append(toIndentedString(cardAccounts)).append("\n");
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

