package de.adorsys.psd2.model;

import io.swagger.annotations.ApiModel;
import org.springframework.validation.annotation.Validated;

import java.util.Objects;

/**
 * If equals &#39;true&#39;, the transaction will involve specific transaction cost as shown by the ASPSP in their
 * public price list or as agreed between ASPSP and PSU. If equals &#39;false&#39;, the transaction will not involve
 * additional specific transaction costs to the PSU.
 */
@ApiModel(description = "If equals 'true', the transaction will involve specific transaction cost as shown by the " +
    "ASPSP in their public price list or as agreed between ASPSP and PSU. If equals 'false', the transaction will not" +
    " involve additional specific transaction costs to the PSU. ")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2018-10-11T14:55" +
    ":45.627+02:00[Europe/Berlin]")
public class TransactionFeeIndicator {
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
        sb.append("class TransactionFeeIndicator {\n");
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

