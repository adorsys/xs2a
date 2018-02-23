package de.adorsys.aspsp.xs2a.spi.domain.aic;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by aro on 27.11.17.
 */
@Data
@ApiModel(description = "Response for created by some methods inthe consent Service")
public class AICResponseHeader {

    @ApiModelProperty(value = "Response Code", required = true)
    private String responseCode;
}


