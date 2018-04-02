package de.adorsys.aspsp.xs2a.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Currency;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "Account Reference", value = "AccountReference")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AccountReference {

    @JsonIgnore
    private String accountId;

    @ApiModelProperty(value = "IBAN: This data element can be used in the body of the CreateConsentReq Request Message for retrieving account access consent from this payment account", example = "DE2310010010123456789")
    private String iban;

    @ApiModelProperty(value = "BBAN: This data elements is used for payment accounts which have no IBAN", example = "1111111111")
    private String bban;

    @ApiModelProperty(value = "PAN: Primary Account Number (PAN) of a card, can be tokenized by the ASPSP due to PCI DSS requirements.", example = "1111")
    private String pan;

    @ApiModelProperty(value = "MASKEDPAN: Primary Account Number (PAN) of a card in a masked form.", example = "23456xxxxxx1234")
    private String maskedPan;

    @ApiModelProperty(value = "MSISDN: An alias to access a payment account via a registered mobile phone number. This alias might be needed e.g. in the payment initiation service, cp. Section 5.3.1. The support of this alias must be explicitly documented by the ASPSP for the corresponding API calls.", example = "0172/1111111")
    private String msisdn;

    @ApiModelProperty(value = "Codes following ISO 4217", example = "EUR")
    private Currency currency;
}
