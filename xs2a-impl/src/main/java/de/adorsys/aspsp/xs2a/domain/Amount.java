package de.adorsys.aspsp.xs2a.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Currency;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "Amount information", value = "Amount")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Amount {

	@ApiModelProperty(value = "currency", required = true, example = "EUR")
	private Currency currency;

	@ApiModelProperty(value = "content", required = true, example = "1000.00")
	private String content;
}
