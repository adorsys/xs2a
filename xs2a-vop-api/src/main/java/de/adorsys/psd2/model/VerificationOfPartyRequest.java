package de.adorsys.psd2.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * VerificationOfPartyRequest
 */

@JsonTypeName("verificationOfParty_request")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2025-09-18T08:00:00.499860948+01:00[Africa/Douala]", comments = "Generator version: 7.15.0")
public class VerificationOfPartyRequest {

  private AccountReference partyAccount;

  private Optional<AgentDescription> partyAgent = Optional.empty();

  private PartyDescription4 party;

  @Valid
  private List<@Size(max = 140)String> unstructuredRemittanceInformation = new ArrayList<>();

  private Optional<AgentDescription> requestingAgent = Optional.empty();

  public VerificationOfPartyRequest() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public VerificationOfPartyRequest(AccountReference partyAccount, PartyDescription4 party) {
    this.partyAccount = partyAccount;
    this.party = party;
  }

  public VerificationOfPartyRequest partyAccount(AccountReference partyAccount) {
    this.partyAccount = partyAccount;
    return this;
  }

  /**
   * The data type might be restricted by the ASPSP to e.g. \"iban\" use only, which would be Account Reference2 as data type. NOTE; If IBAN or BBAN is not sufficient to identify the related account, e.g. an electronic money account, then the proxy attribut-eremittance information should be used in addition to fully identify the account.
   * @return partyAccount
   */
  @NotNull @Valid
  @Schema(name = "partyAccount", description = "The data type might be restricted by the ASPSP to e.g. \"iban\" use only, which would be Account Reference2 as data type. NOTE; If IBAN or BBAN is not sufficient to identify the related account, e.g. an electronic money account, then the proxy attribut-eremittance information should be used in addition to fully identify the account. ", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("partyAccount")
  public AccountReference getPartyAccount() {
    return partyAccount;
  }

  public void setPartyAccount(AccountReference partyAccount) {
    this.partyAccount = partyAccount;
  }

  public VerificationOfPartyRequest partyAgent(AgentDescription partyAgent) {
    this.partyAgent = Optional.ofNullable(partyAgent);
    return this;
  }

  /**
   *
   * @return partyAgent
   */
  @Valid
  @Schema(name = "partyAgent", description = "", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("partyAgent")
  public Optional<AgentDescription> getPartyAgent() {
    return partyAgent;
  }

  public void setPartyAgent(Optional<AgentDescription> partyAgent) {
    this.partyAgent = partyAgent;
  }

  public VerificationOfPartyRequest party(PartyDescription4 party) {
    this.party = party;
    return this;
  }

  /**
   * This structure shall contain the party name and/or a  party identifier,  Related API Access Schemes might define further requirements/restrictions, e.g.  - Only support the usage of either name or identifica-tion. - Or only support organisa-tion identification
   * @return party
   */
  @NotNull @Valid
  @Schema(name = "party", description = "This structure shall contain the party name and/or a  party identifier,  Related API Access Schemes might define further requirements/restrictions, e.g.  - Only support the usage of either name or identifica-tion. - Or only support organisa-tion identification ", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("party")
  public PartyDescription4 getParty() {
    return party;
  }

  public void setParty(PartyDescription4 party) {
    this.party = party;
  }

  public VerificationOfPartyRequest unstructuredRemittanceInformation(List<@Size(max = 140)String> unstructuredRemittanceInformation) {
    this.unstructuredRemittanceInformation = unstructuredRemittanceInformation;
    return this;
  }

  public VerificationOfPartyRequest addUnstructuredRemittanceInformationItem(String unstructuredRemittanceInformationItem) {
    if (this.unstructuredRemittanceInformation == null) {
      this.unstructuredRemittanceInformation = new ArrayList<>();
    }
    this.unstructuredRemittanceInformation.add(unstructuredRemittanceInformationItem);
    return this;
  }

  /**
   * This attribute may contain data to refer to electronic money ac-counts.
   * @return unstructuredRemittanceInformation
   */

  @Schema(name = "unstructuredRemittanceInformation", description = "This attribute may contain data to refer to electronic money ac-counts.  ", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("unstructuredRemittanceInformation")
  public List<@Size(max = 140)String> getUnstructuredRemittanceInformation() {
    return unstructuredRemittanceInformation;
  }

  public void setUnstructuredRemittanceInformation(List<@Size(max = 140)String> unstructuredRemittanceInformation) {
    this.unstructuredRemittanceInformation = unstructuredRemittanceInformation;
  }

  public VerificationOfPartyRequest requestingAgent(AgentDescription requestingAgent) {
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
    VerificationOfPartyRequest verificationOfPartyRequest = (VerificationOfPartyRequest) o;
    return Objects.equals(this.partyAccount, verificationOfPartyRequest.partyAccount) &&
        Objects.equals(this.partyAgent, verificationOfPartyRequest.partyAgent) &&
        Objects.equals(this.party, verificationOfPartyRequest.party) &&
        Objects.equals(this.unstructuredRemittanceInformation, verificationOfPartyRequest.unstructuredRemittanceInformation) &&
        Objects.equals(this.requestingAgent, verificationOfPartyRequest.requestingAgent);
  }

  @Override
  public int hashCode() {
    return Objects.hash(partyAccount, partyAgent, party, unstructuredRemittanceInformation, requestingAgent);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class VerificationOfPartyRequest {\n");
    sb.append("    partyAccount: ").append(toIndentedString(partyAccount)).append("\n");
    sb.append("    partyAgent: ").append(toIndentedString(partyAgent)).append("\n");
    sb.append("    party: ").append(toIndentedString(party)).append("\n");
    sb.append("    unstructuredRemittanceInformation: ").append(toIndentedString(unstructuredRemittanceInformation)).append("\n");
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

