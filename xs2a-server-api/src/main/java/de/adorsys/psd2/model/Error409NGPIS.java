package de.adorsys.psd2.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * NextGenPSD2 specific definition of reporting error information in case of a HTTP error code 409.
 */
@Schema(description = "NextGenPSD2 specific definition of reporting error information in case of a HTTP error code 409. ")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2022-05-06T13:00:42.214155+03:00[Europe/Kiev]")


public class Error409NGPIS   {
  @JsonProperty("tppMessages")
  @Valid
  private List<TppMessage409PIS> tppMessages = null;

  @JsonProperty("_links")
  private LinksAll _links = null;

  public Error409NGPIS tppMessages(List<TppMessage409PIS> tppMessages) {
    this.tppMessages = tppMessages;
    return this;
  }

  public Error409NGPIS addTppMessagesItem(TppMessage409PIS tppMessagesItem) {
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
  @Schema(description = "")
      @Valid
    public List<TppMessage409PIS> getTppMessages() {
    return tppMessages;
  }

  public void setTppMessages(List<TppMessage409PIS> tppMessages) {
    this.tppMessages = tppMessages;
  }

  public Error409NGPIS _links(LinksAll _links) {
    this._links = _links;
    return this;
  }

  /**
   * Get _links
   * @return _links
   **/
  @Schema(description = "")

    @Valid
    public LinksAll getLinks() {
    return _links;
  }

  public void setLinks(LinksAll _links) {
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
    Error409NGPIS error409NGPIS = (Error409NGPIS) o;
    return Objects.equals(this.tppMessages, error409NGPIS.tppMessages) &&
        Objects.equals(this._links, error409NGPIS._links);
  }

  @Override
  public int hashCode() {
    return Objects.hash(tppMessages, _links);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Error409NGPIS {\n");

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
