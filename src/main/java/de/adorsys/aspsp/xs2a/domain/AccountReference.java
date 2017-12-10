package de.adorsys.aspsp.xs2a.domain;

import java.util.List;
import java.util.Map;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by aro on 23.11.17.
 */

@Data
@ApiModel(description = "Account Reference", value = "AccountReference")
public class AccountReference {

	@ApiModelProperty(value = "IBAN: This data element can be used in the body of the AICRequestBody Request Message for retrieving account access consent from this payment account", example = "1111111111")
	 private String iban;

	@ApiModelProperty(value = "BBAN: This data elements is used for payment accounts which have no IBAN", example = "1111111111")
	 private String bban;
	
	@ApiModelProperty(value = "PAN: Primary Account Number (PAN) of a card, can be tokenized by the ASPSP due to PCI DSS requirements.", example = "1111")
	 private String pan;

	@ApiModelProperty(value = "MSISDN: An alias to access a payment account via a registered mobile phone number. This alias might be needed e.g. in the payment initiation service, cp. Section 5.3.1. The support of this alias must be explicitly documented by the ASPSP for the corresponding API calls.", example = "0172/1111111")
	 private String msisdn;
	
}
