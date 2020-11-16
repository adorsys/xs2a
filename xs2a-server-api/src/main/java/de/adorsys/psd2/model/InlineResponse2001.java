package de.adorsys.psd2.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import de.adorsys.psd2.model.TransactionDetailsBody;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import javax.validation.constraints.*;

/**
 * InlineResponse2001
 */
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2020-11-12T17:35:11.808068+02:00[Europe/Kiev]")

public class InlineResponse2001   {
  @JsonProperty("transactionsDetails")
  private TransactionDetailsBody transactionsDetails = null;

  public InlineResponse2001 transactionsDetails(TransactionDetailsBody transactionsDetails) {
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
  public TransactionDetailsBody getTransactionsDetails() {
    return transactionsDetails;
  }

  public void setTransactionsDetails(TransactionDetailsBody transactionsDetails) {
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

