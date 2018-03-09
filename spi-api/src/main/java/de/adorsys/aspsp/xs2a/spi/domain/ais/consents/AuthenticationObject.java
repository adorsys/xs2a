package de.adorsys.aspsp.xs2a.spi.domain.ais.consents;


import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Size;

@Data
@ApiModel(description = "Authentication object", value = "AuthenticationObject")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthenticationObject {

	@ApiModelProperty(value = "Type of the authentication method", required = true)
	private AuthenticationType authenticationType;

	@ApiModelProperty(value = "Version can be used by differentiating authentication tools used within performing OTP generation in the same authentication type")
    private String authenticationVersion;


	@ApiModelProperty(value = "Provided by the ASPSP for the later identification of the authentication method selection.", required = true)
    @Size(max = 35)
	private String authenticationMethodId;

	@ApiModelProperty(value = "Name of the authentication method", example = "redirect")
	private String name;

	@ApiModelProperty(value = "Detailed information about the sca method for the PSU")
	private String explanation;

}

