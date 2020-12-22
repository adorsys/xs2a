/*
 * Copyright 2018-2020 adorsys GmbH & Co KG
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
