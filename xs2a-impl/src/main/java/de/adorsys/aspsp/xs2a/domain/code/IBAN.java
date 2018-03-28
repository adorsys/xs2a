package de.adorsys.aspsp.xs2a.domain.code;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;


@Data
@ApiModel(description = "IBAN", value = "The IBAN associated to the account.")
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class IBAN {
    //todo change property type to newly created Iban object and make validation accordingly  https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/42
    @ApiModelProperty(value = "IBAN code", example = "IBAN")
    private String code;
}
