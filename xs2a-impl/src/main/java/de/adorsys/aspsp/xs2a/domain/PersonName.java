package de.adorsys.aspsp.xs2a.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "Person name", value = "Michael, Schmidt")
public class PersonName {
    @ApiModelProperty(value = "First name", example = "Michael")
    private final String firstName;

    @ApiModelProperty(value = "Last name", example = "Schmidt")
    private final String lastName;
}
