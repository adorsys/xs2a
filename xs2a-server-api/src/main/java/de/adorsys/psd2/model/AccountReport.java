package de.adorsys.psd2.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Map;
import java.util.Objects;

/**
 * JSON based account report.
 */
@ApiModel(description = "JSON based account report.")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2018-08-09T18:41:17.591+02:00[Europe/Berlin]")
public class AccountReport {

    @JsonProperty("booked")
    private TransactionList booked = null;

    @JsonProperty("pending")
    private TransactionList pending = null;

    @JsonProperty("_links")
    private Map _links = null;

    public AccountReport booked(TransactionList booked) {
        this.booked = booked;
        return this;
    }

    /**
     * Get booked
     *
     * @return booked
     **/
    @ApiModelProperty(required = true)
    @NotNull
    @Valid
    public TransactionList getBooked() {
        return booked;
    }

    public void setBooked(TransactionList booked) {
        this.booked = booked;
    }

    public AccountReport pending(TransactionList pending) {
        this.pending = pending;
        return this;
    }

    /**
     * Get pending
     *
     * @return pending
     **/
    @ApiModelProperty
    @Valid
    public TransactionList getPending() {
        return pending;
    }

    public void setPending(TransactionList pending) {
        this.pending = pending;
    }

    public AccountReport _links(Map _links) {
        this._links = _links;
        return this;
    }

    /**
     * Get _links
     *
     * @return _links
     **/
    @ApiModelProperty(required = true)
    @NotNull
    @Valid
    public Map getLinks() {
        return _links;
    }

    public void setLinks(Map _links) {
        this._links = _links;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AccountReport accountReport = (AccountReport) o;
        return Objects.equals(this.booked, accountReport.booked) &&
            Objects.equals(this.pending, accountReport.pending) &&
            Objects.equals(this._links, accountReport._links);
    }

    @Override
    public int hashCode() {
        return Objects.hash(booked, pending, _links);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class AccountReport {\n");

        sb.append("    booked: ").append(toIndentedString(booked)).append("\n");
        sb.append("    pending: ").append(toIndentedString(pending)).append("\n");
        sb.append("    _links: ").append(toIndentedString(_links)).append("\n");
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
