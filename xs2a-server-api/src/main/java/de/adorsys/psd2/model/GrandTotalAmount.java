package de.adorsys.psd2.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.Objects;

/**
 * Total amount of the instalment including charges, insurance and taxes in addition to the funded amount.
 */
@Schema(description = "Total amount of the instalment including charges, insurance and taxes in addition to the funded amount. ")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2022-05-06T13:00:42.214155+03:00[Europe/Kiev]")


public class GrandTotalAmount   {
  @JsonProperty("currency")
  private String currency = null;

  @JsonProperty("amount")
  private String amount = null;

  public GrandTotalAmount currency(String currency) {
    this.currency = currency;
    return this;
  }

  /**
   * ISO 4217 Alpha 3 currency code.
   * @return currency
   **/
  @Schema(example = "EUR", required = true, description = "ISO 4217 Alpha 3 currency code. ")
      @NotNull

  @Pattern(regexp="[A-Z]{3}")   public String getCurrency() {
    return currency;
  }

  public void setCurrency(String currency) {
    this.currency = currency;
  }

  public GrandTotalAmount amount(String amount) {
    this.amount = amount;
    return this;
  }

  /**
   * The amount given with fractional digits, where fractions must be compliant to the currency definition. Up to 14 significant figures. Negative amounts are signed by minus. The decimal separator is a dot.  **Example:** Valid representations for EUR with up to two decimals are:    * 1056   * 5768.2   * -1.50   * 5877.78
   * @return amount
   **/
  @Schema(example = "5877.78", required = true, description = "The amount given with fractional digits, where fractions must be compliant to the currency definition. Up to 14 significant figures. Negative amounts are signed by minus. The decimal separator is a dot.  **Example:** Valid representations for EUR with up to two decimals are:    * 1056   * 5768.2   * -1.50   * 5877.78 ")
      @NotNull

  @Pattern(regexp="-?[0-9]{1,14}(\\.[0-9]{1,3})?")   public String getAmount() {
    return amount;
  }

  public void setAmount(String amount) {
    this.amount = amount;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    GrandTotalAmount grandTotalAmount = (GrandTotalAmount) o;
    return Objects.equals(this.currency, grandTotalAmount.currency) &&
        Objects.equals(this.amount, grandTotalAmount.amount);
  }

  @Override
  public int hashCode() {
    return Objects.hash(currency, amount);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class GrandTotalAmount {\n");

    sb.append("    currency: ").append(toIndentedString(currency)).append("\n");
    sb.append("    amount: ").append(toIndentedString(amount)).append("\n");
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
