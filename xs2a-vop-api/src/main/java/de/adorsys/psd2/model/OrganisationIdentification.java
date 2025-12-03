package de.adorsys.psd2.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * See document \\\&quot;openFinance API Framework Data Dictionary\\\&quot;, section \\\&quot;Organisation Identification\\\&quot; for more details.
 */

@Schema(name = "organisationIdentification", description = "See document \\\"openFinance API Framework Data Dictionary\\\", section \\\"Organisation Identification\\\" for more details. ")
@JsonTypeName("organisationIdentification")
@SuppressWarnings("PMD")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2025-09-18T08:00:00.499860948+01:00[Africa/Douala]", comments = "Generator version: 7.15.0")
public class OrganisationIdentification {

  private Optional<@Pattern(regexp = "[A-Z]{6,6}[A-Z2-9][A-NP-Z0-9]([A-Z0-9]{3,3}){0,1}") String> anyBIC = Optional.empty();

  private Optional<@Pattern(regexp = "[A-Z0-9]{18,18}[0-9]{2,2}") String> lei = Optional.empty();

  @Valid
  private List<@Valid GenericOrganisationIdentification> others = new ArrayList<>();

  public OrganisationIdentification anyBIC(String anyBIC) {
    this.anyBIC = Optional.ofNullable(anyBIC);
    return this;
  }

  /**
   * A code allocated to a business entity or to a financial institution by a Registration Authority under an international identification scheme.
   * @return anyBIC
   */

  @Schema(name = "anyBIC", example = "ECBFDEFFFIM", description = "A code allocated to a business entity or to a financial institution by a Registration Authority under an international identification scheme. ", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("anyBIC")
  public Optional<@Pattern(regexp = "[A-Z]{6,6}[A-Z2-9][A-NP-Z0-9]([A-Z0-9]{3,3}){0,1}") String> getAnyBIC() {
    return anyBIC;
  }

  public void setAnyBIC(Optional<String> anyBIC) {
    this.anyBIC = anyBIC;
  }

  public OrganisationIdentification lei(String lei) {
    this.lei = Optional.ofNullable(lei);
    return this;
  }

  /**
   * Legal Entity Identifier.
   * @return lei
   */

  @Schema(name = "lei", example = "549300DTUYXVMJXZNY75", description = "Legal Entity Identifier. ", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("lei")
  public Optional<@Pattern(regexp = "[A-Z0-9]{18,18}[0-9]{2,2}") String> getLei() {
    return lei;
  }

  public void setLei(Optional<String> lei) {
    this.lei = lei;
  }

  public OrganisationIdentification others(List<@Valid GenericOrganisationIdentification> others) {
    this.others = others;
    return this;
  }

  public OrganisationIdentification addOthersItem(GenericOrganisationIdentification othersItem) {
    if (this.others == null) {
      this.others = new ArrayList<>();
    }
    this.others.add(othersItem);
    return this;
  }

  /**
   * Unique identification of an organisation, as assigned by an institution, using an identification scheme.
   * @return others
   */
  @Valid
  @Schema(name = "others", description = "Unique identification of an organisation, as assigned by an institution, using an identification scheme. ", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("others")
  public List<@Valid GenericOrganisationIdentification> getOthers() {
    return others;
  }

  public void setOthers(List<@Valid GenericOrganisationIdentification> others) {
    this.others = others;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    OrganisationIdentification organisationIdentification = (OrganisationIdentification) o;
    return Objects.equals(this.anyBIC, organisationIdentification.anyBIC) &&
        Objects.equals(this.lei, organisationIdentification.lei) &&
        Objects.equals(this.others, organisationIdentification.others);
  }

  @Override
  public int hashCode() {
    return Objects.hash(anyBIC, lei, others);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class OrganisationIdentification {\n");
    sb.append("    anyBIC: ").append(toIndentedString(anyBIC)).append("\n");
    sb.append("    lei: ").append(toIndentedString(lei)).append("\n");
    sb.append("    others: ").append(toIndentedString(others)).append("\n");
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

