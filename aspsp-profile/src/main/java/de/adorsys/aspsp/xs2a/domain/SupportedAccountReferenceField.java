package de.adorsys.aspsp.xs2a.domain;

import io.swagger.annotations.ApiModel;

@ApiModel(description = "Supported AccountReference Filed", value = "SupportedAccountReferenceField")
public enum SupportedAccountReferenceField {
    IBAN,
    BBAN,
    PAN,
    MASKEDPAN,
    MSISDN
}
