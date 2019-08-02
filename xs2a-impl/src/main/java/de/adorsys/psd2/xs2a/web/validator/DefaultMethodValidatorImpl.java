/*
 * Copyright 2018-2019 adorsys GmbH & Co KG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.adorsys.psd2.xs2a.web.validator;

import de.adorsys.psd2.xs2a.exception.MessageError;
import de.adorsys.psd2.xs2a.web.validator.body.BodyValidator;
import de.adorsys.psd2.xs2a.web.validator.constants.Xs2aHeaderConstant;
import de.adorsys.psd2.xs2a.web.validator.header.HeaderValidator;
import de.adorsys.psd2.xs2a.web.validator.header.XRequestIdHeaderValidatorImpl;
import de.adorsys.psd2.xs2a.web.validator.path.PathParameterValidator;
import de.adorsys.psd2.xs2a.web.validator.query.QueryParameterValidator;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;

@Component
public class DefaultMethodValidatorImpl extends AbstractMethodValidator<HeaderValidator, BodyValidator, QueryParameterValidator, PathParameterValidator> {

    private XRequestIdHeaderValidatorImpl xRequestIdHeaderValidator;

    @Autowired
    public DefaultMethodValidatorImpl(XRequestIdHeaderValidatorImpl xRequestIdHeaderValidator) {
        super(Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        this.xRequestIdHeaderValidator = xRequestIdHeaderValidator;
    }

    @Override
    public String getMethodName() {
        return StringUtils.EMPTY;
    }

    @Override
    public void validate(HttpServletRequest request, MessageError messageError) {
        xRequestIdHeaderValidator.validate(Collections.singletonMap(Xs2aHeaderConstant.X_REQUEST_ID,
                                                                    request.getHeader(Xs2aHeaderConstant.X_REQUEST_ID)), messageError);
    }
}
