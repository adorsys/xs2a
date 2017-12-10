package de.adorsys.aspsp.xs2a.domain;


import io.swagger.annotations.ApiModel;
import lombok.Data;

/**
 * Created by aro on 27.11.17.
 */
@Data
@ApiModel(description = "Response for created by some methods inthe consent Service")
public class AICAccountsList  {

    private SingleAccountAccess[] accounts;
	private String valid_until;
	private Integer frequency_per_day;
	private boolean recurring_indicator;
	private TransactionStatus transaction_status;
	private String conset_status;
	

}


