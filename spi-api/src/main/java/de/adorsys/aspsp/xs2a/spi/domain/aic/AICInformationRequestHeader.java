package de.adorsys.aspsp.xs2a.spi.domain.aic;

import lombok.Data;
import java.util.Date;

import io.swagger.annotations.ApiModelProperty;

@Data
public class AICInformationRequestHeader extends AICStatusRequestHeader {
	@ApiModelProperty(value = "Might be mandated in the ASPSP's documentation, if Oauth is not chosen as Pre- Step")
	private String psuId;
	@ApiModelProperty(value = "The PSU IP adress", required=true)
	private String psuIpAdress;
	@ApiModelProperty(value = "The PSU Agent")
	private String psuAgent;
	@ApiModelProperty(value = "The PSU geo location")
	private String psuGeoLocation;
	
}
