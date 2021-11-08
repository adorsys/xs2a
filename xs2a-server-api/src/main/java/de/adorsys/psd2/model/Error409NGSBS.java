package de.adorsys.psd2.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * NextGenPSD2 specific definition of reporting error information in case of a HTTP error code 409.
 */
@ApiModel(description = "NextGenPSD2 specific definition of reporting error information in case of a HTTP error code 409. ")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2021-11-05T12:22:49.487689+02:00[Europe/Kiev]")

public class Error409NGSBS   {
  @JsonProperty("tppMessages")
  @Valid
  private List<TppMessage409SBS> tppMessages = null;

  @JsonProperty("_links")
  private Map _links = null;

  public Error409NGSBS tppMessages(List<TppMessage409SBS> tppMessages) {
    this.tppMessages = tppMessages;
    return this;
  }

  public Error409NGSBS addTppMessagesItem(TppMessage409SBS tppMessagesItem) {
    if (this.tppMessages == null) {
      this.tppMessages = new ArrayList<>();
    }
    this.tppMessages.add(tppMessagesItem);
    return this;
  }

  /**
   * Get tppMessages
   * @return tppMessages
  **/
  @ApiModelProperty(value = "")

  @Valid


  @JsonProperty("tppMessages")
  public List<TppMessage409SBS> getTppMessages() {
    return tppMessages;
  }

  public void setTppMessages(List<TppMessage409SBS> tppMessages) {
    this.tppMessages = tppMessages;
  }

  public Error409NGSBS _links(Map _links) {
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


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
}    Error409NGSBS error409NGSBS = (Error409NGSBS) o;
    return Objects.equals(this.tppMessages, error409NGSBS.tppMessages) &&
    Objects.equals(this._links, error409NGSBS._links);
  }

  @Override
  public int hashCode() {
    return Objects.hash(tppMessages, _links);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Error409NGSBS {\n");

    sb.append("    tppMessages: ").append(toIndentedString(tppMessages)).append("\n");
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

