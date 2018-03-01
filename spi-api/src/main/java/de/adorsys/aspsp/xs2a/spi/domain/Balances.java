package de.adorsys.aspsp.xs2a.spi.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonRootName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "Balances", value = "Balances")
@JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
@JsonRootName(value = "balances")
public class Balances {

	@ApiModelProperty(value = "booked: Last known book balance of the account.")
	private SingleBalance booked;

	@ApiModelProperty(value = "expected: Balance composed of booked entries and pending items known at the time of calculation, which projects the end of day balance if everything is booked on the account and no other entry is posted.")
	private SingleBalance expected;

	@ApiModelProperty(value = "authorised: The expected balance together with the value of a pre-approved credit line the ASPSP makes permanently available to the user.")
	private SingleBalance authorised;

	@ApiModelProperty(value = "opening booked: Book balance of the account at the beginning of the account reporting period. It always equals the closing book balance from the previous report.")
	private SingleBalance opening_booked;

	@ApiModelProperty(value = "closing booked: Balance of the account at the end of the pre-agreed account reporting period. It is the sum of the opening booked balance at the beginning of the period and all entries booked to the account during the pre- agreed account reporting period.")
	private SingleBalance closing_booked;

	@ApiModelProperty(value = "interim available: Available balance calculated in the course of the account ’ervicer's business day, at the time specified, and subject to further changes during the business day. The interim balance is calculated on the basis of booked credit and debit items during the calculation time/period specified.")
	private SingleBalance interim_available;
}
