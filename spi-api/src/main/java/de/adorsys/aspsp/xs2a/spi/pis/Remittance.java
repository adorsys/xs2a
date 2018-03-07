package de.adorsys.aspsp.xs2a.spi.pis;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Size;


@Data
@ApiModel(description = "Remittance", value = "Remittance")
public class Remittance {
    
    @ApiModelProperty(value = "the actual reference", required = true, example = "Ref Number Merchant")
    @Size(max = 35)
    private String reference;
    
    @ApiModelProperty(value = "reference type")
    @Size(max = 35)
    private String referenceType;
    
    @ApiModelProperty(value = "reference issuer")
    @Size(max = 35)
    private String referenceIssuer;
}
