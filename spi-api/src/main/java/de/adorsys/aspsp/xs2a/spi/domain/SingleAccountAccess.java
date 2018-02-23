package de.adorsys.aspsp.xs2a.spi.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by aro on 23.11.17.
 */

@Data
@ApiModel(description = "Single account access", value = "SingleAccountAccess")
@JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)

public class SingleAccountAccess {

    @ApiModelProperty(value = "account", required = true)
    private AccountReference account;
    @ApiModelProperty(value = "access type: The“values balance and transactions are permitted. ", required = true, example = "balance, transactions")
    private String[] access_type;
}
