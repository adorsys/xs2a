package de.adorsys.psd2.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Body of the JSON response with SCA Status.
 */
@ApiModel(description = "Body of the JSON response with SCA Status.")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2021-05-24T13:41:46.273636+03:00[Europe/Kiev]")

public class ScaStatusResponse   {
  @JsonProperty("scaStatus")
  private ScaStatus scaStatus = null;

  @JsonProperty("psuMessage")
  private String psuMessage = null;

  @JsonProperty("trustedBeneficiaryFlag")
  private Boolean trustedBeneficiaryFlag = null;

  @JsonProperty("_links")
  private Map _links = null;

  @JsonProperty("tppMessage")
  @Valid
  private List<TppMessageGeneric> tppMessage = null;

  public ScaStatusResponse scaStatus(ScaStatus scaStatus) {
    this.scaStatus = scaStatus;
    return this;
  }

  /**
   * Get scaStatus
   * @return scaStatus
  **/
  @ApiModelProperty(required = true, value = "")
  @NotNull

  @Valid


  @JsonProperty("scaStatus")
  public ScaStatus getScaStatus() {
    return scaStatus;
  }

  public void setScaStatus(ScaStatus scaStatus) {
    this.scaStatus = scaStatus;
  }

  public ScaStatusResponse psuMessage(String psuMessage) {
    this.psuMessage = psuMessage;
    return this;
  }

  /**
   * Get psuMessage
   * @return psuMessage
  **/
  @ApiModelProperty(value = "")

@Size(max=500)

  @JsonProperty("psuMessage")
  public String getPsuMessage() {
    return psuMessage;
  }

  public void setPsuMessage(String psuMessage) {
    this.psuMessage = psuMessage;
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

  public ScaStatusResponse _links(Map _links) {
    this._links = _links;
    return this;
  }

  /**
   * Get _links
   * @return _links
  **/
  @ApiModelProperty(value = "")

  @Valid


  @JsonProperty("_links")
  public Map getLinks() {
    return _links;
  }

  public void setLinks(Map _links) {
    this._links = _links;
  }

  public ScaStatusResponse tppMessage(List<TppMessageGeneric> tppMessage) {
    this.tppMessage = tppMessage;
    return this;
  }

  public ScaStatusResponse addTppMessageItem(TppMessageGeneric tppMessageItem) {
    if (this.tppMessage == null) {
      this.tppMessage = new ArrayList<>();
    }
    this.tppMessage.add(tppMessageItem);
    return this;
  }

  /**
   * Messages to the TPP on operational issues.
   * @return tppMessage
  **/
  @ApiModelProperty(value = "Messages to the TPP on operational issues.")

  @Valid


  @JsonProperty("tppMessage")
  public List<TppMessageGeneric> getTppMessage() {
    return tppMessage;
  }

  public void setTppMessage(List<TppMessageGeneric> tppMessage) {
    this.tppMessage = tppMessage;
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
    Objects.equals(this.psuMessage, scaStatusResponse.psuMessage) &&
    Objects.equals(this.trustedBeneficiaryFlag, scaStatusResponse.trustedBeneficiaryFlag) &&
    Objects.equals(this._links, scaStatusResponse._links) &&
    Objects.equals(this.tppMessage, scaStatusResponse.tppMessage);
  }

  @Override
  public int hashCode() {
    return Objects.hash(scaStatus, psuMessage, trustedBeneficiaryFlag, _links, tppMessage);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ScaStatusResponse {\n");

    sb.append("    scaStatus: ").append(toIndentedString(scaStatus)).append("\n");
    sb.append("    psuMessage: ").append(toIndentedString(psuMessage)).append("\n");
    sb.append("    trustedBeneficiaryFlag: ").append(toIndentedString(trustedBeneficiaryFlag)).append("\n");
    sb.append("    _links: ").append(toIndentedString(_links)).append("\n");
    sb.append("    tppMessage: ").append(toIndentedString(tppMessage)).append("\n");
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

