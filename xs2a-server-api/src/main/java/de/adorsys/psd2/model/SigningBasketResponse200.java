package de.adorsys.psd2.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Objects;

/**
 * Body of the JSON response for a successful get signing basket request.    * &#39;payments&#39;: payment initiations which shall be authorised through this signing basket.   * &#39;consents&#39;: consent objects which shall be authorised through this signing basket.   * &#39;transactionStatus&#39;: Only the codes RCVD, ACTC, RJCT are used.   * &#39;_links&#39;: The ASPSP might integrate hyperlinks to indicate next (authorisation) steps to be taken.
 */
@ApiModel(description = "Body of the JSON response for a successful get signing basket request.    * 'payments': payment initiations which shall be authorised through this signing basket.   * 'consents': consent objects which shall be authorised through this signing basket.   * 'transactionStatus': Only the codes RCVD, ACTC, RJCT are used.   * '_links': The ASPSP might integrate hyperlinks to indicate next (authorisation) steps to be taken. ")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2021-11-05T12:22:49.487689+02:00[Europe/Kiev]")

public class SigningBasketResponse200   {
  @JsonProperty("payments")
  private PaymentIdList payments = null;

  @JsonProperty("consents")
  private ConsentIdList consents = null;

  @JsonProperty("transactionStatus")
  private TransactionStatusSBS transactionStatus = null;

  @JsonProperty("_links")
  private LinksSigningBasket _links = null;

  public SigningBasketResponse200 payments(PaymentIdList payments) {
    this.payments = payments;
    return this;
  }

  /**
   * Get payments
   * @return payments
  **/
  @ApiModelProperty(value = "")

  @Valid


  @JsonProperty("payments")
  public PaymentIdList getPayments() {
    return payments;
  }

  public void setPayments(PaymentIdList payments) {
    this.payments = payments;
  }

  public SigningBasketResponse200 consents(ConsentIdList consents) {
    this.consents = consents;
    return this;
  }

  /**
   * Get consents
   * @return consents
  **/
  @ApiModelProperty(value = "")

  @Valid


  @JsonProperty("consents")
  public ConsentIdList getConsents() {
    return consents;
  }

  public void setConsents(ConsentIdList consents) {
    this.consents = consents;
  }

  public SigningBasketResponse200 transactionStatus(TransactionStatusSBS transactionStatus) {
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
  public TransactionStatusSBS getTransactionStatus() {
    return transactionStatus;
  }

  public void setTransactionStatus(TransactionStatusSBS transactionStatus) {
    this.transactionStatus = transactionStatus;
  }

  public SigningBasketResponse200 _links(LinksSigningBasket _links) {
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
  public LinksSigningBasket getLinks() {
    return _links;
  }

  public void setLinks(LinksSigningBasket _links) {
    this._links = _links;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
}    SigningBasketResponse200 signingBasketResponse200 = (SigningBasketResponse200) o;
    return Objects.equals(this.payments, signingBasketResponse200.payments) &&
    Objects.equals(this.consents, signingBasketResponse200.consents) &&
    Objects.equals(this.transactionStatus, signingBasketResponse200.transactionStatus) &&
    Objects.equals(this._links, signingBasketResponse200._links);
  }

  @Override
  public int hashCode() {
    return Objects.hash(payments, consents, transactionStatus, _links);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SigningBasketResponse200 {\n");

    sb.append("    payments: ").append(toIndentedString(payments)).append("\n");
    sb.append("    consents: ").append(toIndentedString(consents)).append("\n");
    sb.append("    transactionStatus: ").append(toIndentedString(transactionStatus)).append("\n");
    sb.append("    _links: ").append(toIndentedString(_links)).append("\n");
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

