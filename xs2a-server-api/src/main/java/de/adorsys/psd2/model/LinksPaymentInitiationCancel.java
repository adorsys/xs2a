/*
 * Copyright 2018-2024 adorsys GmbH & Co KG
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version. This program is distributed in the hope that
 * it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 *
 * This project is also available under a separate commercial license. You can
 * contact us at sales@adorsys.com.
 */

package de.adorsys.psd2.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Objects;

/**
 * A list of hyperlinks to be recognised by the TPP. The actual hyperlinks used in the response depend on the  dynamical decisions of the ASPSP when processing the request.  Remark: All links can be relative or full links, to be decided by the ASPSP.  Type of links admitted in this response, (further links might be added for ASPSP defined extensions):    * &#x27;startAuthorisation&#x27;:      In case, where just the authorisation process of the cancellation needs to be started,      but no additional data needs to be updated for time being (no authentication method to be selected,      no PSU identification nor PSU authentication data to be uploaded).   * &#x27;startAuthorisationWithPsuIdentification&#x27;:      In case where a PSU identification needs to be updated when starting the cancellation authorisation:     The link to the cancellation-authorisations end-point, where the cancellation sub-resource has to be      generated while uploading the PSU identification data.   * &#x27;startAuthorisationWithPsuAuthentication&#x27;:      In case of a yet to be created authorisation sub-resource: The link to the cancalation authorisation end-point,      where the authorisation sub-resource has to be generated while uploading the PSU authentication data.   * &#x27;startAuthorisationWithEncryptedPsuAuthentication&#x27;:     Same as startAuthorisactionWithPsuAuthentication where the authentication data need to be encrypted on      application layer in uploading.   * &#x27;startAuthorisationWithAuthenticationMethodSelection&#x27;:     The link to the authorisation end-point, where the cancellation-authorisation sub-resource has to be      generated while selecting the authentication method. This link is contained under exactly the same      conditions as the data element &#x27;scaMethods&#x27;
 */
@Schema(description = "A list of hyperlinks to be recognised by the TPP. The actual hyperlinks used in the response depend on the  dynamical decisions of the ASPSP when processing the request.  Remark: All links can be relative or full links, to be decided by the ASPSP.  Type of links admitted in this response, (further links might be added for ASPSP defined extensions):    * 'startAuthorisation':      In case, where just the authorisation process of the cancellation needs to be started,      but no additional data needs to be updated for time being (no authentication method to be selected,      no PSU identification nor PSU authentication data to be uploaded).   * 'startAuthorisationWithPsuIdentification':      In case where a PSU identification needs to be updated when starting the cancellation authorisation:     The link to the cancellation-authorisations end-point, where the cancellation sub-resource has to be      generated while uploading the PSU identification data.   * 'startAuthorisationWithPsuAuthentication':      In case of a yet to be created authorisation sub-resource: The link to the cancalation authorisation end-point,      where the authorisation sub-resource has to be generated while uploading the PSU authentication data.   * 'startAuthorisationWithEncryptedPsuAuthentication':     Same as startAuthorisactionWithPsuAuthentication where the authentication data need to be encrypted on      application layer in uploading.   * 'startAuthorisationWithAuthenticationMethodSelection':     The link to the authorisation end-point, where the cancellation-authorisation sub-resource has to be      generated while selecting the authentication method. This link is contained under exactly the same      conditions as the data element 'scaMethods' ")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2022-05-06T13:00:42.214155+03:00[Europe/Kiev]")


public class LinksPaymentInitiationCancel extends HashMap<String, HrefType>  {
  @JsonProperty("startAuthorisation")
  private HrefType startAuthorisation = null;

  @JsonProperty("startAuthorisationWithPsuIdentification")
  private HrefType startAuthorisationWithPsuIdentification = null;

  @JsonProperty("startAuthorisationWithPsuAuthentication")
  private HrefType startAuthorisationWithPsuAuthentication = null;

  @JsonProperty("startAuthorisationWithEncryptedPsuAuthentication")
  private HrefType startAuthorisationWithEncryptedPsuAuthentication = null;

  @JsonProperty("startAuthorisationWithAuthenticationMethodSelection")
  private HrefType startAuthorisationWithAuthenticationMethodSelection = null;

  public LinksPaymentInitiationCancel startAuthorisation(HrefType startAuthorisation) {
    this.startAuthorisation = startAuthorisation;
    return this;
  }

  /**
   * Get startAuthorisation
   * @return startAuthorisation
   **/
  @Schema(description = "")

    @Valid
    public HrefType getStartAuthorisation() {
    return startAuthorisation;
  }

  public void setStartAuthorisation(HrefType startAuthorisation) {
    this.startAuthorisation = startAuthorisation;
  }

  public LinksPaymentInitiationCancel startAuthorisationWithPsuIdentification(HrefType startAuthorisationWithPsuIdentification) {
    this.startAuthorisationWithPsuIdentification = startAuthorisationWithPsuIdentification;
    return this;
  }

  /**
   * Get startAuthorisationWithPsuIdentification
   * @return startAuthorisationWithPsuIdentification
   **/
  @Schema(description = "")

    @Valid
    public HrefType getStartAuthorisationWithPsuIdentification() {
    return startAuthorisationWithPsuIdentification;
  }

  public void setStartAuthorisationWithPsuIdentification(HrefType startAuthorisationWithPsuIdentification) {
    this.startAuthorisationWithPsuIdentification = startAuthorisationWithPsuIdentification;
  }

  public LinksPaymentInitiationCancel startAuthorisationWithPsuAuthentication(HrefType startAuthorisationWithPsuAuthentication) {
    this.startAuthorisationWithPsuAuthentication = startAuthorisationWithPsuAuthentication;
    return this;
  }

  /**
   * Get startAuthorisationWithPsuAuthentication
   * @return startAuthorisationWithPsuAuthentication
   **/
  @Schema(description = "")

    @Valid
    public HrefType getStartAuthorisationWithPsuAuthentication() {
    return startAuthorisationWithPsuAuthentication;
  }

  public void setStartAuthorisationWithPsuAuthentication(HrefType startAuthorisationWithPsuAuthentication) {
    this.startAuthorisationWithPsuAuthentication = startAuthorisationWithPsuAuthentication;
  }

  public LinksPaymentInitiationCancel startAuthorisationWithEncryptedPsuAuthentication(HrefType startAuthorisationWithEncryptedPsuAuthentication) {
    this.startAuthorisationWithEncryptedPsuAuthentication = startAuthorisationWithEncryptedPsuAuthentication;
    return this;
  }

  /**
   * Get startAuthorisationWithEncryptedPsuAuthentication
   * @return startAuthorisationWithEncryptedPsuAuthentication
   **/
  @Schema(description = "")

    @Valid
    public HrefType getStartAuthorisationWithEncryptedPsuAuthentication() {
    return startAuthorisationWithEncryptedPsuAuthentication;
  }

  public void setStartAuthorisationWithEncryptedPsuAuthentication(HrefType startAuthorisationWithEncryptedPsuAuthentication) {
    this.startAuthorisationWithEncryptedPsuAuthentication = startAuthorisationWithEncryptedPsuAuthentication;
  }

  public LinksPaymentInitiationCancel startAuthorisationWithAuthenticationMethodSelection(HrefType startAuthorisationWithAuthenticationMethodSelection) {
    this.startAuthorisationWithAuthenticationMethodSelection = startAuthorisationWithAuthenticationMethodSelection;
    return this;
  }

  /**
   * Get startAuthorisationWithAuthenticationMethodSelection
   * @return startAuthorisationWithAuthenticationMethodSelection
   **/
  @Schema(description = "")

    @Valid
    public HrefType getStartAuthorisationWithAuthenticationMethodSelection() {
    return startAuthorisationWithAuthenticationMethodSelection;
  }

  public void setStartAuthorisationWithAuthenticationMethodSelection(HrefType startAuthorisationWithAuthenticationMethodSelection) {
    this.startAuthorisationWithAuthenticationMethodSelection = startAuthorisationWithAuthenticationMethodSelection;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    LinksPaymentInitiationCancel _linksPaymentInitiationCancel = (LinksPaymentInitiationCancel) o;
    return Objects.equals(this.startAuthorisation, _linksPaymentInitiationCancel.startAuthorisation) &&
        Objects.equals(this.startAuthorisationWithPsuIdentification, _linksPaymentInitiationCancel.startAuthorisationWithPsuIdentification) &&
        Objects.equals(this.startAuthorisationWithPsuAuthentication, _linksPaymentInitiationCancel.startAuthorisationWithPsuAuthentication) &&
        Objects.equals(this.startAuthorisationWithEncryptedPsuAuthentication, _linksPaymentInitiationCancel.startAuthorisationWithEncryptedPsuAuthentication) &&
        Objects.equals(this.startAuthorisationWithAuthenticationMethodSelection, _linksPaymentInitiationCancel.startAuthorisationWithAuthenticationMethodSelection) &&
        super.equals(o);
  }

  @Override
  public int hashCode() {
    return Objects.hash(startAuthorisation, startAuthorisationWithPsuIdentification, startAuthorisationWithPsuAuthentication, startAuthorisationWithEncryptedPsuAuthentication, startAuthorisationWithAuthenticationMethodSelection, super.hashCode());
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class LinksPaymentInitiationCancel {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
    sb.append("    startAuthorisation: ").append(toIndentedString(startAuthorisation)).append("\n");
    sb.append("    startAuthorisationWithPsuIdentification: ").append(toIndentedString(startAuthorisationWithPsuIdentification)).append("\n");
    sb.append("    startAuthorisationWithPsuAuthentication: ").append(toIndentedString(startAuthorisationWithPsuAuthentication)).append("\n");
    sb.append("    startAuthorisationWithEncryptedPsuAuthentication: ").append(toIndentedString(startAuthorisationWithEncryptedPsuAuthentication)).append("\n");
    sb.append("    startAuthorisationWithAuthenticationMethodSelection: ").append(toIndentedString(startAuthorisationWithAuthenticationMethodSelection)).append("\n");
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
