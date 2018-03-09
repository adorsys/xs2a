package de.adorsys.aspsp.xs2a.spi.domain.codes;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;


@Data
@ApiModel(description = "BBAN", value = "The BBAN associated to the account.")
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BBAN {

    // todo documentation doesn't have any definition. https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/42
    @ApiModelProperty(value = "BBAN code", example = "BBAN")
    private String code;
}
