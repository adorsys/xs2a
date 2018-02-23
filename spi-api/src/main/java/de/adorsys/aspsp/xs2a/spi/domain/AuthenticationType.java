package de.adorsys.aspsp.xs2a.spi.domain;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * Created by aro on 30.11.17.
 */

@ApiModel(description = "Authentication type", value = "AuthenticationType")
public enum AuthenticationType {
	SMS_OTP(""),
	CHIP_OTP(""),
	PHOTO_OTP(""),
	PUSH_OTP("");
	
	@ApiModelProperty(value = "description", example = "Will be defined later")
	private String description;

	private AuthenticationType(String description) {
		this.description = description;
	}
	
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
