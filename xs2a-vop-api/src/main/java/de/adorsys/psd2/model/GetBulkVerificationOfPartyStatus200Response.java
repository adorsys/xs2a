package de.adorsys.psd2.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * GetBulkVerificationOfPartyStatus200Response
 */

@JsonTypeName("getBulkVerificationOfPartyStatus_200_response")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2025-09-18T08:00:00.499860948+01:00[Africa/Douala]", comments = "Generator version: 7.15.0")
public class GetBulkVerificationOfPartyStatus200Response {

  private UUID vopBulkId;

  private VOPBulkStatus vopBulkStatus;

  private Optional<StatusReasonCode> reasonCode = Optional.empty();

  private Optional<@Size(max = 35) String> reasonProprietary = Optional.empty();

  public GetBulkVerificationOfPartyStatus200Response() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public GetBulkVerificationOfPartyStatus200Response(UUID vopBulkId, VOPBulkStatus vopBulkStatus) {
    this.vopBulkId = vopBulkId;
    this.vopBulkStatus = vopBulkStatus;
  }

  public GetBulkVerificationOfPartyStatus200Response vopBulkId(UUID vopBulkId) {
    this.vopBulkId = vopBulkId;
    return this;
  }

  /**
   * Technical resource identification of the VOP bulk.
   * @return vopBulkId
   */
  @NotNull @Valid
  @Schema(name = "vopBulkId", example = "99435c7e-ad88-49ec-a2ad-99ddcb1f5555", description = "Technical resource identification of the VOP bulk. ", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("vopBulkId")
  public UUID getVopBulkId() {
    return vopBulkId;
  }

  public void setVopBulkId(UUID vopBulkId) {
    this.vopBulkId = vopBulkId;
  }

  public GetBulkVerificationOfPartyStatus200Response vopBulkStatus(VOPBulkStatus vopBulkStatus) {
    this.vopBulkStatus = vopBulkStatus;
    return this;
  }

  /**
   *
   * @return vopBulkStatus
   */
  @NotNull @Valid
  @Schema(name = "vopBulkStatus", description = "", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("vopBulkStatus")
  public VOPBulkStatus getVopBulkStatus() {
    return vopBulkStatus;
  }

  public void setVopBulkStatus(VOPBulkStatus vopBulkStatus) {
    this.vopBulkStatus = vopBulkStatus;
  }

  public GetBulkVerificationOfPartyStatus200Response reasonCode(StatusReasonCode reasonCode) {
    this.reasonCode = Optional.ofNullable(reasonCode);
    return this;
  }

  /**
   *
   * @return reasonCode
   */
  @Valid
  @Schema(name = "reasonCode", description = "", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("reasonCode")
  public Optional<StatusReasonCode> getReasonCode() {
    return reasonCode;
  }

  public void setReasonCode(Optional<StatusReasonCode> reasonCode) {
    this.reasonCode = reasonCode;
  }

  public GetBulkVerificationOfPartyStatus200Response reasonProprietary(String reasonProprietary) {
    this.reasonProprietary = Optional.ofNullable(reasonProprietary);
    return this;
  }

  /**
   *
   * @return reasonProprietary
   */

  @Schema(name = "reasonProprietary", example = "Text, maximum of 35 characters.", description = "", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("reasonProprietary")
  public Optional<@Size(max = 35) String> getReasonProprietary() {
    return reasonProprietary;
  }

  public void setReasonProprietary(Optional<String> reasonProprietary) {
    this.reasonProprietary = reasonProprietary;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    GetBulkVerificationOfPartyStatus200Response getBulkVerificationOfPartyStatus200Response = (GetBulkVerificationOfPartyStatus200Response) o;
    return Objects.equals(this.vopBulkId, getBulkVerificationOfPartyStatus200Response.vopBulkId) &&
        Objects.equals(this.vopBulkStatus, getBulkVerificationOfPartyStatus200Response.vopBulkStatus) &&
        Objects.equals(this.reasonCode, getBulkVerificationOfPartyStatus200Response.reasonCode) &&
        Objects.equals(this.reasonProprietary, getBulkVerificationOfPartyStatus200Response.reasonProprietary);
  }

  @Override
  public int hashCode() {
    return Objects.hash(vopBulkId, vopBulkStatus, reasonCode, reasonProprietary);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class GetBulkVerificationOfPartyStatus200Response {\n");
    sb.append("    vopBulkId: ").append(toIndentedString(vopBulkId)).append("\n");
    sb.append("    vopBulkStatus: ").append(toIndentedString(vopBulkStatus)).append("\n");
    sb.append("    reasonCode: ").append(toIndentedString(reasonCode)).append("\n");
    sb.append("    reasonProprietary: ").append(toIndentedString(reasonProprietary)).append("\n");
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

