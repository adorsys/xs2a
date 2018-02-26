package de.adorsys.aspsp.xs2a.spi.domain.aic;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
@ApiModel(description = "Array of account responses", value = "AccountResponseList")
public class AccountResponseList {

    @ApiModelProperty(value = "Array of account responses", required = true)
    private List<AccountResponse> account_list;
}
