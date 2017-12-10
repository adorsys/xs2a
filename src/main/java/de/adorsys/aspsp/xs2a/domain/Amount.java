package de.adorsys.aspsp.xs2a.domain;

import java.util.List;
import java.util.Map;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by aro on 23.11.17.
 */

@Data
@ApiModel(description = "Amount information", value = "Amount")
public class Amount {
	
	@ApiModelProperty(value = "currency", required=true, example = "€")
	 private String currency;
	
	
//TO DO type not defined in the documentation
	@ApiModelProperty(value = "content", required=true, example = "1000")
	 private String content;
}
