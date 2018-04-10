package de.adorsys.aspsp.xs2a.service.validator.parameter.impl;

import de.adorsys.aspsp.xs2a.service.validator.parameter.RequestParameter;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "Is used when parameter are not correct", value = "ErrorMessageParameterImpl")
public class ErrorMessageParameterImpl implements RequestParameter {
    @ApiModelProperty(value = "Error description", required = false, example = "Error: 'dataFrom' parameter has wrong format")
    private final String errorMessage;
}
