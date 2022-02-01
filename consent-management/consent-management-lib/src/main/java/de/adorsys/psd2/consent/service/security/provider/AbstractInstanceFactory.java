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

package de.adorsys.psd2.consent.service.security.provider;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractInstanceFactory {
    protected String getStringValueByIndex(String[] paramsArr, int index, String defaultValue) {
        if (paramsArr == null) {
            return defaultValue;
        }

        if (paramsArr.length > index) {
            return paramsArr[index];
        }
        return defaultValue;
    }

    protected Integer getIntegerValueByIndex(String[] paramsArr, int index, Integer defaultValue) {
        if (paramsArr == null) {
            return defaultValue;
        }

        if (paramsArr.length > index) {
            try {
                return Integer.valueOf(paramsArr[index]);
            } catch (NumberFormatException e) {
                log.error(paramsArr[index] + " is not a number");
            }
        }
        return defaultValue;
    }
}
