package de.adorsys.aspsp.xs2a.domain;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by aro on 30.11.17.
 */

@Data
@ApiModel(description = "Authentification", value = "Authentification")
public class Authentification {
	
	@ApiModelProperty(value = "Authentification Tpye", required = true)
	private AuthentificationType authentification_type;
	
	@ApiModelProperty(value = "Identification ID for the authentification, provided by the ASPSP", required = true)
    private String authentification__method_id;
	
	@ApiModelProperty(value = "Name of the authentification method", example= "redirect")
	private String name;
	
	@ApiModelProperty(value = "details information about the sca method", required = true)
	private String explanation;
	
	
}

