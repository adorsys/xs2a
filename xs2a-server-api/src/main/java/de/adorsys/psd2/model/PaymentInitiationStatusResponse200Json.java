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
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2021-10-20T16:47:52.806132+03:00[Europe/Kiev]")

public class PaymentInitiationStatusResponse200Json   {
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
  private List<TppMessageGeneric> tppMessage = null;

  public PaymentInitiationStatusResponse200Json transactionStatus(TransactionStatus transactionStatus) {
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

  public PaymentInitiationStatusResponse200Json fundsAvailable(Boolean fundsAvailable) {
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

  public PaymentInitiationStatusResponse200Json psuMessage(String psuMessage) {
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

  public PaymentInitiationStatusResponse200Json _links(Map _links) {
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

  public PaymentInitiationStatusResponse200Json tppMessage(List<TppMessageGeneric> tppMessage) {
    this.tppMessage = tppMessage;
    return this;
  }

  public PaymentInitiationStatusResponse200Json addTppMessageItem(TppMessageGeneric tppMessageItem) {
    if (this.tppMessage == null) {
      this.tppMessage = new ArrayList<>();
    }
    this.tppMessage.add(tppMessageItem);
    return this;
  }

  /**
   * Messages to the TPP on operational issues.
   * @return tppMessage
  **/
  @ApiModelProperty(value = "Messages to the TPP on operational issues.")

  @Valid


  @JsonProperty("tppMessage")
  public List<TppMessageGeneric> getTppMessage() {
    return tppMessage;
  }

  public void setTppMessage(List<TppMessageGeneric> tppMessage) {
    this.tppMessage = tppMessage;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
}    PaymentInitiationStatusResponse200Json paymentInitiationStatusResponse200Json = (PaymentInitiationStatusResponse200Json) o;
    return Objects.equals(this.transactionStatus, paymentInitiationStatusResponse200Json.transactionStatus) &&
    Objects.equals(this.fundsAvailable, paymentInitiationStatusResponse200Json.fundsAvailable) &&
    Objects.equals(this.psuMessage, paymentInitiationStatusResponse200Json.psuMessage) &&
    Objects.equals(this._links, paymentInitiationStatusResponse200Json._links) &&
    Objects.equals(this.tppMessage, paymentInitiationStatusResponse200Json.tppMessage);
  }

  @Override
  public int hashCode() {
    return Objects.hash(transactionStatus, fundsAvailable, psuMessage, _links, tppMessage);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class PaymentInitiationStatusResponse200Json {\n");

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

