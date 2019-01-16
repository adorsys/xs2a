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
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.HashMap;
import java.util.Map;
import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import javax.validation.constraints.*;

/**
 * A list of hyperlinks to be recognised by the TPP. The actual hyperlinks used in  the response depend on the dynamical decisions of the ASPSP when processing the request.  **Remark:** All links can be relative or full links, to be decided by the ASPSP.   **Remark:** This method can be applied before or after PSU identification.  This leads to many possible hyperlink responses. Type of links admitted in this response, (further links might be added for ASPSP defined  extensions):  - \&quot;scaRedirect\&quot;:    In case of an SCA Redirect Approach, the ASPSP is transmitting the link to which to    redirect the PSU browser. - \&quot;scaOAuth\&quot;:    In case of a SCA OAuth2 Approach, the ASPSP is transmitting the URI where the    configuration of the Authorisation Server can be retrieved.    The configuration follows the OAuth 2.0 Authorisation Server Metadata specification. - \&quot;updatePsuIdentification\&quot;:    The link to the authorisation or cancellation authorisation sub-resource,    where PSU identification data needs to be uploaded. - \&quot;updatePsuAuthentication\&quot;:   The link to the authorisation or cancellation authorisation sub-resource,    where PSU authentication data needs to be uploaded. \&quot;authoriseTransaction\&quot;:   The link to the authorisation or cancellation authorisation sub-resource,    where the authorisation data has to be uploaded, e.g. the TOP received by SMS.  \&quot;scaStatus\&quot;:    The link to retrieve the scaStatus of the corresponding authorisation sub-resource. 
 */
@ApiModel(description = "A list of hyperlinks to be recognised by the TPP. The actual hyperlinks used in  the response depend on the dynamical decisions of the ASPSP when processing the request.  **Remark:** All links can be relative or full links, to be decided by the ASPSP.   **Remark:** This method can be applied before or after PSU identification.  This leads to many possible hyperlink responses. Type of links admitted in this response, (further links might be added for ASPSP defined  extensions):  - \"scaRedirect\":    In case of an SCA Redirect Approach, the ASPSP is transmitting the link to which to    redirect the PSU browser. - \"scaOAuth\":    In case of a SCA OAuth2 Approach, the ASPSP is transmitting the URI where the    configuration of the Authorisation Server can be retrieved.    The configuration follows the OAuth 2.0 Authorisation Server Metadata specification. - \"updatePsuIdentification\":    The link to the authorisation or cancellation authorisation sub-resource,    where PSU identification data needs to be uploaded. - \"updatePsuAuthentication\":   The link to the authorisation or cancellation authorisation sub-resource,    where PSU authentication data needs to be uploaded. \"authoriseTransaction\":   The link to the authorisation or cancellation authorisation sub-resource,    where the authorisation data has to be uploaded, e.g. the TOP received by SMS.  \"scaStatus\":    The link to retrieve the scaStatus of the corresponding authorisation sub-resource. ")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2019-01-11T12:48:04.675377+02:00[Europe/Kiev]")

public class LinksSelectPsuAuthenticationMethod extends HashMap<String, String>  {
  @JsonProperty("scaRedirect")
  private String scaRedirect = null;

  @JsonProperty("scaOAuth")
  private String scaOAuth = null;

  @JsonProperty("updatePsuIdentification")
  private String updatePsuIdentification = null;

  @JsonProperty("updatePsuAuthentication")
  private String updatePsuAuthentication = null;

  @JsonProperty("authoriseTransaction")
  private String authoriseTransaction = null;

  @JsonProperty("scaStatus")
  private String scaStatus = null;

  public LinksSelectPsuAuthenticationMethod scaRedirect(String scaRedirect) {
    this.scaRedirect = scaRedirect;
    return this;
  }

  /**
   * Get scaRedirect
   * @return scaRedirect
  **/
  @ApiModelProperty(value = "")


  public String getScaRedirect() {
    return scaRedirect;
  }

  public void setScaRedirect(String scaRedirect) {
    this.scaRedirect = scaRedirect;
  }

  public LinksSelectPsuAuthenticationMethod scaOAuth(String scaOAuth) {
    this.scaOAuth = scaOAuth;
    return this;
  }

  /**
   * Get scaOAuth
   * @return scaOAuth
  **/
  @ApiModelProperty(value = "")


  public String getScaOAuth() {
    return scaOAuth;
  }

  public void setScaOAuth(String scaOAuth) {
    this.scaOAuth = scaOAuth;
  }

  public LinksSelectPsuAuthenticationMethod updatePsuIdentification(String updatePsuIdentification) {
    this.updatePsuIdentification = updatePsuIdentification;
    return this;
  }

  /**
   * Get updatePsuIdentification
   * @return updatePsuIdentification
  **/
  @ApiModelProperty(value = "")


  public String getUpdatePsuIdentification() {
    return updatePsuIdentification;
  }

  public void setUpdatePsuIdentification(String updatePsuIdentification) {
    this.updatePsuIdentification = updatePsuIdentification;
  }

  public LinksSelectPsuAuthenticationMethod updatePsuAuthentication(String updatePsuAuthentication) {
    this.updatePsuAuthentication = updatePsuAuthentication;
    return this;
  }

  /**
   * Get updatePsuAuthentication
   * @return updatePsuAuthentication
  **/
  @ApiModelProperty(value = "")


  public String getUpdatePsuAuthentication() {
    return updatePsuAuthentication;
  }

  public void setUpdatePsuAuthentication(String updatePsuAuthentication) {
    this.updatePsuAuthentication = updatePsuAuthentication;
  }

  public LinksSelectPsuAuthenticationMethod authoriseTransaction(String authoriseTransaction) {
    this.authoriseTransaction = authoriseTransaction;
    return this;
  }

  /**
   * Get authoriseTransaction
   * @return authoriseTransaction
  **/
  @ApiModelProperty(value = "")


  public String getAuthoriseTransaction() {
    return authoriseTransaction;
  }

  public void setAuthoriseTransaction(String authoriseTransaction) {
    this.authoriseTransaction = authoriseTransaction;
  }

  public LinksSelectPsuAuthenticationMethod scaStatus(String scaStatus) {
    this.scaStatus = scaStatus;
    return this;
  }

  /**
   * Get scaStatus
   * @return scaStatus
  **/
  @ApiModelProperty(value = "")


  public String getScaStatus() {
    return scaStatus;
  }

  public void setScaStatus(String scaStatus) {
    this.scaStatus = scaStatus;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    LinksSelectPsuAuthenticationMethod _linksSelectPsuAuthenticationMethod = (LinksSelectPsuAuthenticationMethod) o;
    return Objects.equals(this.scaRedirect, _linksSelectPsuAuthenticationMethod.scaRedirect) &&
        Objects.equals(this.scaOAuth, _linksSelectPsuAuthenticationMethod.scaOAuth) &&
        Objects.equals(this.updatePsuIdentification, _linksSelectPsuAuthenticationMethod.updatePsuIdentification) &&
        Objects.equals(this.updatePsuAuthentication, _linksSelectPsuAuthenticationMethod.updatePsuAuthentication) &&
        Objects.equals(this.authoriseTransaction, _linksSelectPsuAuthenticationMethod.authoriseTransaction) &&
        Objects.equals(this.scaStatus, _linksSelectPsuAuthenticationMethod.scaStatus) &&
        super.equals(o);
  }

  @Override
  public int hashCode() {
    return Objects.hash(scaRedirect, scaOAuth, updatePsuIdentification, updatePsuAuthentication, authoriseTransaction, scaStatus, super.hashCode());
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class LinksSelectPsuAuthenticationMethod {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
    sb.append("    scaRedirect: ").append(toIndentedString(scaRedirect)).append("\n");
    sb.append("    scaOAuth: ").append(toIndentedString(scaOAuth)).append("\n");
    sb.append("    updatePsuIdentification: ").append(toIndentedString(updatePsuIdentification)).append("\n");
    sb.append("    updatePsuAuthentication: ").append(toIndentedString(updatePsuAuthentication)).append("\n");
    sb.append("    authoriseTransaction: ").append(toIndentedString(authoriseTransaction)).append("\n");
    sb.append("    scaStatus: ").append(toIndentedString(scaStatus)).append("\n");
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

