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

package de.adorsys.psd2.xs2a.web.validator.header;

import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.web.validator.ErrorBuildingService;
import de.adorsys.psd2.xs2a.web.validator.header.account.TransactionListDownloadHeaderValidator;
import de.adorsys.psd2.xs2a.web.validator.header.account.TransactionListHeaderValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.regex.Pattern;

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.FORMAT_ERROR_WRONG_HEADER;
import static de.adorsys.psd2.xs2a.web.validator.constants.Xs2aHeaderConstant.X_REQUEST_ID;

@Component
public class XRequestIdHeaderValidatorImpl extends AbstractHeaderValidatorImpl
    implements ConsentHeaderValidator, PaymentHeaderValidator, TransactionListHeaderValidator, FundsConfirmationHeaderValidator,
                   CancelPaymentHeaderValidator, TransactionListDownloadHeaderValidator, CreateConsentConfirmationOfFundsHeaderValidator {

    private static final String UUID_REGEX = "^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}\\z";
    private static final Pattern PATTERN = Pattern.compile(UUID_REGEX, Pattern.CASE_INSENSITIVE);

    @Autowired
    public XRequestIdHeaderValidatorImpl(ErrorBuildingService errorBuildingService) {
        super(errorBuildingService);
    }

    @Override
    protected String getHeaderName() {
        return X_REQUEST_ID;
    }

    @Override
    protected ValidationResult checkHeaderContent(Map<String, String> headers) {
        String header = headers.get(getHeaderName());
        if (isNonValid(header)) {
            return ValidationResult.invalid(
                errorBuildingService.buildErrorType(), TppMessageInformation.of(FORMAT_ERROR_WRONG_HEADER, getHeaderName()));
        }

        return super.checkHeaderContent(headers);
    }

    private boolean isNonValid(String xRequestId) {
        return !PATTERN.matcher(xRequestId).matches();
    }

}
