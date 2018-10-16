package de.adorsys.psd2.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Objects;

/**
 * LinksAccountReport
 */
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2018-10-11T14:55" +
    ":45.627+02:00[Europe/Berlin]")
public class LinksAccountReport extends HashMap<String, String> {
    @JsonProperty("account")
    private String account = null;
    @JsonProperty("first")
    private String first = null;
    @JsonProperty("next")
    private String next = null;
    @JsonProperty("previous")
    private String previous = null;
    @JsonProperty("last")
    private String last = null;

    public LinksAccountReport account(String account) {
        this.account = account;
        return this;
    }

    /**
     * Get account
     *
     * @return account
     **/
    @ApiModelProperty(required = true, value = "")
    @NotNull
    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public LinksAccountReport first(String first) {
        this.first = first;
        return this;
    }

    /**
     * Get first
     *
     * @return first
     **/
    @ApiModelProperty(value = "")
    public String getFirst() {
        return first;
    }

    public void setFirst(String first) {
        this.first = first;
    }

    public LinksAccountReport next(String next) {
        this.next = next;
        return this;
    }

    /**
     * Get next
     *
     * @return next
     **/
    @ApiModelProperty(value = "")
    public String getNext() {
        return next;
    }

    public void setNext(String next) {
        this.next = next;
    }

    public LinksAccountReport previous(String previous) {
        this.previous = previous;
        return this;
    }

    /**
     * Get previous
     *
     * @return previous
     **/
    @ApiModelProperty(value = "")
    public String getPrevious() {
        return previous;
    }

    public void setPrevious(String previous) {
        this.previous = previous;
    }

    public LinksAccountReport last(String last) {
        this.last = last;
        return this;
    }

    /**
     * Get last
     *
     * @return last
     **/
    @ApiModelProperty(value = "")
    public String getLast() {
        return last;
    }

    public void setLast(String last) {
        this.last = last;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        LinksAccountReport _linksAccountReport = (LinksAccountReport) o;
        return Objects.equals(this.account, _linksAccountReport.account) && Objects.equals(this.first,
            _linksAccountReport.first) && Objects.equals(this.next, _linksAccountReport.next) && Objects.equals(this.previous, _linksAccountReport.previous) && Objects.equals(this.last, _linksAccountReport.last) && super.equals(o);
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
    private String toIndentedString(java.lang.Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }
}

