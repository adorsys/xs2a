package de.adorsys.psd2.model;

import java.util.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import jakarta.annotation.Generated;

/**
 * GetBulkVerificationOfParty200Response
 */

@JsonTypeName("getBulkVerificationOfParty_200_response")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2025-09-18T08:00:00.499860948+01:00[Africa/Douala]", comments = "Generator version: 7.15.0")
public class GetBulkVerificationOfParty200Response {

  private UUID vopBulkId;

  private VOPBulkStatus vopBulkStatus;

  @Valid
  private List<@Valid PartyVerificationRequestAndResponse> partyVerificationRequestsAndResults = new ArrayList<>();

  public GetBulkVerificationOfParty200Response() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public GetBulkVerificationOfParty200Response(UUID vopBulkId, VOPBulkStatus vopBulkStatus, List<@Valid PartyVerificationRequestAndResponse> partyVerificationRequestsAndResults) {
    this.vopBulkId = vopBulkId;
    this.vopBulkStatus = vopBulkStatus;
    this.partyVerificationRequestsAndResults = partyVerificationRequestsAndResults;
  }

  public GetBulkVerificationOfParty200Response vopBulkId(UUID vopBulkId) {
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

  public GetBulkVerificationOfParty200Response vopBulkStatus(VOPBulkStatus vopBulkStatus) {
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

  public GetBulkVerificationOfParty200Response partyVerificationRequestsAndResults(List<@Valid PartyVerificationRequestAndResponse> partyVerificationRequestsAndResults) {
    this.partyVerificationRequestsAndResults = partyVerificationRequestsAndResults;
    return this;
  }

  public GetBulkVerificationOfParty200Response addPartyVerificationRequestsAndResultsItem(PartyVerificationRequestAndResponse partyVerificationRequestsAndResultsItem) {
    if (this.partyVerificationRequestsAndResults == null) {
      this.partyVerificationRequestsAndResults = new ArrayList<>();
    }
    this.partyVerificationRequestsAndResults.add(partyVerificationRequestsAndResultsItem);
    return this;
  }

  /**
   * A non-empty array of related party verification results.
   * @return partyVerificationRequestsAndResults
   */
  @NotNull @Valid @Size(min = 1)
  @Schema(name = "partyVerificationRequestsAndResults", description = "A non-empty array of related party verification results. ", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("partyVerificationRequestsAndResults")
  public List<@Valid PartyVerificationRequestAndResponse> getPartyVerificationRequestsAndResults() {
    return partyVerificationRequestsAndResults;
  }

  public void setPartyVerificationRequestsAndResults(List<@Valid PartyVerificationRequestAndResponse> partyVerificationRequestsAndResults) {
    this.partyVerificationRequestsAndResults = partyVerificationRequestsAndResults;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    GetBulkVerificationOfParty200Response getBulkVerificationOfParty200Response = (GetBulkVerificationOfParty200Response) o;
    return Objects.equals(this.vopBulkId, getBulkVerificationOfParty200Response.vopBulkId) &&
        Objects.equals(this.vopBulkStatus, getBulkVerificationOfParty200Response.vopBulkStatus) &&
        Objects.equals(this.partyVerificationRequestsAndResults, getBulkVerificationOfParty200Response.partyVerificationRequestsAndResults);
  }

  @Override
  public int hashCode() {
    return Objects.hash(vopBulkId, vopBulkStatus, partyVerificationRequestsAndResults);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class GetBulkVerificationOfParty200Response {\n");
    sb.append("    vopBulkId: ").append(toIndentedString(vopBulkId)).append("\n");
    sb.append("    vopBulkStatus: ").append(toIndentedString(vopBulkStatus)).append("\n");
    sb.append("    partyVerificationRequestsAndResults: ").append(toIndentedString(partyVerificationRequestsAndResults)).append("\n");
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

