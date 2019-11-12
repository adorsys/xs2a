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

package de.adorsys.psd2.xs2a.web.validator.path;

import de.adorsys.psd2.xs2a.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.exception.MessageError;
import de.adorsys.psd2.xs2a.web.validator.ErrorBuildingService;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.regex.Pattern;

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.FORMAT_ERROR_PATH_PARAMETER_INVALID;

@Component
public class TransactionListDownloadPathParamsValidatorImpl implements TransactionListDownloadPathParamsValidator {

    private static final String BASE64_REGEX = "^(?:[A-Za-z0-9-_]{4})*(?:[A-Za-z0-9-_]{2}==|[A-Za-z0-9-_]{3}=)?$";
    private static final Pattern PATTERN = Pattern.compile(BASE64_REGEX);

    private final ErrorBuildingService errorBuildingService;

    public TransactionListDownloadPathParamsValidatorImpl(ErrorBuildingService errorBuildingService) {
        this.errorBuildingService = errorBuildingService;
    }

    @Override
    public MessageError validate(Map<String, String> queryParameterMap, MessageError messageError) {
        String downloadId = queryParameterMap.get("download-id");

        if (isNonValid(downloadId)) {
            errorBuildingService.enrichMessageError(messageError, TppMessageInformation.of(FORMAT_ERROR_PATH_PARAMETER_INVALID));
        }

        return messageError;
    }

    private boolean isNonValid(String base64) {
        return !PATTERN.matcher(base64).matches();
    }

}
