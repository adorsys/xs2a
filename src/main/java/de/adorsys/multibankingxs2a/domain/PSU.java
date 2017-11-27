package de.adorsys.multibankingxs2a.domain;

import java.util.List;
import java.util.Map;


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
