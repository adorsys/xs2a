/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.adorsys.psd2.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Objects;

/**
 * Body of the JSON response for a successful get signing basket request.    * &#39;payments&#39;: payment initiations which shall be authorised through this signing basket.   * &#39;consents&#39;: consent objects which shall be authorised through this signing basket.   * &#39;transactionStatus&#39;: Only the codes RCVD, ACTC, RJCT are used.
 */
@ApiModel(description = "Body of the JSON response for a successful get signing basket request.    * 'payments': payment initiations which shall be authorised through this signing basket.   * 'consents': consent objects which shall be authorised through this signing basket.   * 'transactionStatus': Only the codes RCVD, ACTC, RJCT are used. ")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2018-08-09T18:41:17.591+02:00[Europe/Berlin]")
public class SigningBasketResponse200 {
    @JsonProperty("payments")
    private PaymentIdList payments = null;

    @JsonProperty("consents")
    private ConsentIdList consents = null;

    @JsonProperty("transactionStatus")
    private TransactionStatus transactionStatus = null;

    public SigningBasketResponse200 payments(PaymentIdList payments) {
        this.payments = payments;
        return this;
    }

    /**
     * Get payments
     *
     * @return payments
     **/
    @ApiModelProperty
    @Valid
    public PaymentIdList getPayments() {
        return payments;
    }

    public void setPayments(PaymentIdList payments) {
        this.payments = payments;
    }

    public SigningBasketResponse200 consents(ConsentIdList consents) {
        this.consents = consents;
        return this;
    }

    /**
     * Get consents
     *
     * @return consents
     **/
    @ApiModelProperty
    @Valid
    public ConsentIdList getConsents() {
        return consents;
    }

    public void setConsents(ConsentIdList consents) {
        this.consents = consents;
    }

    public SigningBasketResponse200 transactionStatus(TransactionStatus transactionStatus) {
        this.transactionStatus = transactionStatus;
        return this;
    }

    /**
     * Get transactionStatus
     *
     * @return transactionStatus
     **/
    @ApiModelProperty(required = true)
    @NotNull
    @Valid
    public TransactionStatus getTransactionStatus() {
        return transactionStatus;
    }

    public void setTransactionStatus(TransactionStatus transactionStatus) {
        this.transactionStatus = transactionStatus;
    }


    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SigningBasketResponse200 signingBasketResponse200 = (SigningBasketResponse200) o;
        return Objects.equals(this.payments, signingBasketResponse200.payments) && Objects.equals(this.consents, signingBasketResponse200.consents) && Objects.equals(this.transactionStatus, signingBasketResponse200.transactionStatus);
    }

    @Override
    public int hashCode() {
        return Objects.hash(payments, consents, transactionStatus);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class SigningBasketResponse200 {\n");

        sb.append("    payments: ").append(toIndentedString(payments)).append("\n");
        sb.append("    consents: ").append(toIndentedString(consents)).append("\n");
        sb.append("    transactionStatus: ").append(toIndentedString(transactionStatus)).append("\n");
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

