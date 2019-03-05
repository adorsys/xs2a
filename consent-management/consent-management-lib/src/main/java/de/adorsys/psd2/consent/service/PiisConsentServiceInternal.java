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

package de.adorsys.psd2.consent.service;

import de.adorsys.psd2.consent.api.service.PiisConsentService;
import de.adorsys.psd2.consent.domain.piis.PiisConsentEntity;
import de.adorsys.psd2.consent.repository.PiisConsentRepository;
import de.adorsys.psd2.consent.service.mapper.PiisConsentMapper;
import de.adorsys.psd2.xs2a.core.piis.PiisConsent;
import de.adorsys.psd2.xs2a.core.profile.AccountReferenceSelector;
import de.adorsys.psd2.xs2a.core.profile.AccountReferenceType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Currency;
import java.util.List;

import static de.adorsys.psd2.xs2a.core.profile.AccountReferenceType.*;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PiisConsentServiceInternal implements PiisConsentService {
    private final PiisConsentRepository piisConsentRepository;
    private final PiisConsentMapper piisConsentMapper;

    @Override
    public List<PiisConsent> getPiisConsentListByAccountIdentifier(Currency currency, AccountReferenceSelector accountReferenceSelector) {
        List<PiisConsentEntity> consents = extractPiisConsentList(currency, accountReferenceSelector);
        return piisConsentMapper.mapToPiisConsentList(consents);
    }

    // TODO refactor according to https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/580
    private List<PiisConsentEntity> extractPiisConsentList(Currency currency, AccountReferenceSelector accountReferenceSelector) {
        AccountReferenceType accountReferenceType = accountReferenceSelector.getAccountReferenceType();
        String accountReferenceValue = accountReferenceSelector.getAccountValue();
        if (accountReferenceType == IBAN) {
            return piisConsentRepository.findAllByAccountsIbanAndAccountsCurrency(accountReferenceValue, currency);
        } else if (accountReferenceType == BBAN) {
            return piisConsentRepository.findAllByAccountsBbanAndAccountsCurrency(accountReferenceValue, currency);
        } else if (accountReferenceType == MSISDN) {
            return piisConsentRepository.findAllByAccountsMsisdnAndAccountsCurrency(accountReferenceValue, currency);
        } else if (accountReferenceType == MASKED_PAN) {
            return piisConsentRepository.findAllByAccountsMaskedPanAndAccountsCurrency(accountReferenceValue, currency);
        } else if (accountReferenceType == PAN) {
            return piisConsentRepository.findAllByAccountsPanAndAccountsCurrency(accountReferenceValue, currency);
        } else {
            log.info("Account identifier is unknown!");
            return Collections.emptyList();
        }
    }
}
