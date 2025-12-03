package de.adorsys.psd2.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * See document \\\&quot;openFinance API Framework Extended Services Verification of Party (VOP) \\\&quot;,  section \\\&quot;Party Verification Response\\\&quot; for more details.
 */

@Schema(name = "partyVerificationResponse", description = "See document \\\"openFinance API Framework Extended Services Verification of Party (VOP) \\\",  section \\\"Party Verification Response\\\" for more details. ")
@JsonTypeName("partyVerificationResponse")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2025-09-18T08:00:00.499860948+01:00[Africa/Douala]", comments = "Generator version: 7.15.0")
public class PartyVerificationResponse {

  private UUID uetr;

  private Optional<PartyNameMatchCode> partyNameMatch = Optional.empty();

  private Optional<PartyIdentificationMatchCode> partyIdMatch = Optional.empty();

  private Optional<@Size(max = 140) String> matchedName = Optional.empty();

  private Optional<@Size(max = 500) String> additionalInformation = Optional.empty();

  public PartyVerificationResponse() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public PartyVerificationResponse(UUID uetr) {
    this.uetr = uetr;
  }

  public PartyVerificationResponse uetr(UUID uetr) {
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

  public PartyVerificationResponse partyNameMatch(PartyNameMatchCode partyNameMatch) {
    this.partyNameMatch = Optional.ofNullable(partyNameMatch);
    return this;
  }

  /**
   * Only provided, if a party name has been provided.
   * @return partyNameMatch
   */
  @Valid
  @Schema(name = "partyNameMatch", description = "Only provided, if a party name has been provided. ", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("partyNameMatch")
  public Optional<PartyNameMatchCode> getPartyNameMatch() {
    return partyNameMatch;
  }

  public void setPartyNameMatch(Optional<PartyNameMatchCode> partyNameMatch) {
    this.partyNameMatch = partyNameMatch;
  }

  public PartyVerificationResponse partyIdMatch(PartyIdentificationMatchCode partyIdMatch) {
    this.partyIdMatch = Optional.ofNullable(partyIdMatch);
    return this;
  }

  /**
   * Only provided, if a party identification has been provided.
   * @return partyIdMatch
   */
  @Valid
  @Schema(name = "partyIdMatch", description = "Only provided, if a party identification has been provided. ", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("partyIdMatch")
  public Optional<PartyIdentificationMatchCode> getPartyIdMatch() {
    return partyIdMatch;
  }

  public void setPartyIdMatch(Optional<PartyIdentificationMatchCode> partyIdMatch) {
    this.partyIdMatch = partyIdMatch;
  }

  public PartyVerificationResponse matchedName(String matchedName) {
    this.matchedName = Optional.ofNullable(matchedName);
    return this;
  }

  /**
   * This attribute might be provided in case of a near match and contains the name as provided by the addressed party agent.
   * @return matchedName
   */

  @Schema(name = "matchedName", example = "Text, maximum of 140 characters.", description = "This attribute might be provided in case of a near match and contains the name as provided by the addressed party agent. ", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("matchedName")
  public Optional<@Size(max = 140) String> getMatchedName() {
    return matchedName;
  }

  public void setMatchedName(Optional<String> matchedName) {
    this.matchedName = matchedName;
  }

  public PartyVerificationResponse additionalInformation(String additionalInformation) {
    this.additionalInformation = Optional.ofNullable(additionalInformation);
    return this;
  }

  /**
   * Should be human readable text.
   * @return additionalInformation
   */

  @Schema(name = "additionalInformation", example = "Text, maximum of 500 characters.", description = "Should be human readable text. ", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("additionalInformation")
  public Optional<@Size(max = 500) String> getAdditionalInformation() {
    return additionalInformation;
  }

  public void setAdditionalInformation(Optional<String> additionalInformation) {
    this.additionalInformation = additionalInformation;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PartyVerificationResponse partyVerificationResponse = (PartyVerificationResponse) o;
    return Objects.equals(this.uetr, partyVerificationResponse.uetr) &&
        Objects.equals(this.partyNameMatch, partyVerificationResponse.partyNameMatch) &&
        Objects.equals(this.partyIdMatch, partyVerificationResponse.partyIdMatch) &&
        Objects.equals(this.matchedName, partyVerificationResponse.matchedName) &&
        Objects.equals(this.additionalInformation, partyVerificationResponse.additionalInformation);
  }

  @Override
  public int hashCode() {
    return Objects.hash(uetr, partyNameMatch, partyIdMatch, matchedName, additionalInformation);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class PartyVerificationResponse {\n");
    sb.append("    uetr: ").append(toIndentedString(uetr)).append("\n");
    sb.append("    partyNameMatch: ").append(toIndentedString(partyNameMatch)).append("\n");
    sb.append("    partyIdMatch: ").append(toIndentedString(partyIdMatch)).append("\n");
    sb.append("    matchedName: ").append(toIndentedString(matchedName)).append("\n");
    sb.append("    additionalInformation: ").append(toIndentedString(additionalInformation)).append("\n");
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

