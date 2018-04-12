package de.adorsys.aspsp.xs2a.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.Currency;

@Data
@ApiModel(description = "Amount information", value = "Amount")
public class Amount {

	@ApiModelProperty(value = "ISO 4217 currency code", required = true, example = "EUR")
    @NotNull
    private Currency currency;

	@ApiModelProperty(value = "The amount given with fractional digits, where fractions must be compliant to the currency definition. The decimal separator is a dot", required = true, example = "1000.00")
    @NotNull
    private String content;
}
