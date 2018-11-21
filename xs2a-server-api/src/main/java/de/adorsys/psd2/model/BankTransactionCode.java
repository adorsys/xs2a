package de.adorsys.psd2.model;

import io.swagger.annotations.ApiModel;
import org.springframework.validation.annotation.Validated;

import java.util.Objects;

/**
 * Bank transaction code as used by the ASPSP and using the sub elements of this structured code defined by ISO 20022
 * .  This code type is concatenating the three ISO20022 Codes   * Domain Code,   * Family Code, and   * SubFamiliy
 * Code by hyphens, resulting in “DomainCode”-“FamilyCode”-“SubFamilyCode”.
 */
@ApiModel(description = "Bank transaction code as used by the ASPSP and using the sub elements of this structured " +
    "code defined by ISO 20022.  This code type is concatenating the three ISO20022 Codes   * Domain Code,   * Family" +
    " Code, and   * SubFamiliy Code by hyphens, resulting in “DomainCode”-“FamilyCode”-“SubFamilyCode”. ")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2018-10-11T14:55" +
    ":45.627+02:00[Europe/Berlin]")
public class BankTransactionCode {
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
        sb.append("class BankTransactionCode {\n");
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

