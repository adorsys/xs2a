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

package de.adorsys.psd2.xs2a.web.validator.header;

import de.adorsys.psd2.xs2a.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.web.validator.ErrorBuildingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

import static de.adorsys.psd2.xs2a.web.validator.constants.Xs2aHeaderConstant.TPP_NOK_REDIRECT_URI;

@Component
public class TppNokRedirectUriHeaderValidatorImpl extends AbstractHeaderValidatorImpl
    implements PaymentHeaderValidator, ConsentHeaderValidator, CreateAuthorisationHeaderValidator {
    private final TppDomainValidator tppDomainValidator;

    @Autowired
    public TppNokRedirectUriHeaderValidatorImpl(ErrorBuildingService errorBuildingService, TppDomainValidator tppDomainValidator) {
        super(errorBuildingService);
        this.tppDomainValidator = tppDomainValidator;
    }

    @Override
    protected String getHeaderName() {
        return TPP_NOK_REDIRECT_URI;
    }

    @Override
    public ValidationResult validate(Map<String, String> headers) {
        return tppDomainValidator.validate(headers.get(getHeaderName()));
    }
}
