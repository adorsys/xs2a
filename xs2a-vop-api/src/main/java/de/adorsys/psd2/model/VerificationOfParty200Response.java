package de.adorsys.psd2.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import jakarta.annotation.Generated;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;

/**
 * VerificationOfParty200Response
 */

@JsonTypeName("verificationOfParty_200_response")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2025-09-18T08:00:00.499860948+01:00[Africa/Douala]", comments = "Generator version: 7.15.0")
public class VerificationOfParty200Response {

  private Optional<PartyNameMatchCode> partyNameMatch = Optional.empty();

  private Optional<PartyIdentificationMatchCode> partyIdMatch = Optional.empty();

  private Optional<@Size(max = 140) String> matchedName = Optional.empty();

  private Optional<@Size(max = 500) String> additionalInformation = Optional.empty();

  public VerificationOfParty200Response partyNameMatch(PartyNameMatchCode partyNameMatch) {
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

  public VerificationOfParty200Response partyIdMatch(PartyIdentificationMatchCode partyIdMatch) {
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

  public VerificationOfParty200Response matchedName(String matchedName) {
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

  public VerificationOfParty200Response additionalInformation(String additionalInformation) {
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
    VerificationOfParty200Response verificationOfParty200Response = (VerificationOfParty200Response) o;
    return Objects.equals(this.partyNameMatch, verificationOfParty200Response.partyNameMatch) &&
        Objects.equals(this.partyIdMatch, verificationOfParty200Response.partyIdMatch) &&
        Objects.equals(this.matchedName, verificationOfParty200Response.matchedName) &&
        Objects.equals(this.additionalInformation, verificationOfParty200Response.additionalInformation);
  }

  @Override
  public int hashCode() {
    return Objects.hash(partyNameMatch, partyIdMatch, matchedName, additionalInformation);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class VerificationOfParty200Response {\n");
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

