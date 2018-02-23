package de.adorsys.aspsp.xs2a.spi.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by aro on 30.11.17.
 */

@Data
@ApiModel(description = "Challenge authentication", value = "Challenge")
public class Challenge {
    @ApiModelProperty(value = "OTP maximal length", example = "me.png")
    private String image;
    @ApiModelProperty(value = "image", example = "1234")
    private int OTP_max_length;
    @ApiModelProperty(value = "OTP Format Type", example = "Push")
    private String OPT_format;
    @ApiModelProperty(value = "additional information for the PSU", example = "SCA Redirect")
    private String additional_information;
}
