package de.adorsys.psd2.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * See document \\\&quot;openFinance API Framework Data Dictionary\\\&quot;, section \\\&quot;Authentication Object\\\&quot; for more details.
 */

@Schema(name = "authenticationObject", description = "See document \\\"openFinance API Framework Data Dictionary\\\", section \\\"Authentication Object\\\" for more details. ")
@JsonTypeName("authenticationObject")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2025-09-18T08:00:00.499860948+01:00[Africa/Douala]", comments = "Generator version: 7.15.0")
public class AuthenticationObject {

  private AuthenticationType authenticationType;

  private Optional<String> authenticationVersion = Optional.empty();

  private String authenticationMethodId;

  private String name;

  private Optional<String> explanation = Optional.empty();

  public AuthenticationObject() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public AuthenticationObject(AuthenticationType authenticationType, String authenticationMethodId, String name) {
    this.authenticationType = authenticationType;
    this.authenticationMethodId = authenticationMethodId;
    this.name = name;
  }

  public AuthenticationObject authenticationType(AuthenticationType authenticationType) {
    this.authenticationType = authenticationType;
    return this;
  }

  /**
   * Type of the authentication method.
   * @return authenticationType
   */
  @NotNull @Valid
  @Schema(name = "authenticationType", description = "Type of the authentication method. ", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("authenticationType")
  public AuthenticationType getAuthenticationType() {
    return authenticationType;
  }

  public void setAuthenticationType(AuthenticationType authenticationType) {
    this.authenticationType = authenticationType;
  }

  public AuthenticationObject authenticationVersion(String authenticationVersion) {
    this.authenticationVersion = Optional.ofNullable(authenticationVersion);
    return this;
  }

  /**
   * Depending on the \\\"authenticationType\\\". This version can be used by differentiating authentication tools used within performing OTP generation in the same authentication type.  This version can be referred to in the ASPSP's documentation.
   * @return authenticationVersion
   */

  @Schema(name = "authenticationVersion", description = "Depending on the \\\"authenticationType\\\". This version can be used by differentiating authentication tools used within performing OTP generation in the same authentication type.  This version can be referred to in the ASPSP's documentation. ", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("authenticationVersion")
  public Optional<String> getAuthenticationVersion() {
    return authenticationVersion;
  }

  public void setAuthenticationVersion(Optional<String> authenticationVersion) {
    this.authenticationVersion = authenticationVersion;
  }

  public AuthenticationObject authenticationMethodId(String authenticationMethodId) {
    this.authenticationMethodId = authenticationMethodId;
    return this;
  }

  /**
   * An identification provided by the ASPSP for the later identification of the authentication method selection.
   * @return authenticationMethodId
   */
  @NotNull @Size(max = 35)
  @Schema(name = "authenticationMethodId", example = "Text, maximum of 35 characters.", description = "An identification provided by the ASPSP for the later identification of the authentication method selection. ", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("authenticationMethodId")
  public String getAuthenticationMethodId() {
    return authenticationMethodId;
  }

  public void setAuthenticationMethodId(String authenticationMethodId) {
    this.authenticationMethodId = authenticationMethodId;
  }

  public AuthenticationObject name(String name) {
    this.name = name;
    return this;
  }

  /**
   * This is the name of the authentication method defined by the PSU in the Online Banking frontend of the ASPSP.  Alternatively this could be a description provided by the ASPSP like \\\"SMS OTP on phone +49160 xxxxx 28\\\". This name shall be used by the TPP when presenting a list of authentication methods to the PSU, if available.
   * @return name
   */
  @NotNull
  @Schema(name = "name", description = "This is the name of the authentication method defined by the PSU in the Online Banking frontend of the ASPSP.  Alternatively this could be a description provided by the ASPSP like \\\"SMS OTP on phone +49160 xxxxx 28\\\". This name shall be used by the TPP when presenting a list of authentication methods to the PSU, if available. ", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("name")
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public AuthenticationObject explanation(String explanation) {
    this.explanation = Optional.ofNullable(explanation);
    return this;
  }

  /**
   * detailed information about the SCA method for the PSU
   * @return explanation
   */

  @Schema(name = "explanation", description = "detailed information about the SCA method for the PSU ", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("explanation")
  public Optional<String> getExplanation() {
    return explanation;
  }

  public void setExplanation(Optional<String> explanation) {
    this.explanation = explanation;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AuthenticationObject authenticationObject = (AuthenticationObject) o;
    return Objects.equals(this.authenticationType, authenticationObject.authenticationType) &&
        Objects.equals(this.authenticationVersion, authenticationObject.authenticationVersion) &&
        Objects.equals(this.authenticationMethodId, authenticationObject.authenticationMethodId) &&
        Objects.equals(this.name, authenticationObject.name) &&
        Objects.equals(this.explanation, authenticationObject.explanation);
  }

  @Override
  public int hashCode() {
    return Objects.hash(authenticationType, authenticationVersion, authenticationMethodId, name, explanation);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AuthenticationObject {\n");
    sb.append("    authenticationType: ").append(toIndentedString(authenticationType)).append("\n");
    sb.append("    authenticationVersion: ").append(toIndentedString(authenticationVersion)).append("\n");
    sb.append("    authenticationMethodId: ").append(toIndentedString(authenticationMethodId)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    explanation: ").append(toIndentedString(explanation)).append("\n");
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

