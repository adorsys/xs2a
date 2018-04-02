package de.adorsys.aspsp.xs2a.domain.fund;

import com.fasterxml.jackson.annotation.JsonInclude;
import de.adorsys.aspsp.xs2a.domain.AccountReference;
import de.adorsys.aspsp.xs2a.domain.Amount;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@ApiModel(description = "Request for the Confirmation Funds")
public class FundsConfirmationRequest {

    @ApiModelProperty(value = "cardNumber: ", example = "12345")
    private  String cardNumber;

    @NotNull
    @ApiModelProperty(value = "psuAccount", required = true)
    private  AccountReference psuAccount;

    @ApiModelProperty(value = "payee", example = "Check24")
    private  String payee;

    @NotNull
    @ApiModelProperty(value = "instructedAmount", required = true)
    private  Amount instructedAmount;
}

