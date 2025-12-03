package de.adorsys.psd2.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * See document \\\&quot;openFinance API Framework Data Dictionary\\\&quot;, section \\\&quot;Agent Description\\\&quot; for more details.
 */

@Schema(name = "agentDescription", description = "See document \\\"openFinance API Framework Data Dictionary\\\", section \\\"Agent Description\\\" for more details. ")
@JsonTypeName("agentDescription")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2025-09-18T08:00:00.499860948+01:00[Africa/Douala]", comments = "Generator version: 7.15.0")
public class AgentDescription {

  private FinancialInstitutionIdentification financialInstitutionId;

  private Optional<@Size(max = 35) String> branchIdentificationId = Optional.empty();

  private Optional<@Size(max = 140) String> branchIdentificationName = Optional.empty();

  public AgentDescription() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public AgentDescription(FinancialInstitutionIdentification financialInstitutionId) {
    this.financialInstitutionId = financialInstitutionId;
  }

  public AgentDescription financialInstitutionId(FinancialInstitutionIdentification financialInstitutionId) {
    this.financialInstitutionId = financialInstitutionId;
    return this;
  }

  /**
   *
   * @return financialInstitutionId
   */
  @NotNull @Valid
  @Schema(name = "financialInstitutionId", description = "", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("financialInstitutionId")
  public FinancialInstitutionIdentification getFinancialInstitutionId() {
    return financialInstitutionId;
  }

  public void setFinancialInstitutionId(FinancialInstitutionIdentification financialInstitutionId) {
    this.financialInstitutionId = financialInstitutionId;
  }

  public AgentDescription branchIdentificationId(String branchIdentificationId) {
    this.branchIdentificationId = Optional.ofNullable(branchIdentificationId);
    return this;
  }

  /**
   * Unique and unambiguous identification of a branch of a financial institution.
   * @return branchIdentificationId
   */

  @Schema(name = "branchIdentificationId", example = "Text, maximum of 35 characters.", description = "Unique and unambiguous identification of a branch of a financial institution. ", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("branchIdentificationId")
  public Optional<@Size(max = 35) String> getBranchIdentificationId() {
    return branchIdentificationId;
  }

  public void setBranchIdentificationId(Optional<String> branchIdentificationId) {
    this.branchIdentificationId = branchIdentificationId;
  }

  public AgentDescription branchIdentificationName(String branchIdentificationName) {
    this.branchIdentificationName = Optional.ofNullable(branchIdentificationName);
    return this;
  }

  /**
   * Name by which an agent is known and which is usually used to identify that agent.
   * @return branchIdentificationName
   */

  @Schema(name = "branchIdentificationName", example = "Text, maximum of 140 characters.", description = "Name by which an agent is known and which is usually used to identify that agent. ", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("branchIdentificationName")
  public Optional<@Size(max = 140) String> getBranchIdentificationName() {
    return branchIdentificationName;
  }

  public void setBranchIdentificationName(Optional<String> branchIdentificationName) {
    this.branchIdentificationName = branchIdentificationName;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AgentDescription agentDescription = (AgentDescription) o;
    return Objects.equals(this.financialInstitutionId, agentDescription.financialInstitutionId) &&
        Objects.equals(this.branchIdentificationId, agentDescription.branchIdentificationId) &&
        Objects.equals(this.branchIdentificationName, agentDescription.branchIdentificationName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(financialInstitutionId, branchIdentificationId, branchIdentificationName);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AgentDescription {\n");
    sb.append("    financialInstitutionId: ").append(toIndentedString(financialInstitutionId)).append("\n");
    sb.append("    branchIdentificationId: ").append(toIndentedString(branchIdentificationId)).append("\n");
    sb.append("    branchIdentificationName: ").append(toIndentedString(branchIdentificationName)).append("\n");
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

