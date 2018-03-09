package de.adorsys.aspsp.xs2a.spi.domain.codes;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;


@Data
@ApiModel(description = "BankTransactionCode", value = "The Bank transaction code")
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BankTransactionCode {

    // todo documentation doesn't have any definition. https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/44
    @ApiModelProperty(value = "BankTransactionCode code", example = "123344")
    private String code;
}
