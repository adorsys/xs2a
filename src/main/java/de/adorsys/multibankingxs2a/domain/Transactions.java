package de.adorsys.multibankingxs2a.domain;

import java.util.Date;
import java.util.List;
import java.util.Map;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by aro on 23.11.17.
 */

@Data
@ApiModel(description = "Transactions information", value = "Transactions")
public class Transactions {
	 
	@ApiModelProperty(value = "Transaction ID", example = "12345")
	 private String transaction_id;
	
	@ApiModelProperty(value = "Entry Date", example = "2017-01-01")
	 private Date entry_date;
	
	@ApiModelProperty(value = "Amount")
	 private Amount amount;
	
	@ApiModelProperty(value = "Credited or Debited", example = "Credited or Debited")
	 private String credit_debit;
	
	@ApiModelProperty(value = "Name of the Creditor if a debited transaction", example = "Bauer")
	 private String creditor;
	
	@ApiModelProperty(value = "Creditor account", example = "56666")
	 private String creditor_account;
	
	@ApiModelProperty(value = "Name of the last creditor", example = "Max")
	 private String ultimate_creditor;
	
	@ApiModelProperty(value = "Name of the Debitor if a credited transaction", example = "Jan")
	 private String debitor;
	
	@ApiModelProperty(value = "Debitor account", example = "56666")
	 private String debitor_account;
	
	@ApiModelProperty(value = "Name of the last debitor", example = "Max")
	 private String ultimate_debitor;
	
	@ApiModelProperty(value = "Remittance information", example = "Otto")
	 private String remittance_information;
	 
	
	
}
