package de.adorsys.aspsp.xs2a.spi.domain.address;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Size;

@Data
@ApiModel(description = "Address", value = "Address")
public class Address {
    
    @ApiModelProperty(value = "Street", required = false, example = "Herrnstraße")
    @Size(max = 70)
    private String street;
    
    @ApiModelProperty(value = "Building number", required = false, example = "123-34")
    private String buildingNumber;
    
    @ApiModelProperty(value = "City", required = false, example = "Nürnberg")
    private String city;
    
    @ApiModelProperty(value = "Postal code", required = false, example = "90431")
    private String postalCode;
    
    @ApiModelProperty(value = "Country", required = true, example = "49")
    private CountryCode country;
}
