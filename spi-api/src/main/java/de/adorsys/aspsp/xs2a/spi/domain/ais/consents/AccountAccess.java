package de.adorsys.aspsp.xs2a.spi.domain.ais.consents;

import com.fasterxml.jackson.annotation.JsonInclude;
import de.adorsys.aspsp.xs2a.spi.domain.AccountReference;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "Account access", value = "AccountAccess")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AccountAccess {

    @ApiModelProperty(value = "detailed account information", required = false)
    private AccountReference[] accounts;

    @ApiModelProperty(value = "balances of the addressed accounts", required = false)
    private AccountReference[] balances;

    @ApiModelProperty(value = "transactions of the addressed accounts", required = false)
    private AccountReference[] transactions;

    @ApiModelProperty(value = "only the value 'all-accounts' is admitted", example = "all-accounts", required = false)
    private String availableAccounts;

    @ApiModelProperty(value = "only the value 'all-accounts' is admitted", example = "all-accounts", required = false)
    private String allPsd2;
}
