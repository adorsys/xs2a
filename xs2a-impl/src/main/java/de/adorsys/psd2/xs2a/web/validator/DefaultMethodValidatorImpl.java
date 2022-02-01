/*
 * Copyright 2018-2022 adorsys GmbH & Co KG
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version. This program is distributed in the hope that
 * it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 *
 * This project is also available under a separate commercial license. You can
 * contact us at psd2@adorsys.com.
 */

package de.adorsys.psd2.xs2a.web.validator;

import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.web.validator.constants.Xs2aHeaderConstant;
import de.adorsys.psd2.xs2a.web.validator.header.XRequestIdHeaderValidatorImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;

@Component
public class DefaultMethodValidatorImpl extends AbstractMethodValidator {

    private XRequestIdHeaderValidatorImpl xRequestIdHeaderValidator;

    @Autowired
    public DefaultMethodValidatorImpl(XRequestIdHeaderValidatorImpl xRequestIdHeaderValidator) {
        super(ValidatorWrapper.builder().build());
        this.xRequestIdHeaderValidator = xRequestIdHeaderValidator;
    }

    @Override
    public String getMethodName() {
        return StringUtils.EMPTY;
    }

    @Override
    public MessageError validate(HttpServletRequest request, MessageError messageError) {
        return xRequestIdHeaderValidator.validate(Collections.singletonMap(Xs2aHeaderConstant.X_REQUEST_ID,
                                                                           request.getHeader(Xs2aHeaderConstant.X_REQUEST_ID)), messageError);
    }
}
