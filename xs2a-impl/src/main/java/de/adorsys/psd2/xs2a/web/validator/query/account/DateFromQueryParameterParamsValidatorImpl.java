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

package de.adorsys.psd2.xs2a.web.validator.query.account;

import de.adorsys.psd2.xs2a.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.exception.MessageError;
import de.adorsys.psd2.xs2a.web.validator.ErrorBuildingService;
import de.adorsys.psd2.xs2a.web.validator.query.AbstractQueryParameterValidatorImpl;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.FORMAT_ERROR_ABSENT_PARAMETER;

@Component
public class DateFromQueryParameterParamsValidatorImpl extends AbstractQueryParameterValidatorImpl
    implements TransactionListQueryParamsValidator {
    private static final String DATE_FROM_PARAMETER_NAME = "dateFrom";
    private static final String ENTRY_REFERENCE_FROM_PARAMETER_NAME = "entryReferenceFrom";
    private static final String DELTA_LIST_PARAMETER_NAME = "deltaList";

    public DateFromQueryParameterParamsValidatorImpl(ErrorBuildingService errorBuildingService) {
        super(errorBuildingService);
    }

    @Override
    protected String getQueryParameterName() {
        return DATE_FROM_PARAMETER_NAME;
    }

    @Override
    public MessageError validate(Map<String, List<String>> queryParameterMap, MessageError messageError) {
        String dateFrom = getQueryParameterValue(queryParameterMap, getQueryParameterName());

        if (dateFrom == null && !isDeltaAccess(queryParameterMap)) {
            errorBuildingService.enrichMessageError(messageError, TppMessageInformation.of(FORMAT_ERROR_ABSENT_PARAMETER, getQueryParameterName()));
        }

        return messageError;
    }

    private boolean isDeltaAccess(Map<String, List<String>> queryParameterMap) {
        String entryReferenceFrom = getQueryParameterValue(queryParameterMap, ENTRY_REFERENCE_FROM_PARAMETER_NAME);
        String deltaList = getQueryParameterValue(queryParameterMap, DELTA_LIST_PARAMETER_NAME);
        return entryReferenceFrom != null || deltaList != null;
    }

    private String getQueryParameterValue(Map<String, List<String>> queryParameterMap, String parameter) {
        return Optional.of(parameter)
                   .map(queryParameterMap::get)
                   .orElseGet(ArrayList::new)
                   .stream()
                   .findFirst()
                   .orElse(null);

    }

}
