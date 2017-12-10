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
@ApiModel(description = "Links ", value = "Links")
public class Links {

	
	@ApiModelProperty(value = "redirect: A link to an ASPSP site where SCA is performed within the Redirect SCA approach.", example = "www.testbank.de/login/")
	 private String redirect;
	
	@ApiModelProperty(value = "update psu identification: The link to the payment initiation or account information resource, which needs to be updated by the psu identification if not delivered yet.", example = "api/v1/identification/")
	private String update_psu_identification;
	
	@ApiModelProperty(value = "update psu authentication: The link to the payment initiation or account information resource, which needs to be updated by a psu password and eventually the psu identification if not delivered yet.", example = "api/v1/authentication/")
	private String update_psu_authentication;
	
	@ApiModelProperty(value = "select authentication method: This is a link to a resource, where the TPP can select the applicable second factor authentication methods for the PSU, if there were several available authentication methods.", example = "api/v1/oauth2/")
	private String select_authentication_method;
	
	@ApiModelProperty(value = "Self: The link to the payment initiation resource created by the request itself. This link can be used later to retrieve the transaction status of the payment initiation.", example = "api/v1/payments/")
	private String self;
	
	@ApiModelProperty(value = "Status", example = "api/v1/payments/")
	private String status;
	
	@ApiModelProperty(value = "account link", example = "api/v1/payments/")
	private String account_link;
	
	@ApiModelProperty(value = "balances: A link to the resource providing the balance of a dedicated account.", example = "api/v1/balances/")
	private String balances;
	
	@ApiModelProperty(value = "Transactions: A link to the resource providing the transaction history of a dediated amount.", example = "api/v1/transactions/")
	private String transactions;
	
	@ApiModelProperty(value = "first page link: Navigation link for account reports.", example = "api/v1/firstPage/")
	private String first_page_link;
	
	@ApiModelProperty(value = "second page link: Navigation link for account reports.", example = "api/v1/secondPage/")
	private String second_page_link;
	
	@ApiModelProperty(value = "current_page_link: Navigation link for account reports.", example = "api/v1/currentPage/")
	private String current_page_link;
	
	@ApiModelProperty(value = "last page link: Navigation link for account reports.", example = "api/v1/lastPage/")
	private String last_page_link;
	
}
