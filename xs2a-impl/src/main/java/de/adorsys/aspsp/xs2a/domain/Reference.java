package de.adorsys.aspsp.xs2a.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "Reference Party", value = "Reference")
public class Reference {

	@ApiModelProperty(value = "name", example = "TEST")
	private String name;

	@ApiModelProperty(value = "ID", example = "99999")
	private String id;
}
