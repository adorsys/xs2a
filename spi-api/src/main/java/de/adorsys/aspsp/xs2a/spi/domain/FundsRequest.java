package de.adorsys.aspsp.xs2a.spi.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "Request for the Confirmation Funds")
@JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)

public class FundsRequest {

	@ApiModelProperty(value = "card_number: ", example = "12345")
	private String card_number;

	@ApiModelProperty(value = "psu_account", required = true)
	private AccountReference psu_account;

	@ApiModelProperty(value = "payee", example = "Check24")
	private String payee;

	@ApiModelProperty(value = "instructed_amount", required = true)
	private Amount instructed_amount;
}
