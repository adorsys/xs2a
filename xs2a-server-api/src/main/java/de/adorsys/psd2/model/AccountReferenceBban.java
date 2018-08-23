package de.adorsys.psd2.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;
import java.util.Objects;

/**
 * Reference to an Account.  This data elements is used for payment accounts which have no IBAN.
 */
@ApiModel(description = "Reference to an Account.  This data elements is used for payment accounts which have no IBAN. ")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2018-08-09T18:41:17.591+02:00[Europe/Berlin]")
public class AccountReferenceBban {

    @JsonProperty("bban")
    private String bban = null;

    @JsonProperty("currency")
    private String currency = null;

    public AccountReferenceBban bban(String bban) {
        this.bban = bban;
        return this;
    }

    /**
     * Get bban
     *
     * @return bban
     **/
    @ApiModelProperty(required = true)
    @NotNull

    public String getBban() {
        return bban;
    }

    public void setBban(String bban) {
        this.bban = bban;
    }

    public AccountReferenceBban currency(String currency) {
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
        AccountReferenceBban accountReferenceBban = (AccountReferenceBban) o;
        return Objects.equals(this.bban, accountReferenceBban.bban) &&
            Objects.equals(this.currency, accountReferenceBban.currency);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bban, currency);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class AccountReferenceBban {\n");

        sb.append("    bban: ").append(toIndentedString(bban)).append("\n");
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
