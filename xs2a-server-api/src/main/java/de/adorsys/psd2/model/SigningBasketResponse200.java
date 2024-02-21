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
import java.util.Objects;

/**
 * Body of the JSON response for a successful get signing basket request.    * &#x27;payments&#x27;: payment initiations which shall be authorised through this signing basket.   * &#x27;consents&#x27;: consent objects which shall be authorised through this signing basket.   * &#x27;transactionStatus&#x27;: Only the codes RCVD, ACTC, RJCT are used.   * &#x27;_links&#x27;: The ASPSP might integrate hyperlinks to indicate next (authorisation) steps to be taken.
 */
@Schema(description = "Body of the JSON response for a successful get signing basket request.    * 'payments': payment initiations which shall be authorised through this signing basket.   * 'consents': consent objects which shall be authorised through this signing basket.   * 'transactionStatus': Only the codes RCVD, ACTC, RJCT are used.   * '_links': The ASPSP might integrate hyperlinks to indicate next (authorisation) steps to be taken. ")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2022-10-26T13:16:54.081225+03:00[Europe/Kiev]")


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
     *
     * @return payments
     **/
    @Schema(description = "")
    @JsonProperty("payments")

    @Valid
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
     *
     * @return consents
     **/
    @Schema(description = "")
    @JsonProperty("consents")

    @Valid
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
     *
     * @return transactionStatus
     **/
    @Schema(required = true, description = "")
    @JsonProperty("transactionStatus")
    @NotNull

    @Valid
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
     *
     * @return _links
     **/
    @Schema(description = "")
    @JsonProperty("_links")

    @Valid
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
    }
    SigningBasketResponse200 signingBasketResponse200 = (SigningBasketResponse200) o;
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
