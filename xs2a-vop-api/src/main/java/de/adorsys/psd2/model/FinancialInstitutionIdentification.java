package de.adorsys.psd2.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * See document \\\&quot;openFinance API Framework Data Dictionary\\\&quot;, section \\\&quot;Financial Institution Identification\\\&quot; for more details.
 */

@Schema(name = "financialInstitutionIdentification", description = "See document \\\"openFinance API Framework Data Dictionary\\\", section \\\"Financial Institution Identification\\\" for more details. ")
@JsonTypeName("financialInstitutionIdentification")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2025-09-18T08:00:00.499860948+01:00[Africa/Douala]", comments = "Generator version: 7.15.0")
public class FinancialInstitutionIdentification {

  private Optional<@Pattern(regexp = "[A-Z]{6,6}[A-Z2-9][A-NP-Z0-9]([A-Z0-9]{3,3}){0,1}") String> bicfi = Optional.empty();

  private Optional<ClearingSystemMemberIdentification> clearingSystemMemberId = Optional.empty();

  private Optional<@Size(max = 140) String> name = Optional.empty();

  private Optional<PostalAddress> postalAddress = Optional.empty();

  private Optional<GenericFinancialInstitutionIdentification> other = Optional.empty();

  public FinancialInstitutionIdentification bicfi(String bicfi) {
    this.bicfi = Optional.ofNullable(bicfi);
    return this;
  }

  /**
   *
   * @return bicfi
   */

  @Schema(name = "bicfi", example = "ECBFDEFFFIM", description = "", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("bicfi")
  public Optional<@Pattern(regexp = "[A-Z]{6,6}[A-Z2-9][A-NP-Z0-9]([A-Z0-9]{3,3}){0,1}") String> getBicfi() {
    return bicfi;
  }

  public void setBicfi(Optional<String> bicfi) {
    this.bicfi = bicfi;
  }

  public FinancialInstitutionIdentification clearingSystemMemberId(ClearingSystemMemberIdentification clearingSystemMemberId) {
    this.clearingSystemMemberId = Optional.ofNullable(clearingSystemMemberId);
    return this;
  }

  /**
   * Information used to identify a member within a clearing system.
   * @return clearingSystemMemberId
   */
  @Valid
  @Schema(name = "clearingSystemMemberId", description = "Information used to identify a member within a clearing system. ", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("clearingSystemMemberId")
  public Optional<ClearingSystemMemberIdentification> getClearingSystemMemberId() {
    return clearingSystemMemberId;
  }

  public void setClearingSystemMemberId(Optional<ClearingSystemMemberIdentification> clearingSystemMemberId) {
    this.clearingSystemMemberId = clearingSystemMemberId;
  }

  public FinancialInstitutionIdentification name(String name) {
    this.name = Optional.ofNullable(name);
    return this;
  }

  /**
   * Name of the financial institution
   * @return name
   */

  @Schema(name = "name", example = "Text, maximum of 140 characters.", description = "Name of the financial institution ", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("name")
  public Optional<@Size(max = 140) String> getName() {
    return name;
  }

  public void setName(Optional<String> name) {
    this.name = name;
  }

  public FinancialInstitutionIdentification postalAddress(PostalAddress postalAddress) {
    this.postalAddress = Optional.ofNullable(postalAddress);
    return this;
  }

  /**
   * Postal Address of the financial institution.
   * @return postalAddress
   */
  @Valid
  @Schema(name = "postalAddress", description = "Postal Address of the financial institution. ", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("postalAddress")
  public Optional<PostalAddress> getPostalAddress() {
    return postalAddress;
  }

  public void setPostalAddress(Optional<PostalAddress> postalAddress) {
    this.postalAddress = postalAddress;
  }

  public FinancialInstitutionIdentification other(GenericFinancialInstitutionIdentification other) {
    this.other = Optional.ofNullable(other);
    return this;
  }

  /**
   * Unique identification of an organisation, as assigned by an institution, using an identification scheme.
   * @return other
   */
  @Valid
  @Schema(name = "other", description = "Unique identification of an organisation, as assigned by an institution, using an identification scheme. ", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("other")
  public Optional<GenericFinancialInstitutionIdentification> getOther() {
    return other;
  }

  public void setOther(Optional<GenericFinancialInstitutionIdentification> other) {
    this.other = other;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    FinancialInstitutionIdentification financialInstitutionIdentification = (FinancialInstitutionIdentification) o;
    return Objects.equals(this.bicfi, financialInstitutionIdentification.bicfi) &&
        Objects.equals(this.clearingSystemMemberId, financialInstitutionIdentification.clearingSystemMemberId) &&
        Objects.equals(this.name, financialInstitutionIdentification.name) &&
        Objects.equals(this.postalAddress, financialInstitutionIdentification.postalAddress) &&
        Objects.equals(this.other, financialInstitutionIdentification.other);
  }

  @Override
  public int hashCode() {
    return Objects.hash(bicfi, clearingSystemMemberId, name, postalAddress, other);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class FinancialInstitutionIdentification {\n");
    sb.append("    bicfi: ").append(toIndentedString(bicfi)).append("\n");
    sb.append("    clearingSystemMemberId: ").append(toIndentedString(clearingSystemMemberId)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    postalAddress: ").append(toIndentedString(postalAddress)).append("\n");
    sb.append("    other: ").append(toIndentedString(other)).append("\n");
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

