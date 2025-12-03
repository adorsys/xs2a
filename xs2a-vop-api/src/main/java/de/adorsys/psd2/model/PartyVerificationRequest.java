package de.adorsys.psd2.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * See document \\\&quot;openFinance API Framework Extended Services Verification of Party (VOP) \\\&quot;,  section \\\&quot;Party Verification Request\\\&quot; for more details.
 */

@Schema(name = "partyVerificationRequest", description = "See document \\\"openFinance API Framework Extended Services Verification of Party (VOP) \\\",  section \\\"Party Verification Request\\\" for more details. ")
@JsonTypeName("partyVerificationRequest")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2025-09-18T08:00:00.499860948+01:00[Africa/Douala]", comments = "Generator version: 7.15.0")
public class PartyVerificationRequest {

  private UUID uetr;

  private AccountReference partyAccount;

  private Optional<AgentDescription> partyAgent = Optional.empty();

  private PartyDescription4 party;

  public PartyVerificationRequest() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public PartyVerificationRequest(UUID uetr, AccountReference partyAccount, PartyDescription4 party) {
    this.uetr = uetr;
    this.partyAccount = partyAccount;
    this.party = party;
  }

  public PartyVerificationRequest uetr(UUID uetr) {
    this.uetr = uetr;
    return this;
  }

  /**
   * Identifies this party identification request.
   * @return uetr
   */
  @NotNull @Valid
  @Schema(name = "uetr", example = "99435c7e-ad88-49ec-a2ad-99ddcb1f5555", description = "Identifies this party identification request. ", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("uetr")
  public UUID getUetr() {
    return uetr;
  }

  public void setUetr(UUID uetr) {
    this.uetr = uetr;
  }

  public PartyVerificationRequest partyAccount(AccountReference partyAccount) {
    this.partyAccount = partyAccount;
    return this;
  }

  /**
   * The data type might be restricted by the ASPSP to e.g. \"iban\" use only, which would be Account Reference2 as data type.
   * @return partyAccount
   */
  @NotNull @Valid
  @Schema(name = "partyAccount", description = "The data type might be restricted by the ASPSP to e.g. \"iban\" use only, which would be Account Reference2 as data type. ", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("partyAccount")
  public AccountReference getPartyAccount() {
    return partyAccount;
  }

  public void setPartyAccount(AccountReference partyAccount) {
    this.partyAccount = partyAccount;
  }

  public PartyVerificationRequest partyAgent(AgentDescription partyAgent) {
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

  public PartyVerificationRequest party(PartyDescription4 party) {
    this.party = party;
    return this;
  }

  /**
   * This structure shall contain the party name and/or a  party identifier,  Related API Access Schemes might define further requirements.
   * @return party
   */
  @NotNull @Valid
  @Schema(name = "party", description = "This structure shall contain the party name and/or a  party identifier,  Related API Access Schemes might define further requirements.                     ", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("party")
  public PartyDescription4 getParty() {
    return party;
  }

  public void setParty(PartyDescription4 party) {
    this.party = party;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PartyVerificationRequest partyVerificationRequest = (PartyVerificationRequest) o;
    return Objects.equals(this.uetr, partyVerificationRequest.uetr) &&
        Objects.equals(this.partyAccount, partyVerificationRequest.partyAccount) &&
        Objects.equals(this.partyAgent, partyVerificationRequest.partyAgent) &&
        Objects.equals(this.party, partyVerificationRequest.party);
  }

  @Override
  public int hashCode() {
    return Objects.hash(uetr, partyAccount, partyAgent, party);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class PartyVerificationRequest {\n");
    sb.append("    uetr: ").append(toIndentedString(uetr)).append("\n");
    sb.append("    partyAccount: ").append(toIndentedString(partyAccount)).append("\n");
    sb.append("    partyAgent: ").append(toIndentedString(partyAgent)).append("\n");
    sb.append("    party: ").append(toIndentedString(party)).append("\n");
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

