package de.adorsys.aspsp.xs2a.domain;

import java.util.List;
import java.util.Map;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by aro on 30.11.17.
 */

@ApiModel(description = "Authentification tpye", value = "AuthentificationType")
public enum AuthentificationType {
	
	
	SMS_OTP(""),
	CHIP_OTP(""),
	PHOTO_OTP(""),
	PUSH_OTP("");
	
	
	@ApiModelProperty(value = "description", example = "Will be defined later")
	private String description;

	
	private AuthentificationType(String description) {
		this.description = description;
	}
	
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
