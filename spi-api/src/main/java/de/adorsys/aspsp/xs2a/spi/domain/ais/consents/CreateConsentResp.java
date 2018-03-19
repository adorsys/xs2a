package de.adorsys.aspsp.xs2a.spi.domain.ais.consents;

import com.fasterxml.jackson.annotation.JsonInclude;
import de.adorsys.aspsp.xs2a.spi.domain.Links;
import de.adorsys.aspsp.xs2a.spi.domain.TransactionStatus;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "Response for the create account information consent request in the Account service")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreateConsentResp {
    
    @ApiModelProperty(value = "Authentication status of the consent", required = true)
    private final TransactionStatus transactionStatus;
    
    @ApiModelProperty(value = "Identification of the consent resource as it is used in the API structure")
    private final String consentId;
    
    @ApiModelProperty(value = "This data element might be contained, if SCA is required and if the PSU has a choice between different authentication methods. Depending on the risk management of the ASPSP this choice might be offered before or after the PSU has been identified with the first relevant factor, or if an access token is transported. If this data element is contained, then there is also an hyperlink of type 'selectAuthenticationMethods' contained in the response body.", required = false)
    private final AuthenticationObject[] scaMethods;
    
    @ApiModelProperty(value = "A list of hyperlinks to be recognized by Tpp", required = true)
    private final Links _links;
    
    @ApiModelProperty(value = "Text to be displayed to the PSU, e.g. in a Decoupled SCA Approach", required = false)
    private final String psuMessage;
}
