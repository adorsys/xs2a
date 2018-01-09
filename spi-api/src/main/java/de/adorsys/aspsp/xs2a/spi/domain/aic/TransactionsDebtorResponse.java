package de.adorsys.aspsp.xs2a.spi.domain.aic;
import java.util.Date;
import java.util.List;
import java.util.Map;

import de.adorsys.aspsp.xs2a.spi.domain.Account;
import de.adorsys.aspsp.xs2a.spi.domain.Amount;
import de.adorsys.aspsp.xs2a.spi.domain.Balances;
import de.adorsys.aspsp.xs2a.spi.domain.Links;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


/**
 * Created by aro on 23.11.17.
 */

@Data
@ApiModel(description = "Transactions Response information", value = "TransactionsCreditorResponse")
public class TransactionsDebtorResponse {
	 
	@ApiModelProperty(value = "Transaction ID: Can be used as access-id in the API, where more details on an transaction is offered", example = "12345")
	// we get it in the Header in the Prozess-ID
	private String transaction_id;
	
	@ApiModelProperty(value = "Name of the Debtor if a credited transaction", example = "Jan")
	 private String debtor;
	
	@ApiModelProperty(value = "Debtor account", example = "56666")
	 private Account debtor_account;
	
	@ApiModelProperty(value = "Amount", required=true)
	private Amount amount;
	
	@ApiModelProperty(value = "Booking Date", example = "2017-01-01")
	 private Date booking_date;
	
	@ApiModelProperty(value = "Value Date", example = "2017-01-01")
	 private Date value_date;
	
	@ApiModelProperty(value = "Remittance information", example = "Otto")
	 private String remittance_information;
	 
	
	
}
