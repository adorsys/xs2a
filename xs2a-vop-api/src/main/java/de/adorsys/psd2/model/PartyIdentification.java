package de.adorsys.psd2.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * See document \\\&quot;openFinance API Framework Data Dictionary\\\&quot;, section \\\&quot;Party Identification\\\&quot; for more details.
 */

@Schema(name = "partyIdentification", description = "See document \\\"openFinance API Framework Data Dictionary\\\", section \\\"Party Identification\\\" for more details. ")
@JsonTypeName("partyIdentification")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2025-09-18T08:00:00.499860948+01:00[Africa/Douala]", comments = "Generator version: 7.15.0")
public class PartyIdentification {

  private Optional<OrganisationIdentification> organisationId = Optional.empty();

  private Optional<PrivateIdentification> privateId = Optional.empty();

  public PartyIdentification organisationId(OrganisationIdentification organisationId) {
    this.organisationId = Optional.ofNullable(organisationId);
    return this;
  }

  /**
   * An entry provided by an external ISO code list
   * @return organisationId
   */
  @Valid
  @Schema(name = "organisationId", description = "An entry provided by an external ISO code list ", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("organisationId")
  public Optional<OrganisationIdentification> getOrganisationId() {
    return organisationId;
  }

  public void setOrganisationId(Optional<OrganisationIdentification> organisationId) {
    this.organisationId = organisationId;
  }

  public PartyIdentification privateId(PrivateIdentification privateId) {
    this.privateId = Optional.ofNullable(privateId);
    return this;
  }

  /**
   * A scheme name defined in a proprietary way.
   * @return privateId
   */
  @Valid
  @Schema(name = "privateId", description = "A scheme name defined in a proprietary way. ", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("privateId")
  public Optional<PrivateIdentification> getPrivateId() {
    return privateId;
  }

  public void setPrivateId(Optional<PrivateIdentification> privateId) {
    this.privateId = privateId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PartyIdentification partyIdentification = (PartyIdentification) o;
    return Objects.equals(this.organisationId, partyIdentification.organisationId) &&
        Objects.equals(this.privateId, partyIdentification.privateId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(organisationId, privateId);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class PartyIdentification {\n");
    sb.append("    organisationId: ").append(toIndentedString(organisationId)).append("\n");
    sb.append("    privateId: ").append(toIndentedString(privateId)).append("\n");
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

