package de.adorsys.aspsp.xs2a.spi.pis;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


@Data
@ApiModel(description = "BICFI", value = "The BIC associated to the account.")
public class BICFI {

    // todo documentation doesn't have any definition. https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/39
    @ApiModelProperty(value = "BICFI code", example = "BCENECEQ")
    private String code;
}
