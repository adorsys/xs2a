package de.adorsys.psd2.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Objects;

/**
 * LinksTransactionDetails
 */
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2018-10-11T14:55" +
    ":45.627+02:00[Europe/Berlin]")
public class LinksTransactionDetails extends HashMap<String, String> {
    @JsonProperty("transactionDetails")
    private String transactionDetails = null;

    public LinksTransactionDetails transactionDetails(String transactionDetails) {
        this.transactionDetails = transactionDetails;
        return this;
    }

    /**
     * Get transactionDetails
     *
     * @return transactionDetails
     **/
    @ApiModelProperty(required = true, value = "")
    @NotNull
    public String getTransactionDetails() {
        return transactionDetails;
    }

    public void setTransactionDetails(String transactionDetails) {
        this.transactionDetails = transactionDetails;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        LinksTransactionDetails _linksTransactionDetails = (LinksTransactionDetails) o;
        return Objects.equals(this.transactionDetails, _linksTransactionDetails.transactionDetails) && super.equals(o);
    }

    @Override
    public int hashCode() {
        return Objects.hash(transactionDetails, super.hashCode());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class LinksTransactionDetails {\n");
        sb.append("    ").append(toIndentedString(super.toString())).append("\n");
        sb.append("    transactionDetails: ").append(toIndentedString(transactionDetails)).append("\n");
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

