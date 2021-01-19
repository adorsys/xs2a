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
