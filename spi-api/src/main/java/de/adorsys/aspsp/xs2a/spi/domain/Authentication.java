package de.adorsys.aspsp.xs2a.spi.domain;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by aro on 30.11.17.
 */

@Data
@ApiModel(description = "Authentication", value = "Authentication")
public class Authentication {

    @ApiModelProperty(value = "Authentication Type", required = true)
    private AuthenticationType authentication_type;

    @ApiModelProperty(value = "Identification ID for the authentication, provided by the ASPSP", required = true)
    private String authentication__method_id;

    @ApiModelProperty(value = "Name of the authentication method", example = "redirect")
    private String name;

    @ApiModelProperty(value = "details information about the sca method", required = true)
    private String explanation;
}

