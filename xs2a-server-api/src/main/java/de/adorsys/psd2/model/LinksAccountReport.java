package de.adorsys.psd2.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Objects;

/**
 * LinksAccountReport
 */
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2021-11-05T12:22:49.487689+02:00[Europe/Kiev]")

public class LinksAccountReport extends HashMap<String, HrefType>  {
  @JsonProperty("account")
  private HrefType account = null;

  @JsonProperty("first")
  private HrefType first = null;

  @JsonProperty("next")
  private HrefType next = null;

  @JsonProperty("previous")
  private HrefType previous = null;

  @JsonProperty("last")
  private HrefType last = null;

  public LinksAccountReport account(HrefType account) {
    this.account = account;
    return this;
  }

  /**
   * Get account
   * @return account
  **/
  @ApiModelProperty(required = true, value = "")
  @NotNull

  @Valid


  @JsonProperty("account")
  public HrefType getAccount() {
    return account;
  }

  public void setAccount(HrefType account) {
    this.account = account;
  }

  public LinksAccountReport first(HrefType first) {
    this.first = first;
    return this;
  }

  /**
   * Get first
   * @return first
  **/
  @ApiModelProperty(value = "")

  @Valid


  @JsonProperty("first")
  public HrefType getFirst() {
    return first;
  }

  public void setFirst(HrefType first) {
    this.first = first;
  }

  public LinksAccountReport next(HrefType next) {
    this.next = next;
    return this;
  }

  /**
   * Get next
   * @return next
  **/
  @ApiModelProperty(value = "")

  @Valid


  @JsonProperty("next")
  public HrefType getNext() {
    return next;
  }

  public void setNext(HrefType next) {
    this.next = next;
  }

  public LinksAccountReport previous(HrefType previous) {
    this.previous = previous;
    return this;
  }

  /**
   * Get previous
   * @return previous
  **/
  @ApiModelProperty(value = "")

  @Valid


  @JsonProperty("previous")
  public HrefType getPrevious() {
    return previous;
  }

  public void setPrevious(HrefType previous) {
    this.previous = previous;
  }

  public LinksAccountReport last(HrefType last) {
    this.last = last;
    return this;
  }

  /**
   * Get last
   * @return last
  **/
  @ApiModelProperty(value = "")

  @Valid


  @JsonProperty("last")
  public HrefType getLast() {
    return last;
  }

  public void setLast(HrefType last) {
    this.last = last;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
}
    if (!super.equals(o)) {
    return false;
    }
    LinksAccountReport _linksAccountReport = (LinksAccountReport) o;
    return Objects.equals(this.account, _linksAccountReport.account) &&
    Objects.equals(this.first, _linksAccountReport.first) &&
    Objects.equals(this.next, _linksAccountReport.next) &&
    Objects.equals(this.previous, _linksAccountReport.previous) &&
    Objects.equals(this.last, _linksAccountReport.last);
  }

  @Override
  public int hashCode() {
    return Objects.hash(account, first, next, previous, last, super.hashCode());
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class LinksAccountReport {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
    sb.append("    account: ").append(toIndentedString(account)).append("\n");
    sb.append("    first: ").append(toIndentedString(first)).append("\n");
    sb.append("    next: ").append(toIndentedString(next)).append("\n");
    sb.append("    previous: ").append(toIndentedString(previous)).append("\n");
    sb.append("    last: ").append(toIndentedString(last)).append("\n");
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

