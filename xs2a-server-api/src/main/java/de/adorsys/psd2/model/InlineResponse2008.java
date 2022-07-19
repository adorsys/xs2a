package de.adorsys.psd2.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;
import java.util.Objects;

/**
 * Equals \&quot;true\&quot; if sufficient funds are available at the time of the request,  \&quot;false\&quot; otherwise.
 */
@Schema(description = "Equals \"true\" if sufficient funds are available at the time of the request,  \"false\" otherwise. ")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2022-05-06T13:00:42.214155+03:00[Europe/Kiev]")


public class InlineResponse2008   {
  @JsonProperty("fundsAvailable")
  private Boolean fundsAvailable = null;

  public InlineResponse2008 fundsAvailable(Boolean fundsAvailable) {
    this.fundsAvailable = fundsAvailable;
    return this;
  }

  /**
   * Equals true if sufficient funds are available at the time of the request, false otherwise.  This datalemenet is allways contained in a confirmation of funds response.  This data element is contained in a payment status response,  if supported by the ASPSP, if a funds check has been performed and  if the transactionStatus is \"ACTC\", \"ACWC\" or \"ACCP\".
   * @return fundsAvailable
   **/
  @Schema(required = true, description = "Equals true if sufficient funds are available at the time of the request, false otherwise.  This datalemenet is allways contained in a confirmation of funds response.  This data element is contained in a payment status response,  if supported by the ASPSP, if a funds check has been performed and  if the transactionStatus is \"ACTC\", \"ACWC\" or \"ACCP\". ")
      @NotNull

    public Boolean isFundsAvailable() {
    return fundsAvailable;
  }

  public void setFundsAvailable(Boolean fundsAvailable) {
    this.fundsAvailable = fundsAvailable;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    InlineResponse2008 inlineResponse2008 = (InlineResponse2008) o;
    return Objects.equals(this.fundsAvailable, inlineResponse2008.fundsAvailable);
  }

  @Override
  public int hashCode() {
    return Objects.hash(fundsAvailable);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class InlineResponse2008 {\n");

    sb.append("    fundsAvailable: ").append(toIndentedString(fundsAvailable)).append("\n");
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
