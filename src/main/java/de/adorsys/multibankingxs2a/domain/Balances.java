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
@ApiModel(description = "Balances", value = "Balances")
public class Balances {
	
	@ApiModelProperty(value = "booked")
	 private SingleBalance booked;
	
	@ApiModelProperty(value = "expected")
	 private SingleBalance expected;
	
	@ApiModelProperty(value = "authorised")
	 private SingleBalance authorised;
	
	@ApiModelProperty(value = "opening booked")
	 private SingleBalance opening_booked;
	
	@ApiModelProperty(value = "closing booked")
	 private SingleBalance closing_booked;
	
	@ApiModelProperty(value = "interim available")
	 private SingleBalance interim_available;
	

}
