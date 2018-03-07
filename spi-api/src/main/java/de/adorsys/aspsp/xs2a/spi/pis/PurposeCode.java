package de.adorsys.aspsp.xs2a.spi.pis;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


@Data
@ApiModel(description = "PurposeCode", value = "Purpose code")
public class PurposeCode {
    
    // todo documentation doesn't have any definition. https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/40
    @ApiModelProperty(value = "Purpose code", example = "BCENECEQ")
    private String code;
}
