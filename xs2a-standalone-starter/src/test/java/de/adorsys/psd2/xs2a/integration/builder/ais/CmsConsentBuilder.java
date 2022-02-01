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

package de.adorsys.psd2.xs2a.integration.builder.ais;

import com.fasterxml.jackson.core.type.TypeReference;
import de.adorsys.psd2.consent.api.ais.CmsConsent;
import de.adorsys.psd2.core.data.AccountAccess;
import de.adorsys.psd2.core.data.ais.AisConsentData;
import de.adorsys.psd2.core.mapper.ConsentDataMapper;
import de.adorsys.psd2.mapper.Xs2aObjectMapper;
import de.adorsys.psd2.model.Consents;
import de.adorsys.psd2.model.OtherType;
import de.adorsys.psd2.xs2a.core.ais.AccountAccessType;
import de.adorsys.psd2.xs2a.core.authorisation.Authorisation;
import de.adorsys.psd2.xs2a.core.authorisation.AuthorisationTemplate;
import de.adorsys.psd2.xs2a.core.authorisation.AuthorisationType;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.consent.ConsentTppInformation;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.core.profile.AdditionalInformationAccess;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.integration.builder.AuthorisationTemplateBuilder;
import de.adorsys.psd2.xs2a.integration.builder.PsuIdDataBuilder;
import de.adorsys.psd2.xs2a.integration.builder.TppInfoBuilder;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.apache.commons.io.IOUtils.resourceToString;

public class CmsConsentBuilder {
    private final static TppInfo TPP_INFO = TppInfoBuilder.buildTppInfo();
    private final static AuthorisationTemplate AUTHORISATION_TEMPLATE = AuthorisationTemplateBuilder.buildAuthorisationTemplate();
    private final static PsuIdData PSU_DATA = PsuIdDataBuilder.buildPsuIdData();
    private final static String AUTHORISATION_ID = UUID.randomUUID().toString();
    private static final Charset UTF_8 = StandardCharsets.UTF_8;
    private static final ConsentDataMapper consentDataMapper = new ConsentDataMapper();

    public static CmsConsent buildCmsConsent(String jsonPath, ScaApproach scaApproach, String encryptConsentId, Xs2aObjectMapper mapper, Authorisation authorisation) throws IOException {
        Consents createConsentRequest = mapper.readValue(resourceToString(jsonPath, UTF_8), new TypeReference<Consents>() {
        });
        return buildCmsConsent(createConsentRequest, encryptConsentId, scaApproach, authorisation);
    }

    public static CmsConsent buildCmsConsent(String jsonPath, ScaApproach scaApproach, String encryptConsentId, Xs2aObjectMapper mapper) throws IOException {
        return buildCmsConsent(jsonPath, scaApproach, encryptConsentId, mapper, null);
    }

    private static CmsConsent buildCmsConsent(Consents consentReq, String consentId, ScaApproach scaApproach, Authorisation authorisation) {
        return Optional.ofNullable(consentReq)
                   .map(cr -> {
                            AisConsentData aisConsentData = mapToAisConsentData(consentReq);

                            byte[] bytes = consentDataMapper.getBytesFromConsentData(aisConsentData);
                            OffsetDateTime now = OffsetDateTime.now();
                            ConsentTppInformation tppInformation = new ConsentTppInformation();
                            tppInformation.setTppRedirectPreferred(ScaApproach.REDIRECT.equals(scaApproach));
                            tppInformation.setTppInfo(TPP_INFO);

                            CmsConsent cmsConsent = new CmsConsent();
                            cmsConsent.setConsentData(bytes);
                            cmsConsent.setId(consentId);
                            cmsConsent.setRecurringIndicator(BooleanUtils.toBoolean(cr.getRecurringIndicator()));
                            cmsConsent.setValidUntil(cr.getValidUntil());
                            cmsConsent.setFrequencyPerDay(cr.getFrequencyPerDay());
                            cmsConsent.setLastActionDate(LocalDate.now());
                            cmsConsent.setConsentStatus(ConsentStatus.RECEIVED);
                            cmsConsent.setPsuIdDataList(Collections.singletonList(PSU_DATA));
                            cmsConsent.setAuthorisationTemplate(AUTHORISATION_TEMPLATE);
                            cmsConsent.setAuthorisations(Collections.singletonList(authorisation != null ? authorisation : new Authorisation(AUTHORISATION_ID, PSU_DATA, consentId, AuthorisationType.CONSENT, ScaStatus.RECEIVED)));
                            cmsConsent.setUsages(Collections.emptyMap());
                            cmsConsent.setCreationTimestamp(now);
                            cmsConsent.setStatusChangeTimestamp(now);
                            cmsConsent.setTppInformation(tppInformation);
                            cmsConsent.setTppAccountAccesses(mapToAccountAccess(cr.getAccess()));
                            cmsConsent.setAspspAccountAccesses(AccountAccess.EMPTY_ACCESS);

                            return cmsConsent;
                        }
                   )
                   .orElse(null);
    }

    private static AisConsentData mapToAisConsentData(Consents psd2CreateConsentRequest) {
        de.adorsys.psd2.model.AccountAccess psd2AccountAccess = psd2CreateConsentRequest.getAccess();

        AccountAccessType availableAccounts = Optional.ofNullable(psd2AccountAccess.getAvailableAccounts())
                                                  .map(de.adorsys.psd2.model.AccountAccess.AvailableAccountsEnum::toString)
                                                  .flatMap(AccountAccessType::getByDescription)
                                                  .orElse(null);

        AccountAccessType allPsd2 = Optional.ofNullable(psd2AccountAccess.getAllPsd2())
                                        .map(de.adorsys.psd2.model.AccountAccess.AllPsd2Enum::toString)
                                        .flatMap(AccountAccessType::getByDescription)
                                        .orElse(null);

        AccountAccessType availableAccountsWithBalance = Optional.ofNullable(psd2AccountAccess.getAvailableAccountsWithBalance())
                                                             .map(de.adorsys.psd2.model.AccountAccess.AvailableAccountsWithBalanceEnum::toString)
                                                             .flatMap(AccountAccessType::getByDescription)
                                                             .orElse(null);

        return new AisConsentData(availableAccounts,
                                  allPsd2,
                                  availableAccountsWithBalance,
                                  BooleanUtils.toBoolean(psd2CreateConsentRequest.isCombinedServiceIndicator()));
    }

    private static AccountAccess mapToAccountAccess(de.adorsys.psd2.model.AccountAccess psd2AccountAccess) {
        List<AccountReference> accounts = psd2AccountAccess.getAccounts().stream()
                                              .map(CmsConsentBuilder::mapToAccountReference)
                                              .collect(Collectors.toList());

        List<AccountReference> balances = psd2AccountAccess.getBalances().stream()
                                              .map(CmsConsentBuilder::mapToAccountReference)
                                              .collect(Collectors.toList());

        List<AccountReference> transactions = psd2AccountAccess.getTransactions().stream()
                                                  .map(CmsConsentBuilder::mapToAccountReference)
                                                  .collect(Collectors.toList());

        AdditionalInformationAccess additionalInformationAccess = mapToAdditionalInformationAccess(psd2AccountAccess.getAdditionalInformation());

        return new AccountAccess(accounts, balances, transactions, additionalInformationAccess);
    }

    private static AccountReference mapToAccountReference(de.adorsys.psd2.model.AccountReference psd2AccountReference) {
        return new AccountReference(null, null, psd2AccountReference.getIban(),
                                    psd2AccountReference.getBban(), psd2AccountReference.getPan(),
                                    psd2AccountReference.getMaskedPan(), psd2AccountReference.getMsisdn(),
                                    mapToCurrency(psd2AccountReference.getCurrency()),
                                    mapFromOtherType(psd2AccountReference.getOther()));
    }

    private static Currency mapToCurrency(String currencyCode) {
        if (StringUtils.isBlank(currencyCode)) {
            return null;
        }

        return Currency.getInstance(currencyCode);
    }

    private static AdditionalInformationAccess mapToAdditionalInformationAccess(de.adorsys.psd2.model.AdditionalInformationAccess psd2AdditionalInformation) {
        if (psd2AdditionalInformation == null) {
            return null;
        }

        List<AccountReference> ownerNames = psd2AdditionalInformation.getOwnerName().stream()
                                                .map(CmsConsentBuilder::mapToAccountReference)
                                                .collect(Collectors.toList());

        List<AccountReference> trustedBeneficiaries = psd2AdditionalInformation.getTrustedBeneficiaries().stream()
                                                .map(CmsConsentBuilder::mapToAccountReference)
                                                .collect(Collectors.toList());

        return new AdditionalInformationAccess(ownerNames, trustedBeneficiaries);
    }

    private static String mapFromOtherType(OtherType other) {
        return other == null
                   ? null
                   : other.getIdentification();
    }
}
