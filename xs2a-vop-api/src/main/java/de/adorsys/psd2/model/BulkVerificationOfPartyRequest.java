package de.adorsys.psd2.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * BulkVerificationOfPartyRequest
 */

@JsonTypeName("bulkVerificationOfParty_request")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2025-09-18T08:00:00.499860948+01:00[Africa/Douala]", comments = "Generator version: 7.15.0")
public class BulkVerificationOfPartyRequest {

  @Valid
  private List<@Valid PartyVerificationRequest> partyVerificationRequests = new ArrayList<>();

  private Optional<AgentDescription> requestingAgent = Optional.empty();

  public BulkVerificationOfPartyRequest() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public BulkVerificationOfPartyRequest(List<@Valid PartyVerificationRequest> partyVerificationRequests) {
    this.partyVerificationRequests = partyVerificationRequests;
  }

  public BulkVerificationOfPartyRequest partyVerificationRequests(List<@Valid PartyVerificationRequest> partyVerificationRequests) {
    this.partyVerificationRequests = partyVerificationRequests;
    return this;
  }

  public BulkVerificationOfPartyRequest addPartyVerificationRequestsItem(PartyVerificationRequest partyVerificationRequestsItem) {
    if (this.partyVerificationRequests == null) {
      this.partyVerificationRequests = new ArrayList<>();
    }
    this.partyVerificationRequests.add(partyVerificationRequestsItem);
    return this;
  }

  /**
   * A non-empty bulk of related party verification entries.
   * @return partyVerificationRequests
   */
  @NotNull @Valid @Size(min = 1)
  @Schema(name = "partyVerificationRequests", description = "A non-empty bulk of related party verification entries. ", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("partyVerificationRequests")
  public List<@Valid PartyVerificationRequest> getPartyVerificationRequests() {
    return partyVerificationRequests;
  }

  public void setPartyVerificationRequests(List<@Valid PartyVerificationRequest> partyVerificationRequests) {
    this.partyVerificationRequests = partyVerificationRequests;
  }

  public BulkVerificationOfPartyRequest requestingAgent(AgentDescription requestingAgent) {
    this.requestingAgent = Optional.ofNullable(requestingAgent);
    return this;
  }

  /**
   * Only applicable in the inter-bank/inter-PSP sphere.
   * @return requestingAgent
   */
  @Valid
  @Schema(name = "requestingAgent", description = "Only applicable in the inter-bank/inter-PSP sphere. ", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("requestingAgent")
  public Optional<AgentDescription> getRequestingAgent() {
    return requestingAgent;
  }

  public void setRequestingAgent(Optional<AgentDescription> requestingAgent) {
    this.requestingAgent = requestingAgent;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    BulkVerificationOfPartyRequest bulkVerificationOfPartyRequest = (BulkVerificationOfPartyRequest) o;
    return Objects.equals(this.partyVerificationRequests, bulkVerificationOfPartyRequest.partyVerificationRequests) &&
        Objects.equals(this.requestingAgent, bulkVerificationOfPartyRequest.requestingAgent);
  }

  @Override
  public int hashCode() {
    return Objects.hash(partyVerificationRequests, requestingAgent);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class BulkVerificationOfPartyRequest {\n");
    sb.append("    partyVerificationRequests: ").append(toIndentedString(partyVerificationRequests)).append("\n");
    sb.append("    requestingAgent: ").append(toIndentedString(requestingAgent)).append("\n");
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

