package de.adorsys.aspsp.xs2a.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import de.adorsys.aspsp.xs2a.domain.address.Address;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "Creditor information", value = "Creditor")
@JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
public class Creditor {

	@ApiModelProperty(value = "name", example = "Michael, Schmidt")
	private PersonName name;

	@ApiModelProperty(value = "Address", example = "Herrnstraße, 123-34, Nürnberg, 90431, 49")
	private Address address;
}
