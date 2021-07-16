package de.adorsys.psd2.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Body of the response for a successful payment initiation status request in case of an JSON based endpoint.
 */
@ApiModel(description = "Body of the response for a successful payment initiation status request in case of an JSON based endpoint.")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2021-07-15T10:28:21.780938+03:00[Europe/Kiev]")

public class PaymentInitiationStatusResponse202Json   {
  @JsonProperty("transactionStatus")
  private TransactionStatus transactionStatus = null;

  @JsonProperty("fundsAvailable")
  private Boolean fundsAvailable = null;

  @JsonProperty("psuMessage")
  private String psuMessage = null;

  @JsonProperty("_links")
  private Map _links = null;

  @JsonProperty("tppMessage")
  @Valid
  private List<TppMessageInitiationStatusResponse200> tppMessage = new ArrayList<>();

  public PaymentInitiationStatusResponse202Json transactionStatus(TransactionStatus transactionStatus) {
    this.transactionStatus = transactionStatus;
    return this;
  }

  /**
   * Get transactionStatus
   * @return transactionStatus
  **/
  @ApiModelProperty(required = true, value = "")
  @NotNull

  @Valid


  @JsonProperty("transactionStatus")
  public TransactionStatus getTransactionStatus() {
    return transactionStatus;
  }

  public void setTransactionStatus(TransactionStatus transactionStatus) {
    this.transactionStatus = transactionStatus;
  }

  public PaymentInitiationStatusResponse202Json fundsAvailable(Boolean fundsAvailable) {
    this.fundsAvailable = fundsAvailable;
    return this;
  }

  /**
   * Get fundsAvailable
   * @return fundsAvailable
  **/
  @ApiModelProperty(value = "")



  @JsonProperty("fundsAvailable")
  public Boolean getFundsAvailable() {
    return fundsAvailable;
  }

  public void setFundsAvailable(Boolean fundsAvailable) {
    this.fundsAvailable = fundsAvailable;
  }

  public PaymentInitiationStatusResponse202Json psuMessage(String psuMessage) {
    this.psuMessage = psuMessage;
    return this;
  }

  /**
   * Get psuMessage
   * @return psuMessage
  **/
  @ApiModelProperty(value = "")

@Size(max=500)

  @JsonProperty("psuMessage")
  public String getPsuMessage() {
    return psuMessage;
  }

  public void setPsuMessage(String psuMessage) {
    this.psuMessage = psuMessage;
  }

  public PaymentInitiationStatusResponse202Json _links(Map _links) {
    this._links = _links;
    return this;
  }

  /**
   * Get _links
   * @return _links
  **/
  @ApiModelProperty(value = "")

  @Valid


  @JsonProperty("_links")
  public Map getLinks() {
    return _links;
  }

  public void setLinks(Map _links) {
    this._links = _links;
  }

  public PaymentInitiationStatusResponse202Json tppMessage(List<TppMessageInitiationStatusResponse200> tppMessage) {
    this.tppMessage = tppMessage;
    return this;
  }

  public PaymentInitiationStatusResponse202Json addTppMessageItem(TppMessageInitiationStatusResponse200 tppMessageItem) {
    this.tppMessage.add(tppMessageItem);
    return this;
  }

  /**
   * Messages to the TPP on operational issues.
   * @return tppMessage
  **/
  @ApiModelProperty(required = true, value = "Messages to the TPP on operational issues.")
  @NotNull

  @Valid


  @JsonProperty("tppMessage")
  public List<TppMessageInitiationStatusResponse200> getTppMessage() {
    return tppMessage;
  }

  public void setTppMessage(List<TppMessageInitiationStatusResponse200> tppMessage) {
    this.tppMessage = tppMessage;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
}    PaymentInitiationStatusResponse202Json paymentInitiationStatusResponse202Json = (PaymentInitiationStatusResponse202Json) o;
    return Objects.equals(this.transactionStatus, paymentInitiationStatusResponse202Json.transactionStatus) &&
    Objects.equals(this.fundsAvailable, paymentInitiationStatusResponse202Json.fundsAvailable) &&
    Objects.equals(this.psuMessage, paymentInitiationStatusResponse202Json.psuMessage) &&
    Objects.equals(this._links, paymentInitiationStatusResponse202Json._links) &&
    Objects.equals(this.tppMessage, paymentInitiationStatusResponse202Json.tppMessage);
  }

  @Override
  public int hashCode() {
    return Objects.hash(transactionStatus, fundsAvailable, psuMessage, _links, tppMessage);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class PaymentInitiationStatusResponse202Json {\n");

    sb.append("    transactionStatus: ").append(toIndentedString(transactionStatus)).append("\n");
    sb.append("    fundsAvailable: ").append(toIndentedString(fundsAvailable)).append("\n");
    sb.append("    psuMessage: ").append(toIndentedString(psuMessage)).append("\n");
    sb.append("    _links: ").append(toIndentedString(_links)).append("\n");
    sb.append("    tppMessage: ").append(toIndentedString(tppMessage)).append("\n");
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

