package de.adorsys.psd2.model;


import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
/**
 * Links200CreditorConfirmation
 */

@JsonTypeName("_links_200_Creditor_Confirmation")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2025-09-18T08:00:00.499860948+01:00[Africa/Douala]", comments = "Generator version: 7.15.0")
@SuppressWarnings("PMD")
public class Links200CreditorConfirmation {

  private Optional<HrefType> scaRedirect = Optional.empty();

  private Optional<HrefType> scaOAuth = Optional.empty();

  private Optional<HrefType> confirmation = Optional.empty();

  private Optional<HrefType> startAuthorisation = Optional.empty();

  private Optional<HrefType> startAuthorisationWithPsuIdentification = Optional.empty();

  private Optional<HrefType> startAuthorisationWithPsuAuthentication = Optional.empty();

  private Optional<HrefType> startAuthorisationWithEncryptedPsuAuthentication = Optional.empty();

  private Optional<HrefType> startAuthorisationWithAuthenticationMethodSelection = Optional.empty();

  private Optional<HrefType> startAuthorisationWithTransactionAuthorisation = Optional.empty();

  private Optional<HrefType> self = Optional.empty();

  private Optional<HrefType> status = Optional.empty();

  private Optional<HrefType> scaStatus = Optional.empty();

  @Valid
  private List<@Valid HrefType> encryptionCertificates = new ArrayList<>();

  public Links200CreditorConfirmation scaRedirect(HrefType scaRedirect) {
    this.scaRedirect = Optional.ofNullable(scaRedirect);
    return this;
  }

  /**
   * Get scaRedirect
   * @return scaRedirect
   */
  @Valid
  @Schema(name = "scaRedirect", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("scaRedirect")
  public Optional<HrefType> getScaRedirect() {
    return scaRedirect;
  }

  public void setScaRedirect(Optional<HrefType> scaRedirect) {
    this.scaRedirect = scaRedirect;
  }

  public Links200CreditorConfirmation scaOAuth(HrefType scaOAuth) {
    this.scaOAuth = Optional.ofNullable(scaOAuth);
    return this;
  }

  /**
   * Get scaOAuth
   * @return scaOAuth
   */
  @Valid
  @Schema(name = "scaOAuth", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("scaOAuth")
  public Optional<HrefType> getScaOAuth() {
    return scaOAuth;
  }

  public void setScaOAuth(Optional<HrefType> scaOAuth) {
    this.scaOAuth = scaOAuth;
  }

  public Links200CreditorConfirmation confirmation(HrefType confirmation) {
    this.confirmation = Optional.ofNullable(confirmation);
    return this;
  }

  /**
   * Get confirmation
   * @return confirmation
   */
  @Valid
  @Schema(name = "confirmation", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("confirmation")
  public Optional<HrefType> getConfirmation() {
    return confirmation;
  }

  public void setConfirmation(Optional<HrefType> confirmation) {
    this.confirmation = confirmation;
  }

  public Links200CreditorConfirmation startAuthorisation(HrefType startAuthorisation) {
    this.startAuthorisation = Optional.ofNullable(startAuthorisation);
    return this;
  }

  /**
   * Get startAuthorisation
   * @return startAuthorisation
   */
  @Valid
  @Schema(name = "startAuthorisation", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("startAuthorisation")
  public Optional<HrefType> getStartAuthorisation() {
    return startAuthorisation;
  }

  public void setStartAuthorisation(Optional<HrefType> startAuthorisation) {
    this.startAuthorisation = startAuthorisation;
  }

  public Links200CreditorConfirmation startAuthorisationWithPsuIdentification(HrefType startAuthorisationWithPsuIdentification) {
    this.startAuthorisationWithPsuIdentification = Optional.ofNullable(startAuthorisationWithPsuIdentification);
    return this;
  }

  /**
   * Get startAuthorisationWithPsuIdentification
   * @return startAuthorisationWithPsuIdentification
   */
  @Valid
  @Schema(name = "startAuthorisationWithPsuIdentification", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("startAuthorisationWithPsuIdentification")
  public Optional<HrefType> getStartAuthorisationWithPsuIdentification() {
    return startAuthorisationWithPsuIdentification;
  }

  public void setStartAuthorisationWithPsuIdentification(Optional<HrefType> startAuthorisationWithPsuIdentification) {
    this.startAuthorisationWithPsuIdentification = startAuthorisationWithPsuIdentification;
  }

  public Links200CreditorConfirmation startAuthorisationWithPsuAuthentication(HrefType startAuthorisationWithPsuAuthentication) {
    this.startAuthorisationWithPsuAuthentication = Optional.ofNullable(startAuthorisationWithPsuAuthentication);
    return this;
  }

  /**
   * Get startAuthorisationWithPsuAuthentication
   * @return startAuthorisationWithPsuAuthentication
   */
  @Valid
  @Schema(name = "startAuthorisationWithPsuAuthentication", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("startAuthorisationWithPsuAuthentication")
  public Optional<HrefType> getStartAuthorisationWithPsuAuthentication() {
    return startAuthorisationWithPsuAuthentication;
  }

  public void setStartAuthorisationWithPsuAuthentication(Optional<HrefType> startAuthorisationWithPsuAuthentication) {
    this.startAuthorisationWithPsuAuthentication = startAuthorisationWithPsuAuthentication;
  }

  public Links200CreditorConfirmation startAuthorisationWithEncryptedPsuAuthentication(HrefType startAuthorisationWithEncryptedPsuAuthentication) {
    this.startAuthorisationWithEncryptedPsuAuthentication = Optional.ofNullable(startAuthorisationWithEncryptedPsuAuthentication);
    return this;
  }

  /**
   * Get startAuthorisationWithEncryptedPsuAuthentication
   * @return startAuthorisationWithEncryptedPsuAuthentication
   */
  @Valid
  @Schema(name = "startAuthorisationWithEncryptedPsuAuthentication", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("startAuthorisationWithEncryptedPsuAuthentication")
  public Optional<HrefType> getStartAuthorisationWithEncryptedPsuAuthentication() {
    return startAuthorisationWithEncryptedPsuAuthentication;
  }

  public void setStartAuthorisationWithEncryptedPsuAuthentication(Optional<HrefType> startAuthorisationWithEncryptedPsuAuthentication) {
    this.startAuthorisationWithEncryptedPsuAuthentication = startAuthorisationWithEncryptedPsuAuthentication;
  }

  public Links200CreditorConfirmation startAuthorisationWithAuthenticationMethodSelection(HrefType startAuthorisationWithAuthenticationMethodSelection) {
    this.startAuthorisationWithAuthenticationMethodSelection = Optional.ofNullable(startAuthorisationWithAuthenticationMethodSelection);
    return this;
  }

  /**
   * Get startAuthorisationWithAuthenticationMethodSelection
   * @return startAuthorisationWithAuthenticationMethodSelection
   */
  @Valid
  @Schema(name = "startAuthorisationWithAuthenticationMethodSelection", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("startAuthorisationWithAuthenticationMethodSelection")
  public Optional<HrefType> getStartAuthorisationWithAuthenticationMethodSelection() {
    return startAuthorisationWithAuthenticationMethodSelection;
  }

  public void setStartAuthorisationWithAuthenticationMethodSelection(Optional<HrefType> startAuthorisationWithAuthenticationMethodSelection) {
    this.startAuthorisationWithAuthenticationMethodSelection = startAuthorisationWithAuthenticationMethodSelection;
  }

  public Links200CreditorConfirmation startAuthorisationWithTransactionAuthorisation(HrefType startAuthorisationWithTransactionAuthorisation) {
    this.startAuthorisationWithTransactionAuthorisation = Optional.ofNullable(startAuthorisationWithTransactionAuthorisation);
    return this;
  }

  /**
   * Get startAuthorisationWithTransactionAuthorisation
   * @return startAuthorisationWithTransactionAuthorisation
   */
  @Valid
  @Schema(name = "startAuthorisationWithTransactionAuthorisation", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("startAuthorisationWithTransactionAuthorisation")
  public Optional<HrefType> getStartAuthorisationWithTransactionAuthorisation() {
    return startAuthorisationWithTransactionAuthorisation;
  }

  public void setStartAuthorisationWithTransactionAuthorisation(Optional<HrefType> startAuthorisationWithTransactionAuthorisation) {
    this.startAuthorisationWithTransactionAuthorisation = startAuthorisationWithTransactionAuthorisation;
  }

  public Links200CreditorConfirmation self(HrefType self) {
    this.self = Optional.ofNullable(self);
    return this;
  }

  /**
   * Get self
   * @return self
   */
  @Valid
  @Schema(name = "self", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("self")
  public Optional<HrefType> getSelf() {
    return self;
  }

  public void setSelf(Optional<HrefType> self) {
    this.self = self;
  }

  public Links200CreditorConfirmation status(HrefType status) {
    this.status = Optional.ofNullable(status);
    return this;
  }

  /**
   * Get status
   * @return status
   */
  @Valid
  @Schema(name = "status", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("status")
  public Optional<HrefType> getStatus() {
    return status;
  }

  public void setStatus(Optional<HrefType> status) {
    this.status = status;
  }

  public Links200CreditorConfirmation scaStatus(HrefType scaStatus) {
    this.scaStatus = Optional.ofNullable(scaStatus);
    return this;
  }

  /**
   * Get scaStatus
   * @return scaStatus
   */
  @Valid
  @Schema(name = "scaStatus", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("scaStatus")
  public Optional<HrefType> getScaStatus() {
    return scaStatus;
  }

  public void setScaStatus(Optional<HrefType> scaStatus) {
    this.scaStatus = scaStatus;
  }

  public Links200CreditorConfirmation encryptionCertificates(List<@Valid HrefType> encryptionCertificates) {
    this.encryptionCertificates = encryptionCertificates;
    return this;
  }

  public Links200CreditorConfirmation addEncryptionCertificatesItem(HrefType encryptionCertificatesItem) {
    if (this.encryptionCertificates == null) {
      this.encryptionCertificates = new ArrayList<>();
    }
    this.encryptionCertificates.add(encryptionCertificatesItem);
    return this;
  }

  /**
   * Get encryptionCertificates
   * @return encryptionCertificates
   */
  @Valid
  @Schema(name = "encryptionCertificates", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("encryptionCertificates")
  public List<@Valid HrefType> getEncryptionCertificates() {
    return encryptionCertificates;
  }

  public void setEncryptionCertificates(List<@Valid HrefType> encryptionCertificates) {
    this.encryptionCertificates = encryptionCertificates;
  }
    /**
    * A container for additional, undeclared properties.
    * This is a holder for any undeclared properties as specified with
    * the 'additionalProperties' keyword in the OAS document.
    */
    private Map<String, HrefType> additionalProperties;

    /**
    * Set the additional (undeclared) property with the specified name and value.
    * If the property does not already exist, create it otherwise replace it.
    */
    @JsonAnySetter
    public Links200CreditorConfirmation putAdditionalProperty(String key, HrefType value) {
        if (this.additionalProperties == null) {
            this.additionalProperties = new HashMap<String, HrefType>();
        }
        this.additionalProperties.put(key, value);
        return this;
    }

    /**
    * Return the additional (undeclared) property.
    */
    @JsonAnyGetter
    public Map<String, HrefType> getAdditionalProperties() {
        return additionalProperties;
    }

    /**
    * Return the additional (undeclared) property with the specified name.
    */
    public HrefType getAdditionalProperty(String key) {
        if (this.additionalProperties == null) {
            return null;
        }
        return this.additionalProperties.get(key);
    }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Links200CreditorConfirmation links200CreditorConfirmation = (Links200CreditorConfirmation) o;
    return Objects.equals(this.scaRedirect, links200CreditorConfirmation.scaRedirect) &&
        Objects.equals(this.scaOAuth, links200CreditorConfirmation.scaOAuth) &&
        Objects.equals(this.confirmation, links200CreditorConfirmation.confirmation) &&
        Objects.equals(this.startAuthorisation, links200CreditorConfirmation.startAuthorisation) &&
        Objects.equals(this.startAuthorisationWithPsuIdentification, links200CreditorConfirmation.startAuthorisationWithPsuIdentification) &&
        Objects.equals(this.startAuthorisationWithPsuAuthentication, links200CreditorConfirmation.startAuthorisationWithPsuAuthentication) &&
        Objects.equals(this.startAuthorisationWithEncryptedPsuAuthentication, links200CreditorConfirmation.startAuthorisationWithEncryptedPsuAuthentication) &&
        Objects.equals(this.startAuthorisationWithAuthenticationMethodSelection, links200CreditorConfirmation.startAuthorisationWithAuthenticationMethodSelection) &&
        Objects.equals(this.startAuthorisationWithTransactionAuthorisation, links200CreditorConfirmation.startAuthorisationWithTransactionAuthorisation) &&
        Objects.equals(this.self, links200CreditorConfirmation.self) &&
        Objects.equals(this.status, links200CreditorConfirmation.status) &&
        Objects.equals(this.scaStatus, links200CreditorConfirmation.scaStatus) &&
        Objects.equals(this.encryptionCertificates, links200CreditorConfirmation.encryptionCertificates) &&
    Objects.equals(this.additionalProperties, links200CreditorConfirmation.additionalProperties);
  }

  @Override
  public int hashCode() {
    return Objects.hash(scaRedirect, scaOAuth, confirmation, startAuthorisation, startAuthorisationWithPsuIdentification, startAuthorisationWithPsuAuthentication, startAuthorisationWithEncryptedPsuAuthentication, startAuthorisationWithAuthenticationMethodSelection, startAuthorisationWithTransactionAuthorisation, self, status, scaStatus, encryptionCertificates, additionalProperties);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Links200CreditorConfirmation {\n");
    sb.append("    scaRedirect: ").append(toIndentedString(scaRedirect)).append("\n");
    sb.append("    scaOAuth: ").append(toIndentedString(scaOAuth)).append("\n");
    sb.append("    confirmation: ").append(toIndentedString(confirmation)).append("\n");
    sb.append("    startAuthorisation: ").append(toIndentedString(startAuthorisation)).append("\n");
    sb.append("    startAuthorisationWithPsuIdentification: ").append(toIndentedString(startAuthorisationWithPsuIdentification)).append("\n");
    sb.append("    startAuthorisationWithPsuAuthentication: ").append(toIndentedString(startAuthorisationWithPsuAuthentication)).append("\n");
    sb.append("    startAuthorisationWithEncryptedPsuAuthentication: ").append(toIndentedString(startAuthorisationWithEncryptedPsuAuthentication)).append("\n");
    sb.append("    startAuthorisationWithAuthenticationMethodSelection: ").append(toIndentedString(startAuthorisationWithAuthenticationMethodSelection)).append("\n");
    sb.append("    startAuthorisationWithTransactionAuthorisation: ").append(toIndentedString(startAuthorisationWithTransactionAuthorisation)).append("\n");
    sb.append("    self: ").append(toIndentedString(self)).append("\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
    sb.append("    scaStatus: ").append(toIndentedString(scaStatus)).append("\n");
    sb.append("    encryptionCertificates: ").append(toIndentedString(encryptionCertificates)).append("\n");

    sb.append("    additionalProperties: ").append(toIndentedString(additionalProperties)).append("\n");
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

