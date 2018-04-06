package de.adorsys.aspsp.xs2a.service.validator.header.impl;

import de.adorsys.aspsp.xs2a.service.validator.header.RequestHeader;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "Is used when headers are not correct", value = "NotMatchedHeaderImpl")
public class ErrorMessageHeaderImpl implements RequestHeader {
    @ApiModelProperty(value = "Error description", required = false, example = "Error: 'tpp-transaction-id' header has wrong format")
    private final String errorMessage;
}
