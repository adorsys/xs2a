package de.adorsys.aspsp.xs2a.spi.domain.aic;


import de.adorsys.aspsp.xs2a.spi.domain.Authentication;
import de.adorsys.aspsp.xs2a.spi.domain.Challenge;
import de.adorsys.aspsp.xs2a.spi.domain.Links;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by aro on 27.11.17.
 */
@Data
@ApiModel(description = "Response for the AIC information  request in the AICService")
public class AICInformationResponseBody extends AICStatusResponseBody{
	
	@ApiModelProperty(value = "This data element might be contained, if SCA is required and if the PSU has a choice between different authentication methods. Depending on the risk management of the ASPSP this choice might be offered before or after the PSU has been identified with the first relevant factor, or if an access token is transported. If this data element is contained, then there is also an hyperlink of type \"select_authentication_methods\" contained in the response body.")
	private Authentication[] sca_methods;
	@ApiModelProperty(value = "This data element is only contained in the response if the APSPS has chosen the Embedded SCA Approach, if the PSU is already identified with the first relevant factor or alternatively an access token, if SCA is required and if the authentication method is implicitly selected.")
	private Authentication chosen_sca_method;
	@ApiModelProperty(value = "It is containded in addition to the data element chosen_sca_method if challenge data is needed for SCA.")
	private Challenge sca_challenge_data;
    @ApiModelProperty(value = "A list of hyperlinks to be recognized by TPP", required= true)
    private Links _links;
    
    
    

}


