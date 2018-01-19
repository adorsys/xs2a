package de.adorsys.aspsp.xs2a.spi.domain;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by aro on 23.11.17.
 */

@Data
@ApiModel(description = "TPP Message Information", value = "TPPMessageInformation")
@JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)

public class TPPMessageInformation {
	
	@ApiModelProperty(value = "Category of the error permitted", required=true, example = "Error")
	 private String category;

	//TO DO Should be later change to Message code	
	 @ApiModelProperty(value = "Code", required=true)
	 private String code;
	 
	 @ApiModelProperty(value = "Path", example = "")
	 private String path;
	 
	 @ApiModelProperty(value = "Tadditional expalnation text", example = "Additional Text")
	 private String text;
	 
}
