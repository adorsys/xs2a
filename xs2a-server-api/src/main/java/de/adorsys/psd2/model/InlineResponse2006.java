package de.adorsys.psd2.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Objects;

/**
 * InlineResponse2006
 */
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2022-05-06T13:00:42.214155+03:00[Europe/Kiev]")


public class InlineResponse2006   {
  @JsonProperty("transactionsDetails")
  private Transactions transactionsDetails = null;

  public InlineResponse2006 transactionsDetails(Transactions transactionsDetails) {
    this.transactionsDetails = transactionsDetails;
    return this;
  }

  /**
   * Get transactionsDetails
   * @return transactionsDetails
   **/
  @Schema(required = true, description = "")
      @NotNull

    @Valid
    public Transactions getTransactionsDetails() {
    return transactionsDetails;
  }

  public void setTransactionsDetails(Transactions transactionsDetails) {
    this.transactionsDetails = transactionsDetails;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    InlineResponse2006 inlineResponse2006 = (InlineResponse2006) o;
    return Objects.equals(this.transactionsDetails, inlineResponse2006.transactionsDetails);
  }

  @Override
  public int hashCode() {
    return Objects.hash(transactionsDetails);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class InlineResponse2006 {\n");

    sb.append("    transactionsDetails: ").append(toIndentedString(transactionsDetails)).append("\n");
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
