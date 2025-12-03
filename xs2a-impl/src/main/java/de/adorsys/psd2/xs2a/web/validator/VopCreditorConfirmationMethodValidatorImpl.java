/*
 * Copyright 2018-2025 adorsys GmbH & Co KG
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
 * contact us at sales@adorsys.com.
 */

package de.adorsys.psd2.xs2a.web.validator;

import de.adorsys.psd2.xs2a.web.validator.header.VopBodySigProfileHeaderValidatorImpl;
import de.adorsys.psd2.xs2a.web.validator.header.VopContentTypeHeaderValidatorImpl;
import de.adorsys.psd2.xs2a.web.validator.header.VopHeaderValidator;
import de.adorsys.psd2.xs2a.web.validator.path.PathParameterValidator;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class VopCreditorConfirmationMethodValidatorImpl extends AbstractMethodValidator {

    private static final String METHOD_NAME = "_creditorConfirmation";

    public VopCreditorConfirmationMethodValidatorImpl(List<VopHeaderValidator> headerValidators,
                                                      List<PathParameterValidator> pathParameterValidators) {
        // PUT endpoint with no request body - exclude body validators and body-related header validators
        // Body-Sig-Profile and Content-Type headers are not applicable for PUT requests without a body
        super(ValidatorWrapper.builder()
                  .headerValidators(filterHeaderValidators(headerValidators))
                  .bodyValidators(List.of())
                  .pathParameterValidators(pathParameterValidators)
                  .queryParameterValidators(List.of())
                  .build());
    }

    @Override
    public String getMethodName() {
        return METHOD_NAME;
    }

    /**
     * Filters out header validators that are not applicable for PUT requests without a body.
     * Excludes Body-Sig-Profile and Content-Type validators since this endpoint doesn't have a request body.
     */
    private static List<VopHeaderValidator> filterHeaderValidators(List<VopHeaderValidator> headerValidators) {
        return headerValidators.stream()
            .filter(validator -> !(validator instanceof VopBodySigProfileHeaderValidatorImpl))
            .filter(validator -> !(validator instanceof VopContentTypeHeaderValidatorImpl))
            .collect(Collectors.toList());
    }
}
