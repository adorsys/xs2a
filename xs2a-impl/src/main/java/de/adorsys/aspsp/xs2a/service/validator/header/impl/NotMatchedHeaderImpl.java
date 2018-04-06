package de.adorsys.aspsp.xs2a.service.validator.header.impl;

import de.adorsys.aspsp.xs2a.service.validator.header.RequestHeader;
import io.swagger.annotations.ApiModel;

@ApiModel(description = "Is used when handler is not matched", value = "NotMatchedHeaderImpl")
public class NotMatchedHeaderImpl implements RequestHeader {
}
