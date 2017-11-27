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
@ApiModel(description = "Links ", value = "Links")
public class Links {

	
	@ApiModelProperty(value = "name", example = "www.testbank.de/login/")
	 private String redirect;
	
	@ApiModelProperty(value = "update psu identification", example = "api/v1/identification/")
	private String update_psu_identification;
	
	@ApiModelProperty(value = "update psu authentication", example = "api/v1/authentication/")
	private String update_psu_authentication;
	
	@ApiModelProperty(value = "select authentication method", example = "api/v1/oauth2/")
	private String select_authentication_method;
	
	@ApiModelProperty(value = "authorise transaction", example = "api/v1/consent/")
	private String authorise_transaction;
	
	@ApiModelProperty(value = "Self", example = "api/v1/payments/")
	private String self;
	
	@ApiModelProperty(value = "account link", example = "api/v1/payments/")
	private String account_link;
	
	@ApiModelProperty(value = "balances", example = "api/v1/balances/")
	private String balances;
	
	@ApiModelProperty(value = "Transactions", example = "api/v1/transactions/")
	private String transactions;
	
	@ApiModelProperty(value = "first page link", example = "api/v1/firstPage/")
	private String first_page_link;
	
	@ApiModelProperty(value = "second page link", example = "api/v1/secondPage/")
	private String second_page_link;
	
	@ApiModelProperty(value = "update psu identification", example = "api/v1/currentPage/")
	private String current_page_link;
	
	@ApiModelProperty(value = "last page link", example = "api/v1/lastPage/")
	private String last_page_link;
	
}
