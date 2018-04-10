package de.adorsys.aspsp.xs2a.domain.address;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "Country code", value = "49")
public class CountryCode {

    @ApiModelProperty(value = "Country code", required = true, example = "49")
    private String code;
}
