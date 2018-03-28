package de.adorsys.aspsp.xs2a.domain.ais;

import de.adorsys.aspsp.xs2a.domain.AccountDetails;
import de.adorsys.aspsp.xs2a.domain.Amount;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
@ApiModel(description = "Transaction Response information", value = "TransactionsCreditorResponse")
public class TransactionsCreditorResponse {

	@ApiModelProperty(value = "Transaction ID: Can be used as access-id in the API, where more details on an transaction is offered", example = "12345")
	// we get it in the Header in the Prozess-ID
	private String transaction_id;

	@ApiModelProperty(value = "Name of the Creditor if a debited transaction", example = "Bauer")
	private String creditor;

	@ApiModelProperty(value = "Creditor account", example = "56666")
	private AccountDetails creditor_accountDetails;

	@ApiModelProperty(value = "Amount", required = true)
	private Amount amount;

	@ApiModelProperty(value = "Booking Date", example = "2017-01-01")
	private Date booking_date;

	@ApiModelProperty(value = "Value Date", example = "2017-01-01")
	private Date value_date;

	@ApiModelProperty(value = "Remittance information", example = "Otto")
	private String remittance_information;
}
