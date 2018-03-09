package de.adorsys.aspsp.xs2a.spi.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


@Data
@ApiModel(description = "BBAN", value = "The BBAN associated to the account.")
public class BBAN {
    
    // todo documentation doesn't have any definition. https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/42
    @ApiModelProperty(value = "BBAN code", example = "BBAN")
    private String code;
}
