package de.adorsys.aspsp.xs2a.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@ApiModel(description = "CashAccountType", value = "Cash Account Type")
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public enum CashAccountType {
    // todo documentation doesn't have any definition. hhttps://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/45
    CURRENT_ACCOUNT
}
