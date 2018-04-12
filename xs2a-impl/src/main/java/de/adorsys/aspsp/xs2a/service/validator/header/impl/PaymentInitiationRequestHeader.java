package de.adorsys.aspsp.xs2a.service.validator.header.impl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.adorsys.aspsp.xs2a.spi.domain.ContentType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@ApiModel(description = "Payment initiation request header", value = "PaymentInitiationRequestHeader")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentInitiationRequestHeader extends CommonRequestHeader {

    @ApiModelProperty(value = "Content type", example = "application/json")
    @JsonProperty(value = "content-type")
    private ContentType contentType;

    @ApiModelProperty(value = "Might be mandated in the ASPSP's documentation, if OAuth is not chosen as Pre-Step", required = false, example = "PSU-1234")
    @JsonProperty(value = "psu-id")
    private String psuId;

    @ApiModelProperty(value = "Type of the PSU-ID, needed in scenarios where PSUs have several PSU-IDs as access possibility.", required = false, example = "type")
    @JsonProperty(value = "psu-id-type")
    private String psuIdType;

    @ApiModelProperty(value = "Contained if not yet contained in the first request, and mandated by the ASPSP in the related response. This field is relevant only in a corporate context", required = false, example = "6558e7e6-f72f-407a-9f22-763ad0921915")
    @JsonProperty(value = "psu-corporate-id")
    private String psuCorporateId;

    @ApiModelProperty(value = "Might be mandated by the ASPSP in addition if the PSU-Corporate-ID is contained", required = false, example = "type")
    @JsonProperty(value = "psu-corporate-id-type")
    private String psuCorporateIdType;

    @ApiModelProperty(value = "Is contained only, if an OAuth2 based authentication was performed in a pre-step or an OAuth2 based SCA was performed in the related consent authorisation", required = false, example = "b5eee7b9-9e05-4d72-a9f2-aec8b86256a1")
    @JsonProperty(value = "authorization bearer")
    private String authorizationBearer;

    @ApiModelProperty(value = "This data element may be contained, if the payment initiation transaction is part of a session, i.e. combined AIS/PIS service. This then contains the consentId of the related AIS consent, which was performed prior to this payment initiation.", required = false, example = "91306384-e37a-4536-a51e-1ced42e37a5c")
    @JsonProperty(value = "psu-consent-id")
    private String psuConsentId;

    @ApiModelProperty(value = "The forwarded Agent header field of the http request between PSU and TPP", required = false, example = "Mozilla/5.0 (Windows NT 10.0; WOW64; rv:54.0)")
    @JsonProperty(value = "psu-agent")
    private String psuAgent;

    @ApiModelProperty(value = "The forwarded IP Address header field consists of the corresponding http request IP Address field between PSU and TPP", required = true, example = "192.168.8.78") // //NOPMD.AvoidUsingHardCodedIP TODO review and check PMD assertion
    @JsonProperty(value = "psu-ip-address")
    @NotNull
    private String psuIpAddress;

    @ApiModelProperty(value = "The forwarded IP Address header field consists of the corresponding http request IP Address field between PSU and TPP", required = false, example = "GEO:52.506931,13.144558")
    @JsonProperty(value = "psu-geo-location")
    private String psuGeoLocation;

    @ApiModelProperty(value = "URI of the TPP, where the transaction flow shall be redirected to after a Redirect", required = false, example = "https://www.example.com/authentication/b51de9d9-2baa-4012-9447-e3f57f1f363c")
    @JsonProperty(value = "tpp-redirect-uri")
    private String tppRedirectUri;
}
