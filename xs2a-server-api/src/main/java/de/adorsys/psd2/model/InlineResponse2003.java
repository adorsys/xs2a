package de.adorsys.psd2.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.validation.annotation.Validated;

import java.util.Objects;

/**
 * Equals \&quot;true\&quot; if sufficient funds are available at the time of the request,  \&quot;false\&quot; otherwise.
 */
@ApiModel(description = "Equals \"true\" if sufficient funds are available at the time of the request,  \"false\" otherwise. ")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2021-11-05T12:22:49.487689+02:00[Europe/Kiev]")

public class InlineResponse2003   {
  @JsonProperty("fundsAvailable")
  private Boolean fundsAvailable = null;

  public InlineResponse2003 fundsAvailable(Boolean fundsAvailable) {
    this.fundsAvailable = fundsAvailable;
    return this;
  }

  /**
   * Get fundsAvailable
   * @return fundsAvailable
  **/
  @ApiModelProperty(value = "")



  @JsonProperty("fundsAvailable")
  public Boolean getFundsAvailable() {
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
}    InlineResponse2003 inlineResponse2003 = (InlineResponse2003) o;
    return Objects.equals(this.fundsAvailable, inlineResponse2003.fundsAvailable);
  }

  @Override
  public int hashCode() {
    return Objects.hash(fundsAvailable);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class InlineResponse2003 {\n");

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

