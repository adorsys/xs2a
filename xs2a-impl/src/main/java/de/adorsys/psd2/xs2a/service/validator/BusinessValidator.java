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

package de.adorsys.psd2.xs2a.service.validator;

import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.service.validator.ValidationResult;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Set;

/**
 * Generic validator for validating certain request according to some business rules
 *
 * @param <T> type of object to be checked
 */
public interface BusinessValidator<T> {
    /**
     * Validates some object according to some business rules
     *
     * @param object business object to be validated
     * @return valid result if the object is valid, invalid result with appropriate error otherwise
     */
    @NotNull
    ValidationResult validate(@NotNull T object);

    /**
     * Checks some object according to some business rules and creates warning messages if there is inconsistency in the logic
     *
     * @param object business object to be validated
     * @return empty set if the object doesn't have inconsistencies, set of warning messages  otherwise
     */
    @NotNull
    default Set<TppMessageInformation> buildWarningMessages(@NotNull T object) {
        return Collections.emptySet();
    }
}
