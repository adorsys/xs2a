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

import de.adorsys.psd2.consent.api.piis.CmsPiisValidationInfo;
import de.adorsys.psd2.consent.api.service.PiisConsentService;
import de.adorsys.psd2.consent.domain.piis.PiisConsent;
import de.adorsys.psd2.consent.repository.PiisConsentRepository;
import de.adorsys.psd2.consent.service.mapper.PiisConsentMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Currency;
import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PiisConsentServiceInternal implements PiisConsentService {
    private final PiisConsentRepository piisConsentRepository;
    private final PiisConsentMapper piisConsentMapper;

    @Override
    public List<CmsPiisValidationInfo> getPiisConsentListByAccountIdentifier(Currency currency, String accountIdentifierName, String accountIdentifier) {
        List<PiisConsent> consents = extractPiisConsentList(currency, accountIdentifierName, accountIdentifier);
        return piisConsentMapper.mapToListCmsPiisValidationInfo(consents);
    }

    private List<PiisConsent> extractPiisConsentList(Currency currency, String accountIdentifierName, String accountIdentifier) {
        if (accountIdentifierName.equals("IBAN")) {
            return piisConsentRepository.findAllByAccountsIbanAndAccountsCurrency(accountIdentifier, currency);
        } else if (accountIdentifierName.equals("BBAN")) {
            return piisConsentRepository.findAllByAccountsBbanAndAccountsCurrency(accountIdentifier, currency);
        } else if (accountIdentifierName.equals("MSISDN")) {
            return piisConsentRepository.findAllByAccountsMsisdnAndAccountsCurrency(accountIdentifier, currency);
        } else if (accountIdentifierName.equals("MASKED_PAN")) {
            return piisConsentRepository.findAllByAccountsMaskedPanAndAccountsCurrency(accountIdentifier, currency);
        } else if (accountIdentifierName.equals("PAN")) {
            return piisConsentRepository.findAllByAccountsPanAndAccountsCurrency(accountIdentifier, currency);
        } else {
            return null;
        }
    }
}
