package de.adorsys.aspsp.xs2a.spi.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "Tpp Message Information", value = "TppMessageInformation")
@JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
public class TppMessageInformation {

	@ApiModelProperty(value = "Category of the error permitted", required = true, example = "Error")
	private String category;

	//TODO Should be later change to Message code
	@ApiModelProperty(value = "Code", required = true)
	private String code;

	@ApiModelProperty(value = "Path", example = "")
	private String path;

	@ApiModelProperty(value = "Additional explanation text", example = "Additional Text")
	private String text;

}
