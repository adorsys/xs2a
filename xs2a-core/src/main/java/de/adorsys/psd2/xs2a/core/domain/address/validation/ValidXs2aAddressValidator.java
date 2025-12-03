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

package de.adorsys.psd2.xs2a.core.domain.address.validation;

import de.adorsys.psd2.xs2a.core.domain.address.Xs2aAddress;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.List;

public class ValidXs2aAddressValidator implements ConstraintValidator<ValidXs2aAddress, Xs2aAddress> {

    @Override
    public boolean isValid(Xs2aAddress address, ConstraintValidatorContext context) {
        if (address == null) {
            return true; // nothing to validate
        }

        List<String> addressLines = address.getAddressLines();
        String townName = address.getTownName();

        boolean hasAddressLines = addressLines != null && !addressLines.isEmpty();
        boolean hasTownName = townName != null && !townName.isBlank();

        // If addressLines are missing, townName must be present
        if (!hasAddressLines && !hasTownName) {
            addViolation(context, "townName is mandatory when address lines are not provided");
            return false;
        }

        // Optional: If addressLines exist, check if townName information is embedded in them
        if (hasAddressLines && !hasTownName) {
            boolean townInLines = addressLines.stream()
                .anyMatch(line -> line.toLowerCase().contains("stadt")   // example marker in German addresses
                    || line.toLowerCase().contains("city")
                    || line.toLowerCase().contains("town")
                    || line.toLowerCase().contains("nürnberg")      // could be matched dynamically
                );

            if (!townInLines) {
                addViolation(context, "townName missing: must be provided as field or appear in addressLines");
                return false;
            }
        }

        return true;
    }

    private void addViolation(ConstraintValidatorContext context, String message) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
    }
}

