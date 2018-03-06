package de.adorsys.aspsp.xs2a.spi.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Currency;

@Data
@ApiModel(description = "Amount information", value = "Amount")
@JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
public class Amount {

	@ApiModelProperty(value = "currency", required = true, example = "EUR")
	private Currency currency;

	//TODO type not defined in the documentation
	@ApiModelProperty(value = "content", required = true, example = "1000.00")
	private String content;
}
