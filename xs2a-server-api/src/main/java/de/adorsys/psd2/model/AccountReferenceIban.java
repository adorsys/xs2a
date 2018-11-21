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
public class AccountReferenceIban {
    @JsonProperty("iban")
    private String iban = null;
    @JsonProperty("currency")
    private String currency = null;

    public AccountReferenceIban iban(String iban) {
        this.iban = iban;
        return this;
    }

    /**
     * Get iban
     *
     * @return iban
     **/
    @ApiModelProperty(required = true, value = "")
    @NotNull
    public String getIban() {
        return iban;
    }

    public void setIban(String iban) {
        this.iban = iban;
    }

    public AccountReferenceIban currency(String currency) {
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
        AccountReferenceIban accountReferenceIban = (AccountReferenceIban) o;
        return Objects.equals(this.iban, accountReferenceIban.iban) && Objects.equals(this.currency,
            accountReferenceIban.currency);
    }

    @Override
    public int hashCode() {
        return Objects.hash(iban, currency);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class AccountReferenceIban {\n");
        sb.append("    iban: ").append(toIndentedString(iban)).append("\n");
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

