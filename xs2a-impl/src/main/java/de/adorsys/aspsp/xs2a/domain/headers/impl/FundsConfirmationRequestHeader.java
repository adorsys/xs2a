package de.adorsys.aspsp.xs2a.domain.headers.impl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;

@ApiModel(description = "Funds confirmation request header", value = "FundsConfirmationRequestHeader")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class FundsConfirmationRequestHeader extends CommonRequestHeader {

}
