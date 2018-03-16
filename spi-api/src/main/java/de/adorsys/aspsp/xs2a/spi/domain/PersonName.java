package de.adorsys.aspsp.xs2a.spi.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

@Data
@Setter(AccessLevel.NONE)
@ApiModel(description = "Person name", value = "Michael, Schmidt")
public class PersonName {
    @ApiModelProperty(value = "First name", example = "Michael")
    private String firstName;
    
    @ApiModelProperty(value = "Last name", example = "Schmidt")
    private String lastName;
}
