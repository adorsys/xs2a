/*
 * Copyright 2018-2022 adorsys GmbH & Co KG
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
 * Body of the JSON response with SCA Status.
 */
@Schema(description = "Body of the JSON response with SCA Status.")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2022-10-26T13:16:54.081225+03:00[Europe/Kiev]")


public class ScaStatusResponse {
    @JsonProperty("scaStatus")
    private ScaStatus scaStatus = null;

    @JsonProperty("psuMessage")
    private String psuMessage = null;

    @JsonProperty("trustedBeneficiaryFlag")
    private Boolean trustedBeneficiaryFlag = null;

    @JsonProperty("_links")
  private Map _links = null;

  @JsonProperty("tppMessages")
  @Valid
  private List<TppMessageGeneric> tppMessages = null;

  public ScaStatusResponse scaStatus(ScaStatus scaStatus) {
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

  public ScaStatusResponse psuMessage(String psuMessage) {
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

  public ScaStatusResponse trustedBeneficiaryFlag(Boolean trustedBeneficiaryFlag) {
    this.trustedBeneficiaryFlag = trustedBeneficiaryFlag;
      return this;
  }

    /**
     * Additional Service: Trusted Benificiaries Within this data element, the ASPSP might optionally communicate towards the TPP whether the creditor was part of the related trusted beneficiary list.  This attribute is only contained in case of a final scaStatus.
     *
     * @return trustedBeneficiaryFlag
     **/
    @Schema(example = "false", description = "Additional Service: Trusted Benificiaries Within this data element, the ASPSP might optionally communicate towards the TPP whether the creditor was part of the related trusted beneficiary list.  This attribute is only contained in case of a final scaStatus. ")
    @JsonProperty("trustedBeneficiaryFlag")

    public Boolean isTrustedBeneficiaryFlag() {
        return trustedBeneficiaryFlag;
  }

  public void setTrustedBeneficiaryFlag(Boolean trustedBeneficiaryFlag) {
    this.trustedBeneficiaryFlag = trustedBeneficiaryFlag;
  }

  public ScaStatusResponse _links(Map _links) {
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

  public ScaStatusResponse tppMessages(List<TppMessageGeneric> tppMessages) {
    this.tppMessages = tppMessages;
    return this;
  }

  public ScaStatusResponse addTppMessagesItem(TppMessageGeneric tppMessagesItem) {
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
    ScaStatusResponse scaStatusResponse = (ScaStatusResponse) o;
    return Objects.equals(this.scaStatus, scaStatusResponse.scaStatus) &&
        Objects.equals(this.psuMessage, scaStatusResponse.psuMessage) &&
        Objects.equals(this.trustedBeneficiaryFlag, scaStatusResponse.trustedBeneficiaryFlag) &&
        Objects.equals(this._links, scaStatusResponse._links) &&
        Objects.equals(this.tppMessages, scaStatusResponse.tppMessages);
  }

  @Override
  public int hashCode() {
    return Objects.hash(scaStatus, psuMessage, trustedBeneficiaryFlag, _links, tppMessages);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ScaStatusResponse {\n");

    sb.append("    scaStatus: ").append(toIndentedString(scaStatus)).append("\n");
    sb.append("    psuMessage: ").append(toIndentedString(psuMessage)).append("\n");
    sb.append("    trustedBeneficiaryFlag: ").append(toIndentedString(trustedBeneficiaryFlag)).append("\n");
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
