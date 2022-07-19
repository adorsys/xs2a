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
 * Body of the JSON response for a successful select PSU authentication method request.
 */
@Schema(description = "Body of the JSON response for a successful select PSU authentication method request.")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2022-05-06T13:00:42.214155+03:00[Europe/Kiev]")


public class SelectPsuAuthenticationMethodResponse {
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

  @JsonProperty("_links")
  private Map _links = null;

  @JsonProperty("scaStatus")
  private ScaStatus scaStatus = null;

  @JsonProperty("psuMessage")
  private String psuMessage = null;

  public SelectPsuAuthenticationMethodResponse transactionFees(Amount transactionFees) {
    this.transactionFees = transactionFees;
    return this;
  }

  /**
   * Get transactionFees
   * @return transactionFees
   **/
  @Schema(description = "")

    @Valid
    public Amount getTransactionFees() {
    return transactionFees;
  }

  public void setTransactionFees(Amount transactionFees) {
    this.transactionFees = transactionFees;
  }

  public SelectPsuAuthenticationMethodResponse currencyConversionFees(Amount currencyConversionFees) {
    this.currencyConversionFees = currencyConversionFees;
    return this;
  }

  /**
   * Get currencyConversionFees
   * @return currencyConversionFees
   **/
  @Schema(description = "")

    @Valid
    public Amount getCurrencyConversionFees() {
    return currencyConversionFees;
  }

  public void setCurrencyConversionFees(Amount currencyConversionFees) {
    this.currencyConversionFees = currencyConversionFees;
  }

  public SelectPsuAuthenticationMethodResponse estimatedTotalAmount(Amount estimatedTotalAmount) {
    this.estimatedTotalAmount = estimatedTotalAmount;
    return this;
  }

  /**
   * Get estimatedTotalAmount
   * @return estimatedTotalAmount
   **/
  @Schema(description = "")

    @Valid
    public Amount getEstimatedTotalAmount() {
    return estimatedTotalAmount;
  }

  public void setEstimatedTotalAmount(Amount estimatedTotalAmount) {
    this.estimatedTotalAmount = estimatedTotalAmount;
  }

  public SelectPsuAuthenticationMethodResponse estimatedInterbankSettlementAmount(Amount estimatedInterbankSettlementAmount) {
    this.estimatedInterbankSettlementAmount = estimatedInterbankSettlementAmount;
    return this;
  }

  /**
   * Get estimatedInterbankSettlementAmount
   * @return estimatedInterbankSettlementAmount
   **/
  @Schema(description = "")

    @Valid
    public Amount getEstimatedInterbankSettlementAmount() {
    return estimatedInterbankSettlementAmount;
  }

  public void setEstimatedInterbankSettlementAmount(Amount estimatedInterbankSettlementAmount) {
    this.estimatedInterbankSettlementAmount = estimatedInterbankSettlementAmount;
  }

  public SelectPsuAuthenticationMethodResponse chosenScaMethod(ChosenScaMethod chosenScaMethod) {
    this.chosenScaMethod = chosenScaMethod;
    return this;
  }

  /**
   * Get chosenScaMethod
   * @return chosenScaMethod
   **/
  @Schema(description = "")

    @Valid
    public ChosenScaMethod getChosenScaMethod() {
    return chosenScaMethod;
  }

  public void setChosenScaMethod(ChosenScaMethod chosenScaMethod) {
    this.chosenScaMethod = chosenScaMethod;
  }

  public SelectPsuAuthenticationMethodResponse challengeData(ChallengeData challengeData) {
    this.challengeData = challengeData;
    return this;
  }

  /**
   * Get challengeData
   * @return challengeData
   **/
  @Schema(description = "")

    @Valid
    public ChallengeData getChallengeData() {
    return challengeData;
  }

  public void setChallengeData(ChallengeData challengeData) {
    this.challengeData = challengeData;
  }

  public SelectPsuAuthenticationMethodResponse _links(LinksSelectPsuAuthenticationMethod _links) {
    this._links = _links;
    return this;
  }

  /**
   * Get _links
   * @return _links
   **/
  @Schema(description = "")

    @Valid
    public Map getLinks() {
    return _links;
  }

  public void setLinks(Map _links) {
    this._links = _links;
  }

  public SelectPsuAuthenticationMethodResponse scaStatus(ScaStatus scaStatus) {
    this.scaStatus = scaStatus;
    return this;
  }

  /**
   * Get scaStatus
   * @return scaStatus
   **/
  @Schema(required = true, description = "")
      @NotNull

    @Valid
    public ScaStatus getScaStatus() {
    return scaStatus;
  }

  public void setScaStatus(ScaStatus scaStatus) {
    this.scaStatus = scaStatus;
  }

  public SelectPsuAuthenticationMethodResponse psuMessage(String psuMessage) {
    this.psuMessage = psuMessage;
    return this;
  }

  /**
   * Text to be displayed to the PSU.
   * @return psuMessage
   **/
  @Schema(description = "Text to be displayed to the PSU.")

  @Size(max=500)   public String getPsuMessage() {
    return psuMessage;
  }

  public void setPsuMessage(String psuMessage) {
    this.psuMessage = psuMessage;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SelectPsuAuthenticationMethodResponse selectPsuAuthenticationMethodResponse = (SelectPsuAuthenticationMethodResponse) o;
    return Objects.equals(this.transactionFees, selectPsuAuthenticationMethodResponse.transactionFees) &&
        Objects.equals(this.currencyConversionFees, selectPsuAuthenticationMethodResponse.currencyConversionFees) &&
        Objects.equals(this.estimatedTotalAmount, selectPsuAuthenticationMethodResponse.estimatedTotalAmount) &&
        Objects.equals(this.estimatedInterbankSettlementAmount, selectPsuAuthenticationMethodResponse.estimatedInterbankSettlementAmount) &&
        Objects.equals(this.chosenScaMethod, selectPsuAuthenticationMethodResponse.chosenScaMethod) &&
        Objects.equals(this.challengeData, selectPsuAuthenticationMethodResponse.challengeData) &&
        Objects.equals(this._links, selectPsuAuthenticationMethodResponse._links) &&
        Objects.equals(this.scaStatus, selectPsuAuthenticationMethodResponse.scaStatus) &&
        Objects.equals(this.psuMessage, selectPsuAuthenticationMethodResponse.psuMessage);
  }

  @Override
  public int hashCode() {
    return Objects.hash(transactionFees, currencyConversionFees, estimatedTotalAmount, estimatedInterbankSettlementAmount, chosenScaMethod, challengeData, _links, scaStatus, psuMessage);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SelectPsuAuthenticationMethodResponse {\n");

    sb.append("    transactionFees: ").append(toIndentedString(transactionFees)).append("\n");
    sb.append("    currencyConversionFees: ").append(toIndentedString(currencyConversionFees)).append("\n");
    sb.append("    estimatedTotalAmount: ").append(toIndentedString(estimatedTotalAmount)).append("\n");
    sb.append("    estimatedInterbankSettlementAmount: ").append(toIndentedString(estimatedInterbankSettlementAmount)).append("\n");
    sb.append("    chosenScaMethod: ").append(toIndentedString(chosenScaMethod)).append("\n");
    sb.append("    challengeData: ").append(toIndentedString(challengeData)).append("\n");
    sb.append("    _links: ").append(toIndentedString(_links)).append("\n");
    sb.append("    scaStatus: ").append(toIndentedString(scaStatus)).append("\n");
    sb.append("    psuMessage: ").append(toIndentedString(psuMessage)).append("\n");
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
