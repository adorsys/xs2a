package de.adorsys.aspsp.xs2a.spi.domain.ais;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "Response for created by some methods inthe consent Service")
public class AisResponseHeader {

	@ApiModelProperty(value = "Response Code", required = true)
	private String responseCode;
}


