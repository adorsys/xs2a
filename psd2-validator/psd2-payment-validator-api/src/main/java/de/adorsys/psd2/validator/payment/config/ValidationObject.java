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

package de.adorsys.psd2.validator.payment.config;

import lombok.Data;

@Data
public class ValidationObject {
    private Occurrence use;
    private int maxLength;

    public ValidationObject() {
        this(Occurrence.OPTIONAL, 0);
    }

    public ValidationObject(int maxLength) {
        this(Occurrence.OPTIONAL, maxLength);
    }

    public ValidationObject(Occurrence use, int maxLength) {
        this.use = use;
        this.maxLength = maxLength;
    }

    public boolean isRequired() {
        return Occurrence.REQUIRED == use;
    }

    public boolean isOptional() {
        return Occurrence.OPTIONAL == use;
    }

    public boolean isSkipped() {
        return Occurrence.SKIP == use;
    }

    public boolean isNone() {
        return Occurrence.NONE == use;
    }
}
