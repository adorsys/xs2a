package de.adorsys.psd2.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * See document \\\&quot;openFinance API Framework Data Dictionary\\\&quot;, section \\\&quot;Proxy Account Identification\\\&quot; for more details.
 */

@Schema(name = "proxyAccountIdentification", description = "See document \\\"openFinance API Framework Data Dictionary\\\", section \\\"Proxy Account Identification\\\" for more details. ")
@JsonTypeName("proxyAccountIdentification")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2025-09-18T08:00:00.499860948+01:00[Africa/Douala]", comments = "Generator version: 7.15.0")
public class ProxyAccountIdentification {

  private Optional<ProxyAccountTypeCode> typeCode = Optional.empty();

  private Optional<@Size(max = 35) String> typeProprietary = Optional.empty();

  private String identification;

  public ProxyAccountIdentification() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public ProxyAccountIdentification(String identification) {
    this.identification = identification;
  }

  public ProxyAccountIdentification typeCode(ProxyAccountTypeCode typeCode) {
    this.typeCode = Optional.ofNullable(typeCode);
    return this;
  }

  /**
   * Type of the proxy identification
   * @return typeCode
   */
  @Valid
  @Schema(name = "typeCode", description = "Type of the proxy identification ", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("typeCode")
  public Optional<ProxyAccountTypeCode> getTypeCode() {
    return typeCode;
  }

  public void setTypeCode(Optional<ProxyAccountTypeCode> typeCode) {
    this.typeCode = typeCode;
  }

  public ProxyAccountIdentification typeProprietary(String typeProprietary) {
    this.typeProprietary = Optional.ofNullable(typeProprietary);
    return this;
  }

  /**
   *
   * @return typeProprietary
   */

  @Schema(name = "typeProprietary", example = "Text, maximum of 35 characters.", description = "", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("typeProprietary")
  public Optional<@Size(max = 35) String> getTypeProprietary() {
    return typeProprietary;
  }

  public void setTypeProprietary(Optional<String> typeProprietary) {
    this.typeProprietary = typeProprietary;
  }

  public ProxyAccountIdentification identification(String identification) {
    this.identification = identification;
    return this;
  }

  /**
   *
   * @return identification
   */
  @NotNull @Size(max = 2048)
  @Schema(name = "identification", example = "Text, maximum of 2048 characters.", description = "", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("identification")
  public String getIdentification() {
    return identification;
  }

  public void setIdentification(String identification) {
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
    ProxyAccountIdentification proxyAccountIdentification = (ProxyAccountIdentification) o;
    return Objects.equals(this.typeCode, proxyAccountIdentification.typeCode) &&
        Objects.equals(this.typeProprietary, proxyAccountIdentification.typeProprietary) &&
        Objects.equals(this.identification, proxyAccountIdentification.identification);
  }

  @Override
  public int hashCode() {
    return Objects.hash(typeCode, typeProprietary, identification);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ProxyAccountIdentification {\n");
    sb.append("    typeCode: ").append(toIndentedString(typeCode)).append("\n");
    sb.append("    typeProprietary: ").append(toIndentedString(typeProprietary)).append("\n");
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

