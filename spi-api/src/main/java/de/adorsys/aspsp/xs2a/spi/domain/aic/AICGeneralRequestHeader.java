package de.adorsys.aspsp.xs2a.spi.domain.aic;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Date;

/**
 * Created by aro on 10.12.17.
 */

@ApiModel(description = "AI General RequestHeader", value = "AICGeneralRequestHeader")
public class AICGeneralRequestHeader {

    @ApiModelProperty(value = "ID of the transaction as determined by the initiating party.", required = true, example = "12345")
    private String processId;
    @ApiModelProperty(required = true, example = "1563542")
    private String requestId;
    @ApiModelProperty(value = "Standard https header element for Date and Time of the TPP Request.", required = true, example = "2017-10-10")
    private Date date;

    @ApiModelProperty(value = "Access Token (from optional OAuth2)")
    private String accessToken;
    @ApiModelProperty(value = "TPP Signing Certificate Data")
    private String certificate;
    private @ApiModelProperty(value = "TPP Signing Electronic Signature")
    String signature;
    @ApiModelProperty(value = "Further signature related data")
    private String signatureData;
}
