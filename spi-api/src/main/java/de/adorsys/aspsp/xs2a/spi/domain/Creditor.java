package de.adorsys.aspsp.xs2a.spi.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by aro on 23.11.17.
 */

@Data
@ApiModel(description = "Creditor information", value = "Creditor")
@JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)

public class Creditor {

    //TODO type not defined in the documentation
    @ApiModelProperty(value = "name", example = "Schmidt, Michael")
    private String name;


    //TODO type not defined in the documentation
    @ApiModelProperty(value = "Address", example = "Wiesenweg, 1, 99999 Traumdorf")
    private String address;
}
