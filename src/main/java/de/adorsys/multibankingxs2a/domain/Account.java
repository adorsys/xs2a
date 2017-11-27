package de.adorsys.multibankingxs2a.domain;

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
	 
	@ApiModelProperty(value = "ID", example = "12345")
	 private String id;
	
	@ApiModelProperty(value = "IBAN", example = "1111111111")
	 private String iban;

	@ApiModelProperty(value = "BBAN", example = "1111111111")
	 private String bban;
	
	@ApiModelProperty(value = "PAN", example = "1111")
	 private String pan;

	@ApiModelProperty(value = "MSISDN", example = "0172/1111111")
	 private String msisdn;
	
	@ApiModelProperty(value = "Name", example = "lily")
	 private String name;
	
	@ApiModelProperty(value = "Acoount Type", example = "SCT")
	 private String account_type;
	
	@ApiModelProperty(value = "BIC", example = "1234567890")
	 private String bic;
	
	@ApiModelProperty(value = "Balances")
	 private String balances;
	
	@ApiModelProperty(value = "Currency Type", example = "€")
	 private String currency;
	
	@ApiModelProperty(value = "links" )
	 private String _links;

	
}
