package de.adorsys.psd2.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * See document \\\&quot;openFinance API Framework Data Dictionary\\\&quot;, section \\\&quot;Party Description4\\\&quot; for more details.
 */

@Schema(name = "partyDescription4", description = "See document \\\"openFinance API Framework Data Dictionary\\\", section \\\"Party Description4\\\" for more details. ")
@JsonTypeName("partyDescription4")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2025-09-18T08:00:00.499860948+01:00[Africa/Douala]", comments = "Generator version: 7.15.0")
public class PartyDescription4 {

  private Optional<@Size(max = 140) String> name = Optional.empty();

  private Optional<PartyIdentification> identification = Optional.empty();

  public PartyDescription4 name(String name) {
    this.name = Optional.ofNullable(name);
    return this;
  }

  /**
   * Name of the party.
   * @return name
   */

  @Schema(name = "name", example = "Text, maximum of 140 characters.", description = "Name of the party. ", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("name")
  public Optional<@Size(max = 140) String> getName() {
    return name;
  }

  public void setName(Optional<String> name) {
    this.name = name;
  }

  public PartyDescription4 identification(PartyIdentification identification) {
    this.identification = Optional.ofNullable(identification);
    return this;
  }

  /**
   * Identification of the party.
   * @return identification
   */
  @Valid
  @Schema(name = "identification", description = "Identification of the party. ", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("identification")
  public Optional<PartyIdentification> getIdentification() {
    return identification;
  }

  public void setIdentification(Optional<PartyIdentification> identification) {
    this.identification = identification;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PartyDescription4 partyDescription4 = (PartyDescription4) o;
    return Objects.equals(this.name, partyDescription4.name) &&
        Objects.equals(this.identification, partyDescription4.identification);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, identification);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class PartyDescription4 {\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    identification: ").append(toIndentedString(identification)).append("\n");
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

