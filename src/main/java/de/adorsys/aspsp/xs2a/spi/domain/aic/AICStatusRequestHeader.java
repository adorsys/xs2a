package de.adorsys.aspsp.xs2a.spi.domain.aic;

import lombok.Data;
import java.util.Date;

import io.swagger.annotations.ApiModelProperty;

@Data
public class AICStatusRequestHeader extends AICGeneralRequestHeader{
	
	@ApiModelProperty(value = "Might be mandated in the ASPSP's documentation. Only used in a corporate ")
	private String psuCorporateId;
	
}
