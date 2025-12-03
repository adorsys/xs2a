package de.adorsys.psd2.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * See document \\\&quot;openFinance API Framework Data Dictionary\\\&quot;, section \\\&quot;Generic Financial Institution Identification\\\&quot; for more details.
 */

@Schema(name = "genericFinancialInstitutionIdentification", description = "See document \\\"openFinance API Framework Data Dictionary\\\", section \\\"Generic Financial Institution Identification\\\" for more details. ")
@JsonTypeName("genericFinancialInstitutionIdentification")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2025-09-18T08:00:00.499860948+01:00[Africa/Douala]", comments = "Generator version: 7.15.0")
public class GenericFinancialInstitutionIdentification {

  private String identification;

  private Optional<String> schemeNameCode = Optional.empty();

  private Optional<@Size(max = 35) String> schemeNameProprietary = Optional.empty();

  private Optional<@Size(max = 35) String> issuer = Optional.empty();

  public GenericFinancialInstitutionIdentification() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public GenericFinancialInstitutionIdentification(String identification) {
    this.identification = identification;
  }

  public GenericFinancialInstitutionIdentification identification(String identification) {
    this.identification = identification;
    return this;
  }

  /**
   * Unique and unambiguous identification of a financial insitution.
   * @return identification
   */
  @NotNull @Size(max = 35)
  @Schema(name = "identification", example = "Text, maximum of 35 characters.", description = "Unique and unambiguous identification of a financial insitution. ", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("identification")
  public String getIdentification() {
    return identification;
  }

  public void setIdentification(String identification) {
    this.identification = identification;
  }

  public GenericFinancialInstitutionIdentification schemeNameCode(String schemeNameCode) {
    this.schemeNameCode = Optional.ofNullable(schemeNameCode);
    return this;
  }

  /**
   * An entry provided by an external ISO code list
   * @return schemeNameCode
   */

  @Schema(name = "schemeNameCode", description = "An entry provided by an external ISO code list ", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("schemeNameCode")
  public Optional<String> getSchemeNameCode() {
    return schemeNameCode;
  }

  public void setSchemeNameCode(Optional<String> schemeNameCode) {
    this.schemeNameCode = schemeNameCode;
  }

  public GenericFinancialInstitutionIdentification schemeNameProprietary(String schemeNameProprietary) {
    this.schemeNameProprietary = Optional.ofNullable(schemeNameProprietary);
    return this;
  }

  /**
   * A scheme name defined in a proprietary way.
   * @return schemeNameProprietary
   */

  @Schema(name = "schemeNameProprietary", example = "Text, maximum of 35 characters.", description = "A scheme name defined in a proprietary way. ", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("schemeNameProprietary")
  public Optional<@Size(max = 35) String> getSchemeNameProprietary() {
    return schemeNameProprietary;
  }

  public void setSchemeNameProprietary(Optional<String> schemeNameProprietary) {
    this.schemeNameProprietary = schemeNameProprietary;
  }

  public GenericFinancialInstitutionIdentification issuer(String issuer) {
    this.issuer = Optional.ofNullable(issuer);
    return this;
  }

  /**
   * Issuer of the identification
   * @return issuer
   */

  @Schema(name = "issuer", example = "Text, maximum of 35 characters.", description = "Issuer of the identification ", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("issuer")
  public Optional<@Size(max = 35) String> getIssuer() {
    return issuer;
  }

  public void setIssuer(Optional<String> issuer) {
    this.issuer = issuer;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    GenericFinancialInstitutionIdentification genericFinancialInstitutionIdentification = (GenericFinancialInstitutionIdentification) o;
    return Objects.equals(this.identification, genericFinancialInstitutionIdentification.identification) &&
        Objects.equals(this.schemeNameCode, genericFinancialInstitutionIdentification.schemeNameCode) &&
        Objects.equals(this.schemeNameProprietary, genericFinancialInstitutionIdentification.schemeNameProprietary) &&
        Objects.equals(this.issuer, genericFinancialInstitutionIdentification.issuer);
  }

  @Override
  public int hashCode() {
    return Objects.hash(identification, schemeNameCode, schemeNameProprietary, issuer);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class GenericFinancialInstitutionIdentification {\n");
    sb.append("    identification: ").append(toIndentedString(identification)).append("\n");
    sb.append("    schemeNameCode: ").append(toIndentedString(schemeNameCode)).append("\n");
    sb.append("    schemeNameProprietary: ").append(toIndentedString(schemeNameProprietary)).append("\n");
    sb.append("    issuer: ").append(toIndentedString(issuer)).append("\n");
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

