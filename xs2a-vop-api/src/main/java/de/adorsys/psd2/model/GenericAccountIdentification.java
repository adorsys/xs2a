package de.adorsys.psd2.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * See document \\\&quot;openFinance API Framework Data Dictionary\\\&quot;, section \\\&quot;Generic Account Identification\\\&quot; for more details.
 */

@Schema(name = "genericAccountIdentification", description = "See document \\\"openFinance API Framework Data Dictionary\\\", section \\\"Generic Account Identification\\\" for more details. ")
@JsonTypeName("genericAccountIdentification")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2025-09-18T08:00:00.499860948+01:00[Africa/Douala]", comments = "Generator version: 7.15.0")
public class GenericAccountIdentification {

  private String identification;

  private Optional<AccountIdentificationCode> schemeNameCode = Optional.empty();

  private Optional<@Size(max = 35) String> schemeNameProprietary = Optional.empty();

  private Optional<@Size(max = 35) String> issuer = Optional.empty();

  public GenericAccountIdentification() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public GenericAccountIdentification(String identification) {
    this.identification = identification;
  }

  public GenericAccountIdentification identification(String identification) {
    this.identification = identification;
    return this;
  }

  /**
   * Unique and unambiguous identification of an account.
   * @return identification
   */
  @NotNull @Size(max = 35)
  @Schema(name = "identification", example = "Text, maximum of 35 characters.", description = "Unique and unambiguous identification of an account. ", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("identification")
  public String getIdentification() {
    return identification;
  }

  public void setIdentification(String identification) {
    this.identification = identification;
  }

  public GenericAccountIdentification schemeNameCode(AccountIdentificationCode schemeNameCode) {
    this.schemeNameCode = Optional.ofNullable(schemeNameCode);
    return this;
  }

  /**
   * An entry provided by an external ISO code list
   * @return schemeNameCode
   */
  @Valid
  @Schema(name = "schemeNameCode", description = "An entry provided by an external ISO code list ", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("schemeNameCode")
  public Optional<AccountIdentificationCode> getSchemeNameCode() {
    return schemeNameCode;
  }

  public void setSchemeNameCode(Optional<AccountIdentificationCode> schemeNameCode) {
    this.schemeNameCode = schemeNameCode;
  }

  public GenericAccountIdentification schemeNameProprietary(String schemeNameProprietary) {
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

  public GenericAccountIdentification issuer(String issuer) {
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
    GenericAccountIdentification genericAccountIdentification = (GenericAccountIdentification) o;
    return Objects.equals(this.identification, genericAccountIdentification.identification) &&
        Objects.equals(this.schemeNameCode, genericAccountIdentification.schemeNameCode) &&
        Objects.equals(this.schemeNameProprietary, genericAccountIdentification.schemeNameProprietary) &&
        Objects.equals(this.issuer, genericAccountIdentification.issuer);
  }

  @Override
  public int hashCode() {
    return Objects.hash(identification, schemeNameCode, schemeNameProprietary, issuer);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class GenericAccountIdentification {\n");
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

