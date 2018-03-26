package de.adorsys.aspsp.xs2a.spi.domain.headers.impl;

import de.adorsys.aspsp.xs2a.spi.domain.headers.RequestHeaders;
import io.swagger.annotations.ApiModel;


@ApiModel(description = "Is used when handler is not matched", value = "NotMatchedHeaderImpl")
public class NotMatchedHeaderImpl implements RequestHeaders {

}
