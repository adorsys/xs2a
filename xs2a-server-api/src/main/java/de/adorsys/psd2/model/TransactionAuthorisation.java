package de.adorsys.psd2.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;
import java.util.Objects;

/**
 * Content of the body of a Transaction Authorisation Request
 */
@ApiModel(description = "Content of the body of a Transaction Authorisation Request ")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2018-10-11T14:55" +
    ":45.627+02:00[Europe/Berlin]")
public class TransactionAuthorisation {
    @JsonProperty("scaAuthenticationData")
    private String scaAuthenticationData = null;

    public TransactionAuthorisation scaAuthenticationData(String scaAuthenticationData) {
        this.scaAuthenticationData = scaAuthenticationData;
        return this;
    }

    /**
     * Get scaAuthenticationData
     *
     * @return scaAuthenticationData
     **/
    @ApiModelProperty(required = true, value = "")
    @NotNull
    public String getScaAuthenticationData() {
        return scaAuthenticationData;
    }

    public void setScaAuthenticationData(String scaAuthenticationData) {
        this.scaAuthenticationData = scaAuthenticationData;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TransactionAuthorisation transactionAuthorisation = (TransactionAuthorisation) o;
        return Objects.equals(this.scaAuthenticationData, transactionAuthorisation.scaAuthenticationData);
    }

    @Override
    public int hashCode() {
        return Objects.hash(scaAuthenticationData);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class TransactionAuthorisation {\n");
        sb.append("    scaAuthenticationData: ").append(toIndentedString(scaAuthenticationData)).append("\n");
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

