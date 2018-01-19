package de.adorsys.aspsp.xs2a.spi.domain;

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
@ApiModel(description = "Payment Initialisation Request", value = "PaymentInitialisationRequest")
public class PaymentInitialisationRequest {
	 @ApiModelProperty(value = "end to end authentification", example = "payments")
	 private String end_to_end_identification;
	 @ApiModelProperty(value = "debtor account", required=true, example = "")
	 private AccountReference debtor_account;
	// @ApiModelProperty(value = "debtor_account_currency")
	// private Code debtor_account_currency;
	 @ApiModelProperty(value = "ultimate debtor", example = "Mueller")
	 private String ultimate_debtor;
	 @ApiModelProperty(value = "amount", required=true)
	 private Amount instructed_amount;
	 @ApiModelProperty(value = "creditor account", required=true, example = "")
	 private AccountReference creditor_account;
	 @ApiModelProperty(value = "Creditor Name", required= true, example = "Telekom")
	 private String creditor_name;
	// @ApiModelProperty(value = "Creditor Adress")
	// private Address creditor_adress;
	// @ApiModelProperty(value = "Creditor Agent")
    // private BICFI creditor_agent;
	 @ApiModelProperty(value = "ultimate creditor", example = "Telekom")
	 private String ultimate_creditor;
	// @ApiModelProperty(value = "purpose code")
      // private Code purpose_code;
	 @ApiModelProperty(value = "remitance information unstructured", example = "Ref. Number TELEKOM-1222")
	 private String remitance_information_unstructured;
	 
	// @ApiModelProperty(value = "remitance_information_structured", example = "Telekom")
	// private Remittance remitance_information_structured;
	 @ApiModelProperty(value = "requested execution time", example = "Telekom")
	 private Date requested_execution_time;
	 
	 
}
