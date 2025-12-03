package de.adorsys.psd2.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * ErrorNG403VOP
 */

@JsonTypeName("error_NG_403_VOP")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2025-09-18T08:00:00.499860948+01:00[Africa/Douala]", comments = "Generator version: 7.15.0")
public class ErrorNG403VOP {

  @Valid
  private List<@Valid ClientMessageInformation403VOP> apiClientMessages = new ArrayList<>();

  private Optional<Links> links = Optional.empty();

  public ErrorNG403VOP apiClientMessages(List<@Valid ClientMessageInformation403VOP> apiClientMessages) {
    this.apiClientMessages = apiClientMessages;
    return this;
  }

  public ErrorNG403VOP addApiClientMessagesItem(ClientMessageInformation403VOP apiClientMessagesItem) {
    if (this.apiClientMessages == null) {
      this.apiClientMessages = new ArrayList<>();
    }
    this.apiClientMessages.add(apiClientMessagesItem);
    return this;
  }

  /**
   * Get apiClientMessages
   * @return apiClientMessages
   */
  @Valid
  @Schema(name = "apiClientMessages", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("apiClientMessages")
  public List<@Valid ClientMessageInformation403VOP> getApiClientMessages() {
    return apiClientMessages;
  }

  public void setApiClientMessages(List<@Valid ClientMessageInformation403VOP> apiClientMessages) {
    this.apiClientMessages = apiClientMessages;
  }

  public ErrorNG403VOP links(Links links) {
    this.links = Optional.ofNullable(links);
    return this;
  }

  /**
   * Get links
   * @return links
   */
  @Valid
  @Schema(name = "_links", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("_links")
  public Optional<Links> getLinks() {
    return links;
  }

  public void setLinks(Optional<Links> links) {
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
    ErrorNG403VOP errorNG403VOP = (ErrorNG403VOP) o;
    return Objects.equals(this.apiClientMessages, errorNG403VOP.apiClientMessages) &&
        Objects.equals(this.links, errorNG403VOP.links);
  }

  @Override
  public int hashCode() {
    return Objects.hash(apiClientMessages, links);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ErrorNG403VOP {\n");
    sb.append("    apiClientMessages: ").append(toIndentedString(apiClientMessages)).append("\n");
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

