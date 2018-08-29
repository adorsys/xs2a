package de.adorsys.psd2.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;
import java.util.Objects;

/**
 * Reference to an account by the Primary Account Number (PAN) of a card in a masked form.
 */
@ApiModel(description = "Reference to an account by the Primary Account Number (PAN) of a card in a masked form. ")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2018-08-09T18:41:17.591+02:00[Europe/Berlin]")
public class AccountReferenceMaskedPan {

    @JsonProperty("maskedPan")
    private String maskedPan = null;

    @JsonProperty("currency")
    private String currency = null;

    public AccountReferenceMaskedPan maskedPan(String maskedPan) {
        this.maskedPan = maskedPan;
        return this;
    }

    /**
     * Get maskedPan
     *
     * @return maskedPan
     **/
    @ApiModelProperty(required = true)
    @NotNull

    public String getMaskedPan() {
        return maskedPan;
    }

    public void setMaskedPan(String maskedPan) {
        this.maskedPan = maskedPan;
    }

    public AccountReferenceMaskedPan currency(String currency) {
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
        AccountReferenceMaskedPan accountReferenceMaskedPan = (AccountReferenceMaskedPan) o;
        return Objects.equals(this.maskedPan, accountReferenceMaskedPan.maskedPan) &&
            Objects.equals(this.currency, accountReferenceMaskedPan.currency);
    }

    @Override
    public int hashCode() {
        return Objects.hash(maskedPan, currency);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class AccountReferenceMaskedPan {\n");

        sb.append("    maskedPan: ").append(toIndentedString(maskedPan)).append("\n");
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
