package de.adorsys.aspsp.xs2a.spi.domain;

import java.util.List;
import java.util.Map;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


/**
 * Created by aro on 23.11.17.
 */

@Data
@ApiModel(description = "Account information", value = "Account")
public class Account {
	 
	@ApiModelProperty(value = "ID: This is the data element to be used in the path when retrieving data from a dedicated account", required=true, example = "12345")
	 private String id;
	
	@ApiModelProperty(value = "IBAN: This data element can be used in the body of the AICRequestBody Request Message for retrieving account access consent from this payment accoun", example = "1111111111")
	 private String iban;

	@ApiModelProperty(value = "BBAN: This data element can be used in the body of the AICRequestBody Request Message for retrieving account access consent from this account, for payment accounts which have no IBAN. ", example = "1111111111")
	 private String bban;
	
	@ApiModelProperty(value = "PAN: Primary Account Number (PAN) of a card, can be tokenized by the ASPSP due to PCI DSS requirements", example = "1111")
	 private String pan;

	@ApiModelProperty(value = "MSISDN: An alias to access a payment account via a registered mobile phone number.", example = "0172/1111111")
	 private String msisdn;
	
	@ApiModelProperty(value = "Name: Name given by the bank or the PSU in Online- Banking", example = "lily")
	 private String name;
	
	@ApiModelProperty(value = "Acoount Type: Product Name of the Bank for this account", example = "SCT")
	 private String account_type;
	
	@ApiModelProperty(value = "BIC: The BIC associated to the account.", example = "1234567890")
	 private String bic;
	
	@ApiModelProperty(value = "Balances")
	 private Balances balances;
	
	@ApiModelProperty(value = "Currency Type", required= true, example = "€")
	 private String currency;
	
	@ApiModelProperty(value = "links: inks to the account, which can be directly used for retrieving account information from the dedicated account" )
	 private Links _links;

	
}
