package de.adorsys.psd2.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import java.util.Objects;

/**
 * InlineResponse2001
 */
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2021-11-05T12:22:49.487689+02:00[Europe/Kiev]")

public class InlineResponse2001   {
  @JsonProperty("transactionsDetails")
  private Transactions transactionsDetails = null;

  public InlineResponse2001 transactionsDetails(Transactions transactionsDetails) {
    this.transactionsDetails = transactionsDetails;
    return this;
  }

  /**
   * Get transactionsDetails
   * @return transactionsDetails
  **/
  @ApiModelProperty(value = "")

  @Valid


  @JsonProperty("transactionsDetails")
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
}    InlineResponse2001 inlineResponse2001 = (InlineResponse2001) o;
    return Objects.equals(this.transactionsDetails, inlineResponse2001.transactionsDetails);
  }

  @Override
  public int hashCode() {
    return Objects.hash(transactionsDetails);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class InlineResponse2001 {\n");

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

