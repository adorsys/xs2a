package de.adorsys.aspsp.xs2a.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "Exception")
@JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)

public class Exception {

	@ApiModelProperty(value = "transaction status", example = "Rejected")
	private TransactionStatus transaction_status;
	@ApiModelProperty(value = "Tpp message")
	private TppMessageInformation tpp_message;
}
