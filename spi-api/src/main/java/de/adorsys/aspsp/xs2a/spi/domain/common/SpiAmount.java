/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
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

package de.adorsys.aspsp.xs2a.spi.domain.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

import java.util.Currency;
import java.util.Optional;

@Slf4j
@Value
public class SpiAmount {
    private Currency currency;
    private String content;

    // todo make content with BigDecimal format: https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/90
    @JsonIgnore
    public double getDoubleContent() {
        try {
            return Optional.ofNullable(content)
                .map(Double::parseDouble)
                .orElse(0.0d);
        } catch (NumberFormatException ex) {
            log.warn("Problem with converting amount content '{}' to double format: {}", content, ex.getMessage());
            return 0.0d;
        }
    }
}
