package de.adorsys.psd2.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import de.adorsys.psd2.model.ScaStatus;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import javax.validation.constraints.*;

/**
 * Body of the JSON response with SCA Status.
 */
@ApiModel(description = "Body of the JSON response with SCA Status.")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2020-11-12T17:35:11.808068+02:00[Europe/Kiev]")

public class ScaStatusResponse   {
  @JsonProperty("scaStatus")
  private ScaStatus scaStatus = null;

  @JsonProperty("trustedBeneficiaryFlag")
  private Boolean trustedBeneficiaryFlag = null;

  public ScaStatusResponse scaStatus(ScaStatus scaStatus) {
    this.scaStatus = scaStatus;
    return this;
  }

  /**
   * Get scaStatus
   * @return scaStatus
  **/
  @ApiModelProperty(value = "")

  @Valid


  @JsonProperty("scaStatus")
  public ScaStatus getScaStatus() {
    return scaStatus;
  }

  public void setScaStatus(ScaStatus scaStatus) {
    this.scaStatus = scaStatus;
  }

  public ScaStatusResponse trustedBeneficiaryFlag(Boolean trustedBeneficiaryFlag) {
    this.trustedBeneficiaryFlag = trustedBeneficiaryFlag;
    return this;
  }

  /**
   * Get trustedBeneficiaryFlag
   * @return trustedBeneficiaryFlag
  **/
  @ApiModelProperty(value = "")



  @JsonProperty("trustedBeneficiaryFlag")
  public Boolean getTrustedBeneficiaryFlag() {
    return trustedBeneficiaryFlag;
  }

  public void setTrustedBeneficiaryFlag(Boolean trustedBeneficiaryFlag) {
    this.trustedBeneficiaryFlag = trustedBeneficiaryFlag;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
}    ScaStatusResponse scaStatusResponse = (ScaStatusResponse) o;
    return Objects.equals(this.scaStatus, scaStatusResponse.scaStatus) &&
    Objects.equals(this.trustedBeneficiaryFlag, scaStatusResponse.trustedBeneficiaryFlag);
  }

  @Override
  public int hashCode() {
    return Objects.hash(scaStatus, trustedBeneficiaryFlag);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ScaStatusResponse {\n");

    sb.append("    scaStatus: ").append(toIndentedString(scaStatus)).append("\n");
    sb.append("    trustedBeneficiaryFlag: ").append(toIndentedString(trustedBeneficiaryFlag)).append("\n");
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

