package de.adorsys.psd2.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * See document \\\&quot;openFinance API Framework Data Dictionary\\\&quot;, section \\\&quot;Clearing System Member Identification\\\&quot; for more details.
 */

@Schema(name = "clearingSystemMemberIdentification", description = "See document \\\"openFinance API Framework Data Dictionary\\\", section \\\"Clearing System Member Identification\\\" for more details. ")
@JsonTypeName("clearingSystemMemberIdentification")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2025-09-18T08:00:00.499860948+01:00[Africa/Douala]", comments = "Generator version: 7.15.0")
public class ClearingSystemMemberIdentification {

  private Optional<@Size(max = 35) String> memberId = Optional.empty();

  private Optional<ClearingSystemIdentificationCode> clearingSystemIdentificationCode = Optional.empty();

  private Optional<@Size(max = 35) String> clearingSystemIdentificationProprietary = Optional.empty();

  public ClearingSystemMemberIdentification memberId(String memberId) {
    this.memberId = Optional.ofNullable(memberId);
    return this;
  }

  /**
   * Identification of a member of a clearing system.
   * @return memberId
   */

  @Schema(name = "memberId", example = "Text, maximum of 35 characters.", description = "Identification of a member of a clearing system. ", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("memberId")
  public Optional<@Size(max = 35) String> getMemberId() {
    return memberId;
  }

  public void setMemberId(Optional<String> memberId) {
    this.memberId = memberId;
  }

  public ClearingSystemMemberIdentification clearingSystemIdentificationCode(ClearingSystemIdentificationCode clearingSystemIdentificationCode) {
    this.clearingSystemIdentificationCode = Optional.ofNullable(clearingSystemIdentificationCode);
    return this;
  }

  /**
   * Identification of a clearing system, in a coded form as published in an external list.
   * @return clearingSystemIdentificationCode
   */
  @Valid
  @Schema(name = "clearingSystemIdentificationCode", description = "Identification of a clearing system, in a coded form as published in an external list. ", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("clearingSystemIdentificationCode")
  public Optional<ClearingSystemIdentificationCode> getClearingSystemIdentificationCode() {
    return clearingSystemIdentificationCode;
  }

  public void setClearingSystemIdentificationCode(Optional<ClearingSystemIdentificationCode> clearingSystemIdentificationCode) {
    this.clearingSystemIdentificationCode = clearingSystemIdentificationCode;
  }

  public ClearingSystemMemberIdentification clearingSystemIdentificationProprietary(String clearingSystemIdentificationProprietary) {
    this.clearingSystemIdentificationProprietary = Optional.ofNullable(clearingSystemIdentificationProprietary);
    return this;
  }

  /**
   * Identification code for a clearing system, that has not yet been identified in the list of clearing systems.
   * @return clearingSystemIdentificationProprietary
   */

  @Schema(name = "clearingSystemIdentificationProprietary", example = "Text, maximum of 35 characters.", description = "Identification code for a clearing system, that has not yet been identified in the list of clearing systems. ", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("clearingSystemIdentificationProprietary")
  public Optional<@Size(max = 35) String> getClearingSystemIdentificationProprietary() {
    return clearingSystemIdentificationProprietary;
  }

  public void setClearingSystemIdentificationProprietary(Optional<String> clearingSystemIdentificationProprietary) {
    this.clearingSystemIdentificationProprietary = clearingSystemIdentificationProprietary;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ClearingSystemMemberIdentification clearingSystemMemberIdentification = (ClearingSystemMemberIdentification) o;
    return Objects.equals(this.memberId, clearingSystemMemberIdentification.memberId) &&
        Objects.equals(this.clearingSystemIdentificationCode, clearingSystemMemberIdentification.clearingSystemIdentificationCode) &&
        Objects.equals(this.clearingSystemIdentificationProprietary, clearingSystemMemberIdentification.clearingSystemIdentificationProprietary);
  }

  @Override
  public int hashCode() {
    return Objects.hash(memberId, clearingSystemIdentificationCode, clearingSystemIdentificationProprietary);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ClearingSystemMemberIdentification {\n");
    sb.append("    memberId: ").append(toIndentedString(memberId)).append("\n");
    sb.append("    clearingSystemIdentificationCode: ").append(toIndentedString(clearingSystemIdentificationCode)).append("\n");
    sb.append("    clearingSystemIdentificationProprietary: ").append(toIndentedString(clearingSystemIdentificationProprietary)).append("\n");
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

