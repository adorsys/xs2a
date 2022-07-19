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
 * Body of the JSON response for an authorisation confirmation request.
 */
@Schema(description = "Body of the JSON response for an authorisation confirmation request.")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2022-05-06T13:00:42.214155+03:00[Europe/Kiev]")


public class AuthorisationConfirmationResponse {
  @JsonProperty("scaStatus")
  private ScaStatusAuthorisationConfirmation scaStatus = null;

  @JsonProperty("_links")
  private Map _links = null;

  @JsonProperty("psuMessage")
  private String psuMessage = null;

  public AuthorisationConfirmationResponse scaStatus(ScaStatusAuthorisationConfirmation scaStatus) {
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
    public ScaStatusAuthorisationConfirmation getScaStatus() {
    return scaStatus;
  }

  public void setScaStatus(ScaStatusAuthorisationConfirmation scaStatus) {
    this.scaStatus = scaStatus;
  }

  public AuthorisationConfirmationResponse _links(LinksAuthorisationConfirmation _links) {
    this._links = _links;
    return this;
  }

  /**
   * Get _links
   * @return _links
   **/
  @Schema(required = true, description = "")
      @NotNull

    @Valid
    public Map getLinks() {
    return _links;
  }

  public void setLinks(Map _links) {
    this._links = _links;
  }

  public AuthorisationConfirmationResponse psuMessage(String psuMessage) {
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
    AuthorisationConfirmationResponse authorisationConfirmationResponse = (AuthorisationConfirmationResponse) o;
    return Objects.equals(this.scaStatus, authorisationConfirmationResponse.scaStatus) &&
        Objects.equals(this._links, authorisationConfirmationResponse._links) &&
        Objects.equals(this.psuMessage, authorisationConfirmationResponse.psuMessage);
  }

  @Override
  public int hashCode() {
    return Objects.hash(scaStatus, _links, psuMessage);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AuthorisationConfirmationResponse {\n");

    sb.append("    scaStatus: ").append(toIndentedString(scaStatus)).append("\n");
    sb.append("    _links: ").append(toIndentedString(_links)).append("\n");
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
