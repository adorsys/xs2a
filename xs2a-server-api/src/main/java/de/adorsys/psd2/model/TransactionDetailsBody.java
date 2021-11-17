package de.adorsys.psd2.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Objects;

/**
 * Transaction details.
 */
@ApiModel(description = "Transaction details.")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2021-11-05T12:22:49.487689+02:00[Europe/Kiev]")

public class TransactionDetailsBody   {
  @JsonProperty("transactionDetails")
  private Transactions transactionDetails = null;

  public TransactionDetailsBody transactionDetails(Transactions transactionDetails) {
    this.transactionDetails = transactionDetails;
    return this;
  }

  /**
   * Get transactionDetails
   * @return transactionDetails
  **/
  @ApiModelProperty(required = true, value = "")
  @NotNull

  @Valid


  @JsonProperty("transactionDetails")
  public Transactions getTransactionDetails() {
    return transactionDetails;
  }

  public void setTransactionDetails(Transactions transactionDetails) {
    this.transactionDetails = transactionDetails;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
}    TransactionDetailsBody transactionDetailsBody = (TransactionDetailsBody) o;
    return Objects.equals(this.transactionDetails, transactionDetailsBody.transactionDetails);
  }

  @Override
  public int hashCode() {
    return Objects.hash(transactionDetails);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class TransactionDetailsBody {\n");

    sb.append("    transactionDetails: ").append(toIndentedString(transactionDetails)).append("\n");
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

