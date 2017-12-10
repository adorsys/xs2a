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
@ApiModel(description = "Account Report", value = "AccountReport")
public class AccountReport {
	 
	@ApiModelProperty(value = "Booked Transactions", required=true)
	 private Transactions[] booked;
	
	@ApiModelProperty(value = "Pending Transactions")
	 private Transactions[] pending;
	
	@ApiModelProperty(value = "Liks: he following links might be used within this context:" + 
			"account link (mandatory)" + 
			"first_page_link (optional)" + 
			"second_page_link (optional)" + 
			"current_page_ link (optional)" + 
			"last_page_link (optional)", required=true)
	 private Links _links;
}
