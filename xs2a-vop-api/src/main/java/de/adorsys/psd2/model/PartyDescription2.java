package de.adorsys.psd2.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * See document \\\&quot;openFinance API Framework Data Dictionary\\\&quot;, section \\\&quot;Party Description2\\\&quot; for more details.
 */

@Schema(name = "partyDescription2", description = "See document \\\"openFinance API Framework Data Dictionary\\\", section \\\"Party Description2\\\" for more details. ")
@JsonTypeName("partyDescription2")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2025-09-18T08:00:00.499860948+01:00[Africa/Douala]", comments = "Generator version: 7.15.0")
public class PartyDescription2 {

  private Optional<@Size(max = 140) String> name = Optional.empty();

  private Optional<PostalAddress> postaladdress = Optional.empty();

  public PartyDescription2 name(String name) {
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

  public PartyDescription2 postaladdress(PostalAddress postaladdress) {
    this.postaladdress = Optional.ofNullable(postaladdress);
    return this;
  }

  /**
   * Postal Address of the party.
   * @return postaladdress
   */
  @Valid
  @Schema(name = "postaladdress", description = "Postal Address of the party. ", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("postaladdress")
  public Optional<PostalAddress> getPostaladdress() {
    return postaladdress;
  }

  public void setPostaladdress(Optional<PostalAddress> postaladdress) {
    this.postaladdress = postaladdress;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PartyDescription2 partyDescription2 = (PartyDescription2) o;
    return Objects.equals(this.name, partyDescription2.name) &&
        Objects.equals(this.postaladdress, partyDescription2.postaladdress);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, postaladdress);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class PartyDescription2 {\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    postaladdress: ").append(toIndentedString(postaladdress)).append("\n");
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

