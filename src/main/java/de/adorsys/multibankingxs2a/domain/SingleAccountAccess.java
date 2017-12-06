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
@ApiModel(description = "Single account access", value = "SingleAccountAccess")
public class SingleAccountAccess {
	 @ApiModelProperty(value = "account", required=true)
	 private AccountReference account;
	 @ApiModelProperty(value = "access type: The“values balance and transactions are permitted. ", required=true, example = "balance, transactions")
	 private String[] access_type;
}
