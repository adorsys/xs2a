package de.adorsys.aspsp.xs2a.spi.domain.address;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Setter;

@Data
@Setter(AccessLevel.NONE)
@AllArgsConstructor
@ApiModel(description = "Country code", value = "49")
public class CountryCode {
    
    @ApiModelProperty(value = "Country code", required = true, example = "49")
    private String code;
}
