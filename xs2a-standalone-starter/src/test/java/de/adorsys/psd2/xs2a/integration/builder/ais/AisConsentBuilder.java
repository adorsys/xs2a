/*
 * Copyright 2018-2019 adorsys GmbH & Co KG
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

package de.adorsys.psd2.xs2a.integration.builder.ais;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.adorsys.psd2.consent.api.ais.AisAccountAccess;
import de.adorsys.psd2.consent.api.ais.AisAccountConsent;
import de.adorsys.psd2.consent.api.ais.AisAccountConsentAuthorisation;
import de.adorsys.psd2.xs2a.core.ais.AccountAccessType;
import de.adorsys.psd2.xs2a.core.autorisation.AuthorisationTemplate;
import de.adorsys.psd2.xs2a.core.consent.AisConsentRequestType;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.domain.consent.CreateConsentReq;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aAccountAccess;
import de.adorsys.psd2.xs2a.integration.builder.AuthorisationTemplateBuilder;
import de.adorsys.psd2.xs2a.integration.builder.PsuIdDataBuilder;
import de.adorsys.psd2.xs2a.integration.builder.TppInfoBuilder;

import java.nio.charset.Charset;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static de.adorsys.psd2.xs2a.core.ais.AccountAccessType.ALL_ACCOUNTS;
import static org.apache.commons.io.IOUtils.resourceToString;

public class AisConsentBuilder {
    private final static TppInfo TPP_INFO = TppInfoBuilder.buildTppInfo();
    private final static AuthorisationTemplate AUTHORISATION_TEMPLATE = AuthorisationTemplateBuilder.buildAuthorisationTemplate();
    private final static PsuIdData PSU_DATA = PsuIdDataBuilder.buildPsuIdData();
    private final static String AUTHORISATION_ID = UUID.randomUUID().toString();
    private static final Charset UTF_8 = Charset.forName("utf-8");

    public static AisAccountConsent buildAisAccountConsent(String jsonPath, ScaApproach scaApproach, String encryptConsentId, ObjectMapper mapper, AisAccountConsentAuthorisation consentAuthorisation) throws Exception {
        CreateConsentReq consentReq = mapper.readValue(
            resourceToString(jsonPath, UTF_8),
            new TypeReference<CreateConsentReq>() {
            });

        return buildAisConsent(consentReq, encryptConsentId, scaApproach, consentAuthorisation);
    }

    public static AisAccountConsent buildAisAccountConsent(String jsonPath, ScaApproach scaApproach, String encryptConsentId, ObjectMapper mapper) throws Exception {
        return buildAisAccountConsent(jsonPath, scaApproach, encryptConsentId, mapper, null);
    }

    private static AisAccountConsent buildAisConsent(CreateConsentReq consentReq, String consentId, ScaApproach scaApproach, AisAccountConsentAuthorisation consentAuthorisation) {
        return Optional.ofNullable(consentReq)
                   .map(cr -> new AisAccountConsent(
                            consentId,
                            mapToAccountAccess(cr.getAccess()),
                            new AisAccountAccess(Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), null, null, null),
                            cr.isRecurringIndicator(),
                            cr.getValidUntil(),
                            cr.getFrequencyPerDay(),
                            LocalDate.now(),
                            ConsentStatus.RECEIVED,
                            !cr.getAccess().getBalances().isEmpty(),
                            ScaApproach.REDIRECT.equals(scaApproach),
                            getAisConsentRequestType(cr.getAccess()),
                            Collections.singletonList(PSU_DATA),
                            TPP_INFO,
                            AUTHORISATION_TEMPLATE,
                            false,
                            Collections.singletonList(consentAuthorisation != null ? consentAuthorisation : new AisAccountConsentAuthorisation(AUTHORISATION_ID, PSU_DATA, ScaStatus.RECEIVED)),
                            Collections.emptyMap(),
                            OffsetDateTime.now(),
                            OffsetDateTime.now()
                        )
                   )
                   .orElse(null);
    }

    private static AisAccountAccess mapToAccountAccess(Xs2aAccountAccess access) {
        AccountAccessType availableAccounts = access.getAvailableAccounts();
        return Optional.ofNullable(access)
                   .map(aa ->
                            new AisAccountAccess(
                                aa.getAccounts(),
                                aa.getBalances(),
                                aa.getTransactions(),
                                availableAccounts != null ? availableAccounts.name() : null,
                                access.getAllPsd2() != null ? access.getAllPsd2().name() : null,
                                access.getAvailableAccountsWithBalance() != null ? access.getAvailableAccountsWithBalance().name() : null
                            )
                   )
                   .orElse(null);
    }

    private static AisConsentRequestType getAisConsentRequestType(Xs2aAccountAccess access) {
        AisConsentRequestType aisConsentRequestType = null;
        if (!access.getAccounts().isEmpty()) {
            aisConsentRequestType = AisConsentRequestType.DEDICATED_ACCOUNTS;
        }

        if (access.getAllPsd2() == ALL_ACCOUNTS) {
            aisConsentRequestType = AisConsentRequestType.GLOBAL;
        }

        if (!access.isNotEmpty()) {
            aisConsentRequestType = AisConsentRequestType.BANK_OFFERED;
        }

        AccountAccessType availableAccounts = access.getAvailableAccounts();
        if (availableAccounts == ALL_ACCOUNTS) {
            aisConsentRequestType = AisConsentRequestType.ALL_AVAILABLE_ACCOUNTS;
        }
        return aisConsentRequestType;
    }

}
