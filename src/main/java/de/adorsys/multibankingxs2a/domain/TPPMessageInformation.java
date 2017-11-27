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
@ApiModel(description = "TPP Message Information", value = "TPPMessageInformation")
public class TPPMessageInformation {
	
	@ApiModelProperty(value = "Category", example = "Error")
	 private String category;

	//TO DO Should be later change to Message code	
	 @ApiModelProperty(value = "Code")
	 private String code;
	 
	 @ApiModelProperty(value = "Path", example = "")
	 private String path;
	 
	 @ApiModelProperty(value = "Text", example = "Additional Text")
	 private String text;
	 
}
