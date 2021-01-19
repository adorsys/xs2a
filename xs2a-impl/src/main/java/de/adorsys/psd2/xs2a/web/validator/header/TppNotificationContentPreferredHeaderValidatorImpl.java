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

import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.profile.NotificationSupportedMode;
import de.adorsys.psd2.xs2a.core.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.web.validator.ErrorBuildingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.FORMAT_ERROR_INVALID_NOTIFICATION_MODE;
import static de.adorsys.psd2.xs2a.web.validator.constants.Xs2aHeaderConstant.TPP_NOTIFICATION_CONTENT_PREFERRED;

@Component
public class TppNotificationContentPreferredHeaderValidatorImpl extends AbstractHeaderValidatorImpl
    implements PaymentHeaderValidator, ConsentHeaderValidator {

    @Autowired
    public TppNotificationContentPreferredHeaderValidatorImpl(ErrorBuildingService errorBuildingService) {
        super(errorBuildingService);
    }

    @Override
    protected String getHeaderName() {
        return TPP_NOTIFICATION_CONTENT_PREFERRED;
    }

    @Override
    public ValidationResult validate(Map<String, String> headers) {

        String tppNotificationContentPreferredString = headers.get(getHeaderName());

        if (tppNotificationContentPreferredString == null) {
            return ValidationResult.valid();
        }

        String[] modes = tppNotificationContentPreferredString.replaceAll("status=", "").split(",");

        for (String mode : modes) {
            String trimmedMode = mode.trim();
            if (NotificationSupportedMode.getByValue(trimmedMode) == null) {
                return ValidationResult.invalid(
                    errorBuildingService.buildErrorType(), TppMessageInformation.of(FORMAT_ERROR_INVALID_NOTIFICATION_MODE, trimmedMode));
            }
        }

        return ValidationResult.valid();
    }
}
