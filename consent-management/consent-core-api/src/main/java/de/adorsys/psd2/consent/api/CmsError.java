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

package de.adorsys.psd2.consent.api;

import java.util.Optional;
import java.util.stream.Stream;

public enum CmsError {
    /**
     * Describes cases, when the error is caused by 3rd party libraries, network errors, etc.
     */
    TECHNICAL_ERROR,
    /**
     * Describes cases, when the error is caused by the mistakes in business logic (like providing wrong payment ID)
     */
    LOGICAL_ERROR,
    /**
     * Should be used in case of wrong AIS consent checksum after definite consent properties were set initially and then
     * are being changed.
     */
    CHECKSUM_ERROR;

    public static Optional<CmsError> getByName(String name) {
        return Stream.of(values())
                   .filter(v -> v.name().equalsIgnoreCase(name))
                   .findFirst();
    }

}
