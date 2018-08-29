package de.adorsys.psd2.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Objects;

/**
 * JSON Body of a establish signing basket request
 */
@ApiModel(description = "JSON Body of a establish signing basket request ")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2018-08-09T18:41:17.591+02:00[Europe/Berlin]")
public class SigningBasket {

    @JsonProperty("paymentIds")
    private PaymentIdList paymentIds = null;

    @JsonProperty("consentIds")
    private ConsentIdList consentIds = null;

    public SigningBasket paymentIds(PaymentIdList paymentIds) {
        this.paymentIds = paymentIds;
        return this;
    }

    /**
     * Get paymentIds
     *
     * @return paymentIds
     **/
    @ApiModelProperty(required = true)
    @NotNull
    @Valid
    public PaymentIdList getPaymentIds() {
        return paymentIds;
    }

    public void setPaymentIds(PaymentIdList paymentIds) {
        this.paymentIds = paymentIds;
    }

    public SigningBasket consentIds(ConsentIdList consentIds) {
        this.consentIds = consentIds;
        return this;
    }

    /**
     * Get consentIds
     *
     * @return consentIds
     **/
    @ApiModelProperty(required = true)
    @NotNull
    @Valid
    public ConsentIdList getConsentIds() {
        return consentIds;
    }

    public void setConsentIds(ConsentIdList consentIds) {
        this.consentIds = consentIds;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SigningBasket signingBasket = (SigningBasket) o;
        return Objects.equals(this.paymentIds, signingBasket.paymentIds) &&
            Objects.equals(this.consentIds, signingBasket.consentIds);
    }

    @Override
    public int hashCode() {
        return Objects.hash(paymentIds, consentIds);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class SigningBasket {\n");

        sb.append("    paymentIds: ").append(toIndentedString(paymentIds)).append("\n");
        sb.append("    consentIds: ").append(toIndentedString(consentIds)).append("\n");
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
