package de.adorsys.aspsp.xs2a.domain.ais;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class AisInformationRequestHeader extends AisStatusRequestHeader {

    @ApiModelProperty(value = "Might be mandated in the ASPSP's documentation, if Oauth is not chosen as Pre- Step")
    private String psuId;
    @ApiModelProperty(value = "The Psu IP address", required = true)
    private String psuIpAddress;
    @ApiModelProperty(value = "The Psu Agent")
    private String psuAgent;
    @ApiModelProperty(value = "The Psu geo location")
    private String psuGeoLocation;
}
