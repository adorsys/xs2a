package de.adorsys.multibankingxs2a.domain;

import java.util.List;
import java.util.Map;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by aro on 23.11.17.
 */

@Data
@ApiModel(description = "Reference Party", value = "Reference")
public class Reference {

	
	@ApiModelProperty(value = "name", example = "TEST")
	 private String name;

	@ApiModelProperty(value = "ID", example = "99999")
	 private String id;
}
