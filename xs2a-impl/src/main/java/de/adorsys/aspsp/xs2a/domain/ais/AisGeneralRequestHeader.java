package de.adorsys.aspsp.xs2a.domain.ais;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Date;

@ApiModel(description = "AIS General RequestHeader", value = "AisGeneralRequestHeader")
public class AisGeneralRequestHeader {

    @ApiModelProperty(value = "ID of the transaction as determined by the initiating party.", required = true, example = "12345")
    private String processId;
    @ApiModelProperty(required = true, example = "1563542")
    private String requestId;
    @ApiModelProperty(value = "Standard https header element for Date and Time of the Tpp Request.", required = true, example = "2017-10-10")
    private Date date;
    @ApiModelProperty(value = "Access Token (from optional OAuth2)")
    private String accessToken;
    @ApiModelProperty(value = "Tpp Signing Certificate Data")
    private String certificate;
    @ApiModelProperty(value = "Tpp Signing Electronic Signature")
    private String signature;
    @ApiModelProperty(value = "Further signature related data")
    private String signatureData;
}
