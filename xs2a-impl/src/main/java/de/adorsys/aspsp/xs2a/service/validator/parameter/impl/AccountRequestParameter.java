package de.adorsys.aspsp.xs2a.service.validator.parameter.impl;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import de.adorsys.aspsp.xs2a.service.validator.parameter.RequestParameter;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
@ApiModel(description = "Account request parameter", value = "AccountRequestParameter")
@JsonIgnoreProperties(ignoreUnknown = true)
public class AccountRequestParameter implements RequestParameter {

    @ApiModelProperty(value = "Starting date of the account statement", example = "2017-10-30")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private Date dateFrom;

    @ApiModelProperty(value = "End date of the account statement", example = "2017-11-30")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private Date dateTo;
}
