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
import de.adorsys.psd2.consent.api.service.ConsentService;
import de.adorsys.psd2.consent.api.service.PiisConsentService;
import de.adorsys.psd2.consent.domain.piis.PiisConsentEntity;
import de.adorsys.psd2.consent.repository.PiisConsentRepository;
import de.adorsys.psd2.consent.service.mapper.PiisConsentMapper;
import de.adorsys.psd2.consent.service.security.SecurityDataService;
import de.adorsys.psd2.xs2a.core.profile.AccountReferenceSelector;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static de.adorsys.psd2.xs2a.core.consent.ConsentStatus.RECEIVED;
import static de.adorsys.psd2.xs2a.core.consent.ConsentStatus.VALID;
import static de.adorsys.psd2.xs2a.core.profile.AccountReferenceSelector.*;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PiisConsentServiceInternal implements PiisConsentService, ConsentService {
    private final PiisConsentRepository piisConsentRepository;
    private final PiisConsentMapper piisConsentMapper;
    private final SecurityDataService securityDataService;

    @Override
    public List<CmsPiisValidationInfo> getPiisConsentListByAccountIdentifier(Currency currency, AccountReferenceSelector accountIdentifierName, String accountIdentifier) {
        List<PiisConsentEntity> consents = extractPiisConsentList(currency, accountIdentifierName, accountIdentifier);
        return piisConsentMapper.mapToListCmsPiisValidationInfo(consents);
    }

    private List<PiisConsentEntity> extractPiisConsentList(Currency currency, AccountReferenceSelector accountIdentifierName, String accountIdentifier) {
        if (accountIdentifierName == IBAN) {
            return piisConsentRepository.findAllByAccountsIbanAndAccountsCurrency(accountIdentifier, currency);
        } else if (accountIdentifierName == BBAN) {
            return piisConsentRepository.findAllByAccountsBbanAndAccountsCurrency(accountIdentifier, currency);
        } else if (accountIdentifierName == MSISDN) {
            return piisConsentRepository.findAllByAccountsMsisdnAndAccountsCurrency(accountIdentifier, currency);
        } else if (accountIdentifierName == MASKED_PAN) {
            return piisConsentRepository.findAllByAccountsMaskedPanAndAccountsCurrency(accountIdentifier, currency);
        } else if (accountIdentifierName == PAN) {
            return piisConsentRepository.findAllByAccountsPanAndAccountsCurrency(accountIdentifier, currency);
        } else {
            log.warn("Account identifier is unknown!");
            return Collections.emptyList();
        }
    }

    @Override
    public boolean isConsentExist(String consentId) {
        return getActualPiisConsent(consentId).isPresent();
    }

    private Optional<PiisConsentEntity> getActualPiisConsent(String encryptedConsentId) {
        Optional<String> consentIdDecrypted = securityDataService.decryptId(encryptedConsentId);
        if (!consentIdDecrypted.isPresent()) {
            log.warn("Consent Id has not encrypted: {}", encryptedConsentId);
        }

        return consentIdDecrypted
                   .flatMap(id -> piisConsentRepository.findByExternalIdAndConsentStatusIn(id, EnumSet.of(RECEIVED, VALID)));
    }
}
