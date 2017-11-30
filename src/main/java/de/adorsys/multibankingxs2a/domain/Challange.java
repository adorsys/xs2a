package de.adorsys.multibankingxs2a.domain;

import java.util.List;
import java.util.Map;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by aro on 30.11.17.
 */

@Data
@ApiModel(description = "Challange authentificatin", value = "Challange")
public class Challange {
	 @ApiModelProperty(value = "OTP maximal length", example = "me.png")
	 private String image;
	 @ApiModelProperty(value = "image", example = "1234")
	 private int OTP_max_length;
	 @ApiModelProperty(value = "OTP Format Type", example = "Push")
	 private String OPT_format;
	 @ApiModelProperty(value = "aditional information for the PSU", example = "SCA Redirect")
	 private String additional_information;
}
