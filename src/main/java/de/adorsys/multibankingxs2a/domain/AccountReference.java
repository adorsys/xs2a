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
@ApiModel(description = "Account Reference", value = "AccountReference")
public class AccountReference {

	@ApiModelProperty(value = "IBAN", example = "1111111111")
	 private String iban;

	@ApiModelProperty(value = "BBAN", example = "1111111111")
	 private String bban;
	
	@ApiModelProperty(value = "PAN", example = "1111")
	 private String pan;

	@ApiModelProperty(value = "MSISDN", example = "0172/1111111")
	 private String msisdn;
	
}
