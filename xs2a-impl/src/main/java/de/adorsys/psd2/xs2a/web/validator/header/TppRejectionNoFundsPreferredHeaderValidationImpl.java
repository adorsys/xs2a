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

import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.web.validator.ErrorBuildingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

import static de.adorsys.psd2.xs2a.web.validator.constants.Xs2aHeaderConstant.TPP_REJECTION_NO_FUNDS_PREFERRED;

@Component
public class TppRejectionNoFundsPreferredHeaderValidationImpl extends AbstractHeaderValidatorImpl
    implements PaymentHeaderValidator {

    @Autowired
    public TppRejectionNoFundsPreferredHeaderValidationImpl(ErrorBuildingService errorBuildingService) {
        super(errorBuildingService);
    }

    @Override
    protected String getHeaderName() {
        return TPP_REJECTION_NO_FUNDS_PREFERRED;
    }

    @Override
    public MessageError validate(Map<String, String> headers, MessageError messageError) {
        return checkBooleanFormat(headers, messageError);
    }
}
