package de.adorsys.aspsp.xs2a.spi.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "Psu information", value = "Psu")
public class Psu {

	@ApiModelProperty(value = "Password", example = "12345")
	private String password;
}
