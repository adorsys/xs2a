package de.adorsys.psd2.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;
import java.util.Objects;

/**
 * An alias to access a payment account via a registered mobile phone number.
 */
@ApiModel(description = "An alias to access a payment account via a registered mobile phone number. ")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2018-08-09T18:41:17.591+02:00[Europe/Berlin]")
public class AccountReferenceMsisdn {

    @JsonProperty("msisdn")
    private String msisdn = null;

    @JsonProperty("currency")
    private String currency = null;

    public AccountReferenceMsisdn msisdn(String msisdn) {
        this.msisdn = msisdn;
        return this;
    }

    /**
     * Get msisdn
     *
     * @return msisdn
     **/
    @ApiModelProperty(required = true)
    @NotNull

    public String getMsisdn() {
        return msisdn;
    }

    public void setMsisdn(String msisdn) {
        this.msisdn = msisdn;
    }

    public AccountReferenceMsisdn currency(String currency) {
        this.currency = currency;
        return this;
    }

    /**
     * Get currency
     *
     * @return currency
     **/
    @ApiModelProperty
    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AccountReferenceMsisdn accountReferenceMsisdn = (AccountReferenceMsisdn) o;
        return Objects.equals(this.msisdn, accountReferenceMsisdn.msisdn) &&
            Objects.equals(this.currency, accountReferenceMsisdn.currency);
    }

    @Override
    public int hashCode() {
        return Objects.hash(msisdn, currency);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class AccountReferenceMsisdn {\n");

        sb.append("    msisdn: ").append(toIndentedString(msisdn)).append("\n");
        sb.append("    currency: ").append(toIndentedString(currency)).append("\n");
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
