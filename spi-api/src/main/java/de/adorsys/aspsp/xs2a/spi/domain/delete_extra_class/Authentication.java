package de.adorsys.aspsp.xs2a.spi.domain.delete_extra_class;


import de.adorsys.aspsp.xs2a.spi.domain.ais.consents.AuthenticationType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "Authentication", value = "Authentication")
public class Authentication {

	@ApiModelProperty(value = "Authentication Type", required = true)
	private AuthenticationType authentication_type;

	@ApiModelProperty(value = "Identification ID for the authentication, provided by the ASPSP", required = true)
	private String authentication__method_id;

	@ApiModelProperty(value = "Name of the authentication method", example = "redirect")
	private String name;

	@ApiModelProperty(value = "Details information about the sca method", required = true)
	private String explanation;
}

