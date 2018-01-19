package de.adorsys.aspsp.xs2a.spi.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by aro on 23.11.17.
 */

@Data
@ApiModel(description = "PSU information", value = "PSU")
public class PSU {
	 @ApiModelProperty(value = "Password", example = "12345")
	 private String password;
}
