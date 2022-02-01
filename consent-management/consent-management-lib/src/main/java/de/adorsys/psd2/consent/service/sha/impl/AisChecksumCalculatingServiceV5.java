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

package de.adorsys.psd2.consent.service.sha.impl;

import de.adorsys.psd2.consent.service.sha.AisAbstractChecksumCalculatingService;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.Currency;
import java.util.Optional;

@Slf4j
@Service
public class AisChecksumCalculatingServiceV5 extends AisAbstractChecksumCalculatingService {

    @Override
    public String getVersion() {
        return "005";
    }

    @Override
    protected Comparator<AccountReference> getComparator() {
        return Comparator.comparing(AccountReference::getResourceId)
                   .thenComparing(acc -> Optional.ofNullable(acc.getAspspAccountId())
                                             .orElse(StringUtils.EMPTY))
                   .thenComparing(acc -> Optional.ofNullable(acc.getCurrency())
                                             .map(Currency::getCurrencyCode)
                                             .orElse(StringUtils.EMPTY));
    }
}
