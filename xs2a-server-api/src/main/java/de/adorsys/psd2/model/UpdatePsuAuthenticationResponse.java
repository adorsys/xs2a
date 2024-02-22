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
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Map;
import java.util.Objects;

/**
 * Body of the JSON response for a successful update PSU authentication request.
 */
@Schema(description = "Body of the JSON response for a successful update PSU authentication request.")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2022-10-26T13:16:54.081225+03:00[Europe/Kiev]")


public class UpdatePsuAuthenticationResponse {
    @JsonProperty("transactionFees")
    private Amount transactionFees = null;

    @JsonProperty("currencyConversionFees")
    private Amount currencyConversionFees = null;

    @JsonProperty("estimatedTotalAmount")
    private Amount estimatedTotalAmount = null;

    @JsonProperty("estimatedInterbankSettlementAmount")
  private Amount estimatedInterbankSettlementAmount = null;

  @JsonProperty("chosenScaMethod")
  private ChosenScaMethod chosenScaMethod = null;

  @JsonProperty("challengeData")
  private ChallengeData challengeData = null;

  @JsonProperty("scaMethods")
  private ScaMethods scaMethods = null;

  @JsonProperty("_links")
  private Map _links = null;

  @JsonProperty("scaStatus")
  private ScaStatus scaStatus = null;

  @JsonProperty("psuMessage")
  private String psuMessage = null;

  @JsonProperty("authorisationId")
  private String authorisationId = null;

  public UpdatePsuAuthenticationResponse transactionFees(Amount transactionFees) {
    this.transactionFees = transactionFees;
    return this;
  }

    /**
     * Get transactionFees
     *
     * @return transactionFees
     **/
    @Schema(description = "")
    @JsonProperty("transactionFees")

    @Valid
    public Amount getTransactionFees() {
        return transactionFees;
    }

  public void setTransactionFees(Amount transactionFees) {
    this.transactionFees = transactionFees;
  }

  public UpdatePsuAuthenticationResponse currencyConversionFees(Amount currencyConversionFees) {
    this.currencyConversionFees = currencyConversionFees;
    return this;
  }

    /**
     * Get currencyConversionFees
     *
     * @return currencyConversionFees
     **/
    @Schema(description = "")
    @JsonProperty("currencyConversionFees")

    @Valid
    public Amount getCurrencyConversionFees() {
        return currencyConversionFees;
  }

  public void setCurrencyConversionFees(Amount currencyConversionFees) {
    this.currencyConversionFees = currencyConversionFees;
  }

  public UpdatePsuAuthenticationResponse estimatedTotalAmount(Amount estimatedTotalAmount) {
    this.estimatedTotalAmount = estimatedTotalAmount;
      return this;
  }

    /**
     * Get estimatedTotalAmount
     *
     * @return estimatedTotalAmount
     **/
    @Schema(description = "")
    @JsonProperty("estimatedTotalAmount")

    @Valid
    public Amount getEstimatedTotalAmount() {
        return estimatedTotalAmount;
  }

  public void setEstimatedTotalAmount(Amount estimatedTotalAmount) {
    this.estimatedTotalAmount = estimatedTotalAmount;
  }

  public UpdatePsuAuthenticationResponse estimatedInterbankSettlementAmount(Amount estimatedInterbankSettlementAmount) {
    this.estimatedInterbankSettlementAmount = estimatedInterbankSettlementAmount;
      return this;
  }

    /**
     * Get estimatedInterbankSettlementAmount
     *
     * @return estimatedInterbankSettlementAmount
     **/
    @Schema(description = "")
    @JsonProperty("estimatedInterbankSettlementAmount")

    @Valid
    public Amount getEstimatedInterbankSettlementAmount() {
        return estimatedInterbankSettlementAmount;
  }

  public void setEstimatedInterbankSettlementAmount(Amount estimatedInterbankSettlementAmount) {
    this.estimatedInterbankSettlementAmount = estimatedInterbankSettlementAmount;
  }

  public UpdatePsuAuthenticationResponse chosenScaMethod(ChosenScaMethod chosenScaMethod) {
    this.chosenScaMethod = chosenScaMethod;
      return this;
  }

    /**
     * Get chosenScaMethod
     *
     * @return chosenScaMethod
     **/
    @Schema(description = "")
    @JsonProperty("chosenScaMethod")

    @Valid
    public ChosenScaMethod getChosenScaMethod() {
    return chosenScaMethod;
  }

  public void setChosenScaMethod(ChosenScaMethod chosenScaMethod) {
    this.chosenScaMethod = chosenScaMethod;
  }

  public UpdatePsuAuthenticationResponse challengeData(ChallengeData challengeData) {
      this.challengeData = challengeData;
      return this;
  }

    /**
     * Get challengeData
     *
     * @return challengeData
     **/
    @Schema(description = "")
    @JsonProperty("challengeData")

    @Valid
    public ChallengeData getChallengeData() {
    return challengeData;
  }

  public void setChallengeData(ChallengeData challengeData) {
    this.challengeData = challengeData;
  }

  public UpdatePsuAuthenticationResponse scaMethods(ScaMethods scaMethods) {
      this.scaMethods = scaMethods;
      return this;
  }

    /**
     * Get scaMethods
     *
     * @return scaMethods
     **/
    @Schema(description = "")
    @JsonProperty("scaMethods")

    @Valid
    public ScaMethods getScaMethods() {
    return scaMethods;
  }

  public void setScaMethods(ScaMethods scaMethods) {
    this.scaMethods = scaMethods;
  }

  public UpdatePsuAuthenticationResponse _links(Map _links) {
      this._links = _links;
      return this;
  }

    /**
     * Get _links
     *
     * @return _links
     **/
    @Schema(description = "")
    @JsonProperty("_links")

    @Valid
    public Map getLinks() {
    return _links;
  }

  public void setLinks(Map _links) {
    this._links = _links;
  }

  public UpdatePsuAuthenticationResponse scaStatus(ScaStatus scaStatus) {
      this.scaStatus = scaStatus;
      return this;
  }

    /**
     * Get scaStatus
     *
     * @return scaStatus
     **/
    @Schema(required = true, description = "")
    @JsonProperty("scaStatus")
    @NotNull

  @Valid
  public ScaStatus getScaStatus() {
    return scaStatus;
  }

  public void setScaStatus(ScaStatus scaStatus) {
    this.scaStatus = scaStatus;
  }

  public UpdatePsuAuthenticationResponse psuMessage(String psuMessage) {
      this.psuMessage = psuMessage;
      return this;
  }

    /**
     * Text to be displayed to the PSU.
     *
     * @return psuMessage
     **/
    @Schema(description = "Text to be displayed to the PSU.")
  @JsonProperty("psuMessage")

@Size(max=500)   public String getPsuMessage() {
    return psuMessage;
  }

  public void setPsuMessage(String psuMessage) {
    this.psuMessage = psuMessage;
  }

  public UpdatePsuAuthenticationResponse authorisationId(String authorisationId) {
      this.authorisationId = authorisationId;
      return this;
  }

    /**
     * Resource identification of the related SCA.
     *
     * @return authorisationId
     **/
    @Schema(example = "123auth456", description = "Resource identification of the related SCA.")
  @JsonProperty("authorisationId")

  public String getAuthorisationId() {
    return authorisationId;
  }

  public void setAuthorisationId(String authorisationId) {
    this.authorisationId = authorisationId;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    UpdatePsuAuthenticationResponse updatePsuAuthenticationResponse = (UpdatePsuAuthenticationResponse) o;
    return Objects.equals(this.transactionFees, updatePsuAuthenticationResponse.transactionFees) &&
        Objects.equals(this.currencyConversionFees, updatePsuAuthenticationResponse.currencyConversionFees) &&
        Objects.equals(this.estimatedTotalAmount, updatePsuAuthenticationResponse.estimatedTotalAmount) &&
        Objects.equals(this.estimatedInterbankSettlementAmount, updatePsuAuthenticationResponse.estimatedInterbankSettlementAmount) &&
        Objects.equals(this.chosenScaMethod, updatePsuAuthenticationResponse.chosenScaMethod) &&
        Objects.equals(this.challengeData, updatePsuAuthenticationResponse.challengeData) &&
        Objects.equals(this.scaMethods, updatePsuAuthenticationResponse.scaMethods) &&
        Objects.equals(this._links, updatePsuAuthenticationResponse._links) &&
        Objects.equals(this.scaStatus, updatePsuAuthenticationResponse.scaStatus) &&
        Objects.equals(this.psuMessage, updatePsuAuthenticationResponse.psuMessage) &&
        Objects.equals(this.authorisationId, updatePsuAuthenticationResponse.authorisationId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(transactionFees, currencyConversionFees, estimatedTotalAmount, estimatedInterbankSettlementAmount, chosenScaMethod, challengeData, scaMethods, _links, scaStatus, psuMessage, authorisationId);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class UpdatePsuAuthenticationResponse {\n");

    sb.append("    transactionFees: ").append(toIndentedString(transactionFees)).append("\n");
    sb.append("    currencyConversionFees: ").append(toIndentedString(currencyConversionFees)).append("\n");
    sb.append("    estimatedTotalAmount: ").append(toIndentedString(estimatedTotalAmount)).append("\n");
    sb.append("    estimatedInterbankSettlementAmount: ").append(toIndentedString(estimatedInterbankSettlementAmount)).append("\n");
    sb.append("    chosenScaMethod: ").append(toIndentedString(chosenScaMethod)).append("\n");
    sb.append("    challengeData: ").append(toIndentedString(challengeData)).append("\n");
    sb.append("    scaMethods: ").append(toIndentedString(scaMethods)).append("\n");
    sb.append("    _links: ").append(toIndentedString(_links)).append("\n");
    sb.append("    scaStatus: ").append(toIndentedString(scaStatus)).append("\n");
    sb.append("    psuMessage: ").append(toIndentedString(psuMessage)).append("\n");
    sb.append("    authorisationId: ").append(toIndentedString(authorisationId)).append("\n");
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
