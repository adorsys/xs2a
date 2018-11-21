package de.adorsys.psd2.model;

import io.swagger.annotations.ApiModel;
import org.springframework.validation.annotation.Validated;

import java.util.Objects;

/**
 * If this element equals &#39;true&#39;, the PSU prefers only one booking entry. If this element equals &#39;
 * false&#39;, the PSU prefers individual booking of all contained individual transactions.  The ASPSP will follow
 * this preference according to contracts agreed on with the PSU.
 */
@ApiModel(description = "If this element equals 'true', the PSU prefers only one booking entry. If this element " +
    "equals 'false', the PSU prefers individual booking of all contained individual transactions.  The ASPSP will " +
    "follow this preference according to contracts agreed on with the PSU. ")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2018-10-11T14:55" +
    ":45.627+02:00[Europe/Berlin]")
public class BatchBookingPreferred {
    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class BatchBookingPreferred {\n");
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

