/*
 * Copyright 2018-2023 adorsys GmbH & Co KG
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
 * contact us at psd2@adorsys.com.
 */

package de.adorsys.psd2.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Body of the response for a successful payment initiation status request in case of an JSON based endpoint. *Remark:* If the PSU does not complete a required SCA within the required timeframe the payment resource&#x27;s status must be set to \&quot;RJCT\&quot;. Particularly, if a multi-level-SCA is required and the number of successful SCAs during the required timeframe is insufficient, the status must also be set to \&quot;RJCT\&quot;.
 */
@Schema(description = "Body of the response for a successful payment initiation status request in case of an JSON based endpoint. *Remark:* If the PSU does not complete a required SCA within the required timeframe the payment resource's status must be set to \"RJCT\". Particularly, if a multi-level-SCA is required and the number of successful SCAs during the required timeframe is insufficient, the status must also be set to \"RJCT\".")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2022-10-26T13:16:54.081225+03:00[Europe/Kiev]")


public class PaymentInitiationStatusResponse200Json   {
  @JsonProperty("transactionStatus")
  private TransactionStatus transactionStatus = null;

  @JsonProperty("fundsAvailable")
  private Boolean fundsAvailable = null;

  @JsonProperty("psuMessage")
  private String psuMessage = null;

  @JsonProperty("_links")
  private Map _links = null;

  @JsonProperty("tppMessages")
  @Valid
  private List<TppMessageGeneric> tppMessages = null;

  public PaymentInitiationStatusResponse200Json transactionStatus(TransactionStatus transactionStatus) {
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
     * Equals true if sufficient funds are available at the time of the request, false otherwise.  This datalemenet is allways contained in a confirmation of funds response.  This data element is contained in a payment status response,  if supported by the ASPSP, if a funds check has been performed and  if the transactionStatus is \"ACTC\", \"ACWC\" or \"ACCP\".
     *
     * @return fundsAvailable
     **/
    @Schema(description = "Equals true if sufficient funds are available at the time of the request, false otherwise.  This datalemenet is allways contained in a confirmation of funds response.  This data element is contained in a payment status response,  if supported by the ASPSP, if a funds check has been performed and  if the transactionStatus is \"ACTC\", \"ACWC\" or \"ACCP\". ")
    @JsonProperty("fundsAvailable")

    public Boolean isFundsAvailable() {
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
     * Text to be displayed to the PSU.
     *
     * @return psuMessage
     **/
    @Schema(description = "Text to be displayed to the PSU.")
    @JsonProperty("psuMessage")

    @Size(max = 500)
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

  public PaymentInitiationStatusResponse200Json tppMessages(List<TppMessageGeneric> tppMessages) {
    this.tppMessages = tppMessages;
    return this;
  }

  public PaymentInitiationStatusResponse200Json addTppMessagesItem(TppMessageGeneric tppMessagesItem) {
    if (this.tppMessages == null) {
      this.tppMessages = new ArrayList<>();
    }
    this.tppMessages.add(tppMessagesItem);
      return this;
  }

    /**
     * Messages to the TPP on operational issues.
     *
     * @return tppMessages
     **/
    @Schema(description = "Messages to the TPP on operational issues.")
    @JsonProperty("tppMessages")
    @Valid
    public List<TppMessageGeneric> getTppMessages() {
    return tppMessages;
  }

  public void setTppMessages(List<TppMessageGeneric> tppMessages) {
    this.tppMessages = tppMessages;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PaymentInitiationStatusResponse200Json paymentInitiationStatusResponse200Json = (PaymentInitiationStatusResponse200Json) o;
    return Objects.equals(this.transactionStatus, paymentInitiationStatusResponse200Json.transactionStatus) &&
        Objects.equals(this.fundsAvailable, paymentInitiationStatusResponse200Json.fundsAvailable) &&
        Objects.equals(this.psuMessage, paymentInitiationStatusResponse200Json.psuMessage) &&
        Objects.equals(this._links, paymentInitiationStatusResponse200Json._links) &&
        Objects.equals(this.tppMessages, paymentInitiationStatusResponse200Json.tppMessages);
  }

  @Override
  public int hashCode() {
    return Objects.hash(transactionStatus, fundsAvailable, psuMessage, _links, tppMessages);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class PaymentInitiationStatusResponse200Json {\n");

    sb.append("    transactionStatus: ").append(toIndentedString(transactionStatus)).append("\n");
    sb.append("    fundsAvailable: ").append(toIndentedString(fundsAvailable)).append("\n");
    sb.append("    psuMessage: ").append(toIndentedString(psuMessage)).append("\n");
    sb.append("    _links: ").append(toIndentedString(_links)).append("\n");
    sb.append("    tppMessages: ").append(toIndentedString(tppMessages)).append("\n");
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
