package de.adorsys.psd2.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Objects;

/**
 * Content of the body of a Update PSU authentication request  Password subfield is used.
 */
@ApiModel(description = "Content of the body of a Update PSU authentication request  Password subfield is used. ")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2021-11-05T12:22:49.487689+02:00[Europe/Kiev]")

public class UpdatePsuAuthentication   {
  @JsonProperty("psuData")
  private PsuData psuData = null;

  public UpdatePsuAuthentication psuData(PsuData psuData) {
    this.psuData = psuData;
    return this;
  }

  /**
   * Get psuData
   * @return psuData
  **/
  @ApiModelProperty(required = true, value = "")
  @NotNull

  @Valid


  @JsonProperty("psuData")
  public PsuData getPsuData() {
    return psuData;
  }

  public void setPsuData(PsuData psuData) {
    this.psuData = psuData;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
}    UpdatePsuAuthentication updatePsuAuthentication = (UpdatePsuAuthentication) o;
    return Objects.equals(this.psuData, updatePsuAuthentication.psuData);
  }

  @Override
  public int hashCode() {
    return Objects.hash(psuData);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class UpdatePsuAuthentication {\n");

    sb.append("    psuData: ").append(toIndentedString(psuData)).append("\n");
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

