/*
 * Copyright 2018-2019 adorsys GmbH & Co KG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.adorsys.psd2.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import de.adorsys.psd2.model.AuthenticationType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import javax.validation.constraints.*;

/**
 * Authentication Object 
 */
@ApiModel(description = "Authentication Object ")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2019-01-11T12:48:04.675377+02:00[Europe/Kiev]")

public class AuthenticationObject   {
  @JsonProperty("authenticationType")
  private AuthenticationType authenticationType = null;

  @JsonProperty("authenticationVersion")
  private String authenticationVersion = null;

  @JsonProperty("authenticationMethodId")
  private String authenticationMethodId = null;

  @JsonProperty("name")
  private String name = null;

  @JsonProperty("explanation")
  private String explanation = null;

  public AuthenticationObject authenticationType(AuthenticationType authenticationType) {
    this.authenticationType = authenticationType;
    return this;
  }

  /**
   * Get authenticationType
   * @return authenticationType
  **/
  @ApiModelProperty(required = true, value = "")
  @NotNull

  @Valid

  public AuthenticationType getAuthenticationType() {
    return authenticationType;
  }

  public void setAuthenticationType(AuthenticationType authenticationType) {
    this.authenticationType = authenticationType;
  }

  public AuthenticationObject authenticationVersion(String authenticationVersion) {
    this.authenticationVersion = authenticationVersion;
    return this;
  }

  /**
   * Depending on the \"authenticationType\". This version can be used by differentiating authentication tools used within performing OTP generation in the same authentication type. This version can be referred to in the ASPSP?s documentation. 
   * @return authenticationVersion
  **/
  @ApiModelProperty(value = "Depending on the \"authenticationType\". This version can be used by differentiating authentication tools used within performing OTP generation in the same authentication type. This version can be referred to in the ASPSP?s documentation. ")


  public String getAuthenticationVersion() {
    return authenticationVersion;
  }

  public void setAuthenticationVersion(String authenticationVersion) {
    this.authenticationVersion = authenticationVersion;
  }

  public AuthenticationObject authenticationMethodId(String authenticationMethodId) {
    this.authenticationMethodId = authenticationMethodId;
    return this;
  }

  /**
   * Get authenticationMethodId
   * @return authenticationMethodId
  **/
  @ApiModelProperty(required = true, value = "")
  @NotNull

@Size(max=35) 
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
   * This is the name of the authentication method defined by the PSU in the Online Banking frontend of the ASPSP. Alternatively this could be a description provided by the ASPSP like \"SMS OTP on phone +49160 xxxxx 28\". This name shall be used by the TPP when presenting a list of authentication methods to the PSU, if available. 
   * @return name
  **/
  @ApiModelProperty(example = "SMS OTP on phone +49160 xxxxx 28", value = "This is the name of the authentication method defined by the PSU in the Online Banking frontend of the ASPSP. Alternatively this could be a description provided by the ASPSP like \"SMS OTP on phone +49160 xxxxx 28\". This name shall be used by the TPP when presenting a list of authentication methods to the PSU, if available. ")


  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public AuthenticationObject explanation(String explanation) {
    this.explanation = explanation;
    return this;
  }

  /**
   * Detailed information about the SCA method for the PSU. 
   * @return explanation
  **/
  @ApiModelProperty(example = "Detailed information about the SCA method for the PSU.", value = "Detailed information about the SCA method for the PSU. ")


  public String getExplanation() {
    return explanation;
  }

  public void setExplanation(String explanation) {
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

