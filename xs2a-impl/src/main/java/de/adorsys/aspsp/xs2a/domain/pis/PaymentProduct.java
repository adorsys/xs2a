package de.adorsys.aspsp.xs2a.domain.pis;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.annotations.ApiModel;

@ApiModel(description = "PaymentProduct", value = "Payment products of ASPSP")
public enum PaymentProduct {
    SCT("sepa-credit-transfers"),
    ISCT("instant-sepa-credit-transfers"),
    T2P("target-2-payments"),
    CBCT("cross-border-credit-transfers");

    private String code;

    @JsonCreator
    PaymentProduct(String code) {
        this.code = code;
    }

    @JsonValue
    public String getCode() {
        return code;
    }

    public static PaymentProduct forValue(String code) {
        for (PaymentProduct prod : values()) {
            if (prod.code.equals(code)) {
                return prod;
            }
        }
        throw new IllegalArgumentException();
    }
}
