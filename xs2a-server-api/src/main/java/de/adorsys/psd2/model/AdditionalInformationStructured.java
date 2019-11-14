package de.adorsys.psd2.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Objects;

/**
 * Is used if and only if the bookingStatus entry equals \&quot;information\&quot;. Every active standing order related to the dedicated payment account result into one entry.
 */
@ApiModel(description = "Is used if and only if the bookingStatus entry equals \"information\". Every active standing order related to the dedicated payment account result into one entry.")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2019-11-11T13:48:52.194360+02:00[Europe/Kiev]")

public class AdditionalInformationStructured   {
  @JsonProperty("standingOrderDetails")
  private StandingOrderDetails standingOrderDetails = null;

  public AdditionalInformationStructured standingOrderDetails(StandingOrderDetails standingOrderDetails) {
    this.standingOrderDetails = standingOrderDetails;
    return this;
  }

  /**
   * Get standingOrderDetails
   * @return standingOrderDetails
  **/
  @ApiModelProperty(required = true, value = "")
  @NotNull

  @Valid


  @JsonProperty("standingOrderDetails")
  public StandingOrderDetails getStandingOrderDetails() {
    return standingOrderDetails;
  }

  public void setStandingOrderDetails(StandingOrderDetails standingOrderDetails) {
    this.standingOrderDetails = standingOrderDetails;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AdditionalInformationStructured additionalInformationStructured = (AdditionalInformationStructured) o;
    return Objects.equals(this.standingOrderDetails, additionalInformationStructured.standingOrderDetails);
  }

  @Override
  public int hashCode() {
    return Objects.hash(standingOrderDetails);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AdditionalInformationStructured {\n");

    sb.append("    standingOrderDetails: ").append(toIndentedString(standingOrderDetails)).append("\n");
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

