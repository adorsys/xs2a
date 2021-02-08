/*
 * Copyright 2018-2021 adorsys GmbH & Co KG
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

package de.adorsys.psd2.consent.service.sha;

import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.Currency;
import java.util.Optional;

@Slf4j
@Service
public class AisChecksumCalculatingServiceV4 extends AisAbstractChecksumCalculatingService {

    @Override
    public String getVersion() {
        return "004";
    }

    @Override
    protected Comparator<AccountReference> getComparator() {
        return Comparator.comparing(AccountReference::getAspspAccountId)
                   .thenComparing(AccountReference::getResourceId)
                   .thenComparing(acc -> Optional.ofNullable(acc.getCurrency())
                                             .map(Currency::getCurrencyCode)
                                             .orElse(StringUtils.EMPTY));
    }
}
