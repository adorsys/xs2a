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
@ApiModel(description = "Response Body")
public class Response {
	 
	@ApiModelProperty(value = "Transactions status", example = "received")
	 private TransactionStatus transaction_status;
	
	@ApiModelProperty(value = "Links", example = "api/v1/payments/")
	 private Links _links;
}
