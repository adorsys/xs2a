package de.adorsys.psd2.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * CreditorConfirmation200Response
 */

@JsonTypeName("creditorConfirmation_200_response")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2025-09-18T08:00:00.499860948+01:00[Africa/Douala]", comments = "Generator version: 7.15.0")
public class CreditorConfirmation200Response {

  private TransactionStatus transactionStatus;

  private Optional<Challenge> challengeData = Optional.empty();

  @Valid
  private List<@Valid AuthenticationObject> scaMethods = new ArrayList<>();

  private Links200CreditorConfirmation links;

  public CreditorConfirmation200Response() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public CreditorConfirmation200Response(TransactionStatus transactionStatus, Links200CreditorConfirmation links) {
    this.transactionStatus = transactionStatus;
    this.links = links;
  }

  public CreditorConfirmation200Response transactionStatus(TransactionStatus transactionStatus) {
    this.transactionStatus = transactionStatus;
    return this;
  }

  /**
   * Transaction Status.
   * @return transactionStatus
   */
  @NotNull @Valid
  @Schema(name = "transactionStatus", description = "Transaction Status. ", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("transactionStatus")
  public TransactionStatus getTransactionStatus() {
    return transactionStatus;
  }

  public void setTransactionStatus(TransactionStatus transactionStatus) {
    this.transactionStatus = transactionStatus;
  }

  public CreditorConfirmation200Response challengeData(Challenge challengeData) {
    this.challengeData = Optional.ofNullable(challengeData);
    return this;
  }

  /**
   * Challenge data might be contained, if only one authentication method is available, and if the Embedded SCA approach is chosen by the ASPSP.
   * @return challengeData
   */
  @Valid
  @Schema(name = "challengeData", description = "Challenge data might be contained, if only one authentication method is available, and if the Embedded SCA approach is chosen by the ASPSP.  ", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("challengeData")
  public Optional<Challenge> getChallengeData() {
    return challengeData;
  }

  public void setChallengeData(Optional<Challenge> challengeData) {
    this.challengeData = challengeData;
  }

  public CreditorConfirmation200Response scaMethods(List<@Valid AuthenticationObject> scaMethods) {
    this.scaMethods = scaMethods;
    return this;
  }

  public CreditorConfirmation200Response addScaMethodsItem(AuthenticationObject scaMethodsItem) {
    if (this.scaMethods == null) {
      this.scaMethods = new ArrayList<>();
    }
    this.scaMethods.add(scaMethodsItem);
    return this;
  }

  /**
   * Might be contained, if several authentication methods are available and not yet selected. (name, type)
   * @return scaMethods
   */
  @Valid
  @Schema(name = "scaMethods", description = "Might be contained, if several authentication methods are available and not yet selected. (name, type) ", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("scaMethods")
  public List<@Valid AuthenticationObject> getScaMethods() {
    return scaMethods;
  }

  public void setScaMethods(List<@Valid AuthenticationObject> scaMethods) {
    this.scaMethods = scaMethods;
  }

  public CreditorConfirmation200Response links(Links200CreditorConfirmation links) {
    this.links = links;
    return this;
  }

  /**
   * The ASPSP is putting here all relevant authorisation related links, depending on the authorisation status, cp. [oFA-PFSM].
   * @return links
   */
  @NotNull @Valid
  @Schema(name = "_links", description = "The ASPSP is putting here all relevant authorisation related links, depending on the authorisation status, cp. [oFA-PFSM]. ", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("_links")
  public Links200CreditorConfirmation getLinks() {
    return links;
  }

  public void setLinks(Links200CreditorConfirmation links) {
    this.links = links;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CreditorConfirmation200Response creditorConfirmation200Response = (CreditorConfirmation200Response) o;
    return Objects.equals(this.transactionStatus, creditorConfirmation200Response.transactionStatus) &&
        Objects.equals(this.challengeData, creditorConfirmation200Response.challengeData) &&
        Objects.equals(this.scaMethods, creditorConfirmation200Response.scaMethods) &&
        Objects.equals(this.links, creditorConfirmation200Response.links);
  }

  @Override
  public int hashCode() {
    return Objects.hash(transactionStatus, challengeData, scaMethods, links);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class CreditorConfirmation200Response {\n");
    sb.append("    transactionStatus: ").append(toIndentedString(transactionStatus)).append("\n");
    sb.append("    challengeData: ").append(toIndentedString(challengeData)).append("\n");
    sb.append("    scaMethods: ").append(toIndentedString(scaMethods)).append("\n");
    sb.append("    links: ").append(toIndentedString(links)).append("\n");
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

