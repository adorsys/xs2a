package de.adorsys.psd2.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * BulkVerificationOfParty200Response
 */

@JsonTypeName("bulkVerificationOfParty_200_response")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2025-09-18T08:00:00.499860948+01:00[Africa/Douala]", comments = "Generator version: 7.15.0")
public class BulkVerificationOfParty200Response {

  private VOPBulkStatus vopBulkStatus;

  @Valid
  private List<@Valid PartyVerificationResponse> partyVerificationResponses = new ArrayList<>();

  public BulkVerificationOfParty200Response() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public BulkVerificationOfParty200Response(VOPBulkStatus vopBulkStatus) {
    this.vopBulkStatus = vopBulkStatus;
  }

  public BulkVerificationOfParty200Response vopBulkStatus(VOPBulkStatus vopBulkStatus) {
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

  public BulkVerificationOfParty200Response partyVerificationResponses(List<@Valid PartyVerificationResponse> partyVerificationResponses) {
    this.partyVerificationResponses = partyVerificationResponses;
    return this;
  }

  public BulkVerificationOfParty200Response addPartyVerificationResponsesItem(PartyVerificationResponse partyVerificationResponsesItem) {
    if (this.partyVerificationResponses == null) {
      this.partyVerificationResponses = new ArrayList<>();
    }
    this.partyVerificationResponses.add(partyVerificationResponsesItem);
    return this;
  }

  /**
   * A non-empty array of related party verification results.
   * @return partyVerificationResponses
   */
  @Valid @Size(min = 1)
  @Schema(name = "partyVerificationResponses", description = "A non-empty array of related party verification results. ", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("partyVerificationResponses")
  public List<@Valid PartyVerificationResponse> getPartyVerificationResponses() {
    return partyVerificationResponses;
  }

  public void setPartyVerificationResponses(List<@Valid PartyVerificationResponse> partyVerificationResponses) {
    this.partyVerificationResponses = partyVerificationResponses;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    BulkVerificationOfParty200Response bulkVerificationOfParty200Response = (BulkVerificationOfParty200Response) o;
    return Objects.equals(this.vopBulkStatus, bulkVerificationOfParty200Response.vopBulkStatus) &&
        Objects.equals(this.partyVerificationResponses, bulkVerificationOfParty200Response.partyVerificationResponses);
  }

  @Override
  public int hashCode() {
    return Objects.hash(vopBulkStatus, partyVerificationResponses);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class BulkVerificationOfParty200Response {\n");
    sb.append("    vopBulkStatus: ").append(toIndentedString(vopBulkStatus)).append("\n");
    sb.append("    partyVerificationResponses: ").append(toIndentedString(partyVerificationResponses)).append("\n");
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

