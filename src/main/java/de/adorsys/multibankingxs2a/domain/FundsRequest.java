package de.adorsys.multibankingxs2a.domain;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


/**
 * Created by aro on 27.11.17.
 */

@Data
@ApiModel(description = "Request for the Confirmation Funds")
public class FundsRequest {
	@ApiModelProperty(value = "card_number: ", example = "12345")
	private String card_number;
	
	@ApiModelProperty(value = "psu_account", required=true)
	private AccountReference psu_account;
	
	@ApiModelProperty(value = "payee", example = "Check24")
	private String payee;
	
	@ApiModelProperty(value = "instructed_amount", required=true)
	private Amount instructed_amount;
	

}
