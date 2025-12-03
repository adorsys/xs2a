package de.adorsys.psd2.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * BulkVerificationOfParty202Response
 */

@JsonTypeName("bulkVerificationOfParty_202_response")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2025-09-18T08:00:00.499860948+01:00[Africa/Douala]", comments = "Generator version: 7.15.0")
public class BulkVerificationOfParty202Response {

  private Optional<UUID> vopBulkId = Optional.empty();

  private VOPBulkStatus vopBulkStatus;

  @Valid
  private List<@Valid PartyVerificationResponse> partyVerificationResponses = new ArrayList<>();

  private Optional<Links202VOPbulk> links = Optional.empty();

  public BulkVerificationOfParty202Response() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public BulkVerificationOfParty202Response(VOPBulkStatus vopBulkStatus) {
    this.vopBulkStatus = vopBulkStatus;
  }

  public BulkVerificationOfParty202Response vopBulkId(UUID vopBulkId) {
    this.vopBulkId = Optional.ofNullable(vopBulkId);
    return this;
  }

  /**
   * Technical resource identification of the VOP bulk.
   * @return vopBulkId
   */
  @Valid
  @Schema(name = "vopBulkId", example = "99435c7e-ad88-49ec-a2ad-99ddcb1f5555", description = "Technical resource identification of the VOP bulk. ", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("vopBulkId")
  public Optional<UUID> getVopBulkId() {
    return vopBulkId;
  }

  public void setVopBulkId(Optional<UUID> vopBulkId) {
    this.vopBulkId = vopBulkId;
  }

  public BulkVerificationOfParty202Response vopBulkStatus(VOPBulkStatus vopBulkStatus) {
    this.vopBulkStatus = vopBulkStatus;
    return this;
  }

  /**
   *
   * @return vopBulkStatus
   */
  @NotNull @Valid
  @Schema(name = "vopBulkStatus", description = "", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("vopBulkStatus")
  public VOPBulkStatus getVopBulkStatus() {
    return vopBulkStatus;
  }

  public void setVopBulkStatus(VOPBulkStatus vopBulkStatus) {
    this.vopBulkStatus = vopBulkStatus;
  }

  public BulkVerificationOfParty202Response partyVerificationResponses(List<@Valid PartyVerificationResponse> partyVerificationResponses) {
    this.partyVerificationResponses = partyVerificationResponses;
    return this;
  }

  public BulkVerificationOfParty202Response addPartyVerificationResponsesItem(PartyVerificationResponse partyVerificationResponsesItem) {
    if (this.partyVerificationResponses == null) {
      this.partyVerificationResponses = new ArrayList<>();
    }
    this.partyVerificationResponses.add(partyVerificationResponsesItem);
    return this;
  }

  /**
   * A non-empty array of related party verification results.
   * @return partyVerificationResponses
   */
  @Valid @Size(min = 1)
  @Schema(name = "partyVerificationResponses", description = "A non-empty array of related party verification results. ", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("partyVerificationResponses")
  public List<@Valid PartyVerificationResponse> getPartyVerificationResponses() {
    return partyVerificationResponses;
  }

  public void setPartyVerificationResponses(List<@Valid PartyVerificationResponse> partyVerificationResponses) {
    this.partyVerificationResponses = partyVerificationResponses;
  }

  public BulkVerificationOfParty202Response links(Links202VOPbulk links) {
    this.links = Optional.ofNullable(links);
    return this;
  }

  /**
   * It will provide at least a hyperlink of href type \"status\".
   * @return links
   */
  @Valid
  @Schema(name = "_links", description = "It will provide at least a hyperlink of href type \"status\". ", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("_links")
  public Optional<Links202VOPbulk> getLinks() {
    return links;
  }

  public void setLinks(Optional<Links202VOPbulk> links) {
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
    BulkVerificationOfParty202Response bulkVerificationOfParty202Response = (BulkVerificationOfParty202Response) o;
    return Objects.equals(this.vopBulkId, bulkVerificationOfParty202Response.vopBulkId) &&
        Objects.equals(this.vopBulkStatus, bulkVerificationOfParty202Response.vopBulkStatus) &&
        Objects.equals(this.partyVerificationResponses, bulkVerificationOfParty202Response.partyVerificationResponses) &&
        Objects.equals(this.links, bulkVerificationOfParty202Response.links);
  }

  @Override
  public int hashCode() {
    return Objects.hash(vopBulkId, vopBulkStatus, partyVerificationResponses, links);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class BulkVerificationOfParty202Response {\n");
    sb.append("    vopBulkId: ").append(toIndentedString(vopBulkId)).append("\n");
    sb.append("    vopBulkStatus: ").append(toIndentedString(vopBulkStatus)).append("\n");
    sb.append("    partyVerificationResponses: ").append(toIndentedString(partyVerificationResponses)).append("\n");
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

