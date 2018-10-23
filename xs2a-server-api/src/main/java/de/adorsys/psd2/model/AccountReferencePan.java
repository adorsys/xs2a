package de.adorsys.psd2.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;
import java.util.Objects;

/**
 * Reference to an account by the Primary Account Number (PAN) of a card, can be tokenised by the ASPSP due to PCI
 * DSS requirements.
 */
@ApiModel(description = "Reference to an account by the Primary Account Number (PAN) of a card, can be tokenised by " +
    "the ASPSP due to PCI DSS requirements. ")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2018-10-11T14:55" +
    ":45.627+02:00[Europe/Berlin]")
public class AccountReferencePan {
    @JsonProperty("pan")
    private String pan = null;
    @JsonProperty("currency")
    private String currency = null;

    public AccountReferencePan pan(String pan) {
        this.pan = pan;
        return this;
    }

    /**
     * Get pan
     *
     * @return pan
     **/
    @ApiModelProperty(required = true, value = "")
    @NotNull
    public String getPan() {
        return pan;
    }

    public void setPan(String pan) {
        this.pan = pan;
    }

    public AccountReferencePan currency(String currency) {
        this.currency = currency;
        return this;
    }

    /**
     * Get currency
     *
     * @return currency
     **/
    @ApiModelProperty(value = "")
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
        AccountReferencePan accountReferencePan = (AccountReferencePan) o;
        return Objects.equals(this.pan, accountReferencePan.pan) && Objects.equals(this.currency,
            accountReferencePan.currency);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pan, currency);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class AccountReferencePan {\n");
        sb.append("    pan: ").append(toIndentedString(pan)).append("\n");
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

