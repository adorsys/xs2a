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

package de.adorsys.psd2.xs2a.web.validator.path;

import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.MessageError;
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
