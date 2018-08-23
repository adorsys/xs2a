package de.adorsys.psd2.model;

import io.swagger.annotations.ApiModel;
import org.springframework.validation.annotation.Validated;

import java.util.Objects;

/**
 * This parameter is requesting a valid until date for the requested consent.  The content is the local ASPSP date in ISO-Date Format, e.g. 2017-10-30.   If a maximal available date is requested, a date in far future is to be used: \&quot;9999-12-31\&quot;.  The consent object to be retrieved by the GET Consent Request will contain the adjusted date.
 */
@ApiModel(description = "This parameter is requesting a valid until date for the requested consent.  The content is the local ASPSP date in ISO-Date Format, e.g. 2017-10-30.   If a maximal available date is requested, a date in far future is to be used: \"9999-12-31\".  The consent object to be retrieved by the GET Consent Request will contain the adjusted date. ")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2018-08-09T18:41:17.591+02:00[Europe/Berlin]")
public class ValidUntil {

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
        sb.append("class ValidUntil {\n");

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
