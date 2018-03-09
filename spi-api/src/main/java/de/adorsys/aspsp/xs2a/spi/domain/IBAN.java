package de.adorsys.aspsp.xs2a.spi.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


@Data
@ApiModel(description = "IBAN", value = "The IBAN associated to the account.")
public class IBAN {
    
    // todo documentation doesn't have any definition. https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/42
    @ApiModelProperty(value = "IBAN code", example = "IBAN")
    private String code;
}
