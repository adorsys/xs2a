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

package de.adorsys.psd2.consent.service.mapper;

import de.adorsys.psd2.consent.api.TypeAccess;
import de.adorsys.psd2.consent.api.ais.AisAccountAccess;
import de.adorsys.psd2.consent.api.ais.AisAccountConsent;
import de.adorsys.psd2.consent.api.ais.AisAccountConsentAuthorisation;
import de.adorsys.psd2.consent.domain.PsuData;
import de.adorsys.psd2.consent.domain.TppInfoEntity;
import de.adorsys.psd2.consent.domain.account.AisConsent;
import de.adorsys.psd2.consent.domain.account.AisConsentAuthorization;
import de.adorsys.psd2.consent.domain.account.AspspAccountAccess;
import de.adorsys.psd2.consent.domain.account.TppAccountAccess;
import de.adorsys.psd2.consent.service.AisConsentUsageService;
import de.adorsys.psd2.xs2a.core.consent.AisConsentRequestType;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.core.profile.AccountReferenceType;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.core.tpp.TppRedirectUri;
import de.adorsys.psd2.xs2a.core.tpp.TppRole;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Currency;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AisConsentMapperTest {
    private static final String EXTERNAL_ID = "ed1d8022-1c38-49ae-898e-78f29234557c";
    private static final String ACCOUNT_IBAN = "DE89876442804656108109";
    private static final String RESOURCE_ID = "resource id";
    private static final String ASPSP_ACCOUNT_ID = "aspsp account id";
    private static final Currency CURRENCY = Currency.getInstance("EUR");
    private static final String PSU_ID = "PSU_ID";
    private static final String PSU_ID_TYPE = "PSU_ID_TYPE";
    private static final String PSU_CORPORATE_ID = "PSU_CORPORATE_ID";
    private static final String PSU_CORPORATE_ID_TYPE = "PSU_CORPORATE_ID_TYPE";
    private static final String TPP_AUTHORISATION_NUMBER = "TPP_AUTHORISATION_NUMBER";
    private static final String TPP_AUTHORITY_ID = "TPP_AUTHORITY_ID";
    private static final String REDIRECT_URI = "REDIRECT_URI";
    private static final String NOK_REDIRECT_URI = "NOK_REDIRECT_URI";
    private static final List<TppRole> TPP_ROLES = buildTppRoles();
    private static final TppInfoEntity TPP_INFO_ENTITY = buildTppInfoEntity();
    private static final TppInfo TPP_INFO = buildTppInfo();
    private static final PsuData PSU_DATA = buildPsuData();
    private static final List<PsuData> PSU_DATA_LIST = Collections.singletonList(PSU_DATA);
    private static final PsuIdData PSU_ID_DATA = buildPsuIdData();
    private static final List<PsuIdData> PSU_ID_DATA_LIST = Collections.singletonList(PSU_ID_DATA);

    @Mock
    private PsuDataMapper psuDataMapper;
    @Mock
    private TppInfoMapper tppInfoMapper;
    @Mock
    private AisConsentUsageService aisConsentUsageService;

    @InjectMocks
    private AisConsentMapper aisConsentMapper;

    @Before
    public void setUp() {
        when(psuDataMapper.mapToPsuIdDataList(PSU_DATA_LIST)).thenReturn(PSU_ID_DATA_LIST);
        when(tppInfoMapper.mapToTppInfo(TPP_INFO_ENTITY)).thenReturn(TPP_INFO);
        when(psuDataMapper.mapToPsuIdData(PSU_DATA)).thenReturn(PSU_ID_DATA);
    }

    @Test
    public void mapToAisAccountConsent_accountAccess_emptyAspspAccountAccesses() {
        AisConsent aisConsent = buildAisConsentEmptyAspspAccesses();

        AisAccountAccess expectedAccess = buildAisAccountAccessAccounts();
        AisAccountConsent result = aisConsentMapper.mapToAisAccountConsent(aisConsent);

        assertEquals(expectedAccess, result.getAccess());
        assertEquals(aisConsent.getStatusChangeTimestamp(), result.getStatusChangeTimestamp());
    }

    @Test
    public void mapToAisAccountConsent() {
        // Given
        AisConsent aisConsent = buildAisConsent();
        AisAccountAccess expectedAccess = buildAisAccountAccessAccountsWithResourceId();
        when(aisConsentUsageService.getUsageCounter(aisConsent)).thenReturn(aisConsent.getUsageCounter());

        // When
        AisAccountConsent result = aisConsentMapper.mapToAisAccountConsent(aisConsent);

        // Then
        assertConsentsEquals(expectedAccess, aisConsent, result);
    }

    @Test
    public void mapToInitialAisAccountConsent() {
        // Given
        AisConsent aisConsent = buildAisConsent();
        AisAccountAccess expectedAccess = buildAisAccountAccessAccounts();
        when(aisConsentUsageService.getUsageCounter(aisConsent)).thenReturn(aisConsent.getUsageCounter());

        // When
        AisAccountConsent result = aisConsentMapper.mapToInitialAisAccountConsent(aisConsent);

        // Then
        assertConsentsEquals(expectedAccess, aisConsent, result);
    }

    private void assertConsentsEquals(AisAccountAccess expectedAccess, AisConsent aisConsent, AisAccountConsent aisAccountConsent) {
        AisConsentAuthorization aisConsentAuthorization = aisConsent.getAuthorizations().get(0);
        AisAccountConsentAuthorisation aisAccountConsentAuthorisation = aisAccountConsent.getAccountConsentAuthorizations().get(0);

        assertEquals(expectedAccess, aisAccountConsent.getAccess());
        assertEquals(aisConsent.getExternalId(), aisAccountConsent.getId());
        assertEquals(aisConsent.isRecurringIndicator(), aisAccountConsent.isRecurringIndicator());
        assertEquals(aisConsent.getExpireDate(), aisAccountConsent.getValidUntil());
        assertEquals(aisConsent.getAllowedFrequencyPerDay(), aisAccountConsent.getFrequencyPerDay());
        assertEquals(aisConsent.getLastActionDate(), aisAccountConsent.getLastActionDate());
        assertEquals(aisConsent.getConsentStatus(), aisAccountConsent.getConsentStatus());
        assertEquals(aisConsent.getAccesses().stream().anyMatch(a -> a.getTypeAccess() == TypeAccess.BALANCE), aisAccountConsent.isWithBalance());
        assertEquals(aisConsent.isTppRedirectPreferred(), aisAccountConsent.isTppRedirectPreferred());
        assertEquals(aisConsent.getAisConsentRequestType(), aisAccountConsent.getAisConsentRequestType());
        assertEquals(PSU_ID_DATA_LIST, aisAccountConsent.getPsuIdDataList());
        assertEquals(TPP_INFO, aisAccountConsent.getTppInfo());
        assertEquals(aisConsent.isMultilevelScaRequired(), aisAccountConsent.isMultilevelScaRequired());
        assertFalse(aisAccountConsent.getAccountConsentAuthorizations().isEmpty());
        assertEquals(aisConsent.getAuthorizations().size(), aisAccountConsent.getAccountConsentAuthorizations().size());
        assertEquals(PSU_ID_DATA, aisAccountConsentAuthorisation.getPsuIdData());
        assertEquals(aisConsentAuthorization.getScaStatus(), aisAccountConsentAuthorisation.getScaStatus());
        assertEquals(aisConsent.getUsageCounter(), aisAccountConsent.getUsageCounter());
        assertEquals(aisConsent.getCreationTimestamp(), aisAccountConsent.getCreationTimestamp());
        assertEquals(aisConsent.getStatusChangeTimestamp(), aisAccountConsent.getStatusChangeTimestamp());
    }

    private AisConsent buildAisConsentEmptyAspspAccesses() {
        return buildAisConsent(Collections.emptyList());
    }

    private AisConsent buildAisConsent() {
        return buildAisConsent(Collections.singletonList(buildAspspAccountAccessAccounts()));
    }

    private AisConsent buildAisConsent(List<AspspAccountAccess> aspspAccountAccesses) {
        AisConsent aisConsent = new AisConsent();
        aisConsent.setExternalId(EXTERNAL_ID);
        aisConsent.setAspspAccountAccesses(aspspAccountAccesses);
        aisConsent.setAccesses(Collections.singletonList(buildTppAccountAccessAccounts()));
        aisConsent.setCreationTimestamp(OffsetDateTime.now());
        aisConsent.setStatusChangeTimestamp(OffsetDateTime.now());
        aisConsent.setRecurringIndicator(true);
        aisConsent.setTppRedirectPreferred(true);
        aisConsent.setExpireDate(LocalDate.now().plusDays(3));
        aisConsent.setPsuDataList(Collections.singletonList(PSU_DATA));
        aisConsent.setTppInfo(TPP_INFO_ENTITY);
        aisConsent.setConsentStatus(ConsentStatus.VALID);
        aisConsent.setAllowedFrequencyPerDay(7);
        aisConsent.setUsageCounter(9);
        aisConsent.setAuthorizations(Collections.singletonList(buildAisConsentAuthorization()));
        aisConsent.setMultilevelScaRequired(true);
        aisConsent.setAisConsentRequestType(AisConsentRequestType.BANK_OFFERED);
        aisConsent.setLastActionDate(LocalDate.now());

        return aisConsent;
    }

    private AisConsentAuthorization buildAisConsentAuthorization() {
        AisConsentAuthorization authorization = new AisConsentAuthorization();
        authorization.setPsuData(PSU_DATA);
        authorization.setScaStatus(ScaStatus.RECEIVED);
        return authorization;
    }

    private static TppAccountAccess buildTppAccountAccessAccounts() {
        return new TppAccountAccess(ACCOUNT_IBAN, TypeAccess.ACCOUNT, AccountReferenceType.IBAN, CURRENCY);
    }

    private static AspspAccountAccess buildAspspAccountAccessAccounts() {
        return new AspspAccountAccess(ACCOUNT_IBAN, TypeAccess.ACCOUNT, AccountReferenceType.IBAN, CURRENCY, RESOURCE_ID, ASPSP_ACCOUNT_ID);
    }

    private AisAccountAccess buildAisAccountAccessAccounts() {
        AccountReference accountReference = new AccountReference(AccountReferenceType.IBAN, ACCOUNT_IBAN, CURRENCY);
        List<AccountReference> accountReferences = Collections.singletonList(accountReference);
        return new AisAccountAccess(accountReferences, Collections.emptyList(), Collections.emptyList(), null, null);
    }

    private AisAccountAccess buildAisAccountAccessAccountsWithResourceId() {
        AccountReference accountReference = new AccountReference(AccountReferenceType.IBAN, ACCOUNT_IBAN, CURRENCY,
                                                                 RESOURCE_ID, ASPSP_ACCOUNT_ID);
        List<AccountReference> accountReferences = Collections.singletonList(accountReference);
        return new AisAccountAccess(accountReferences, Collections.emptyList(), Collections.emptyList(), null, null);
    }

    private static TppInfoEntity buildTppInfoEntity() {
        TppInfoEntity tppInfoEntity = new TppInfoEntity();
        tppInfoEntity.setAuthorisationNumber(TPP_AUTHORISATION_NUMBER);
        tppInfoEntity.setAuthorityId(TPP_AUTHORITY_ID);
        tppInfoEntity.setTppRoles(TPP_ROLES);
        tppInfoEntity.setRedirectUri(REDIRECT_URI);
        tppInfoEntity.setNokRedirectUri(NOK_REDIRECT_URI);
        return tppInfoEntity;
    }

    private static TppInfo buildTppInfo() {
        TppInfo tppInfo = new TppInfo();
        tppInfo.setAuthorisationNumber(TPP_AUTHORISATION_NUMBER);
        tppInfo.setAuthorityId(TPP_AUTHORITY_ID);
        tppInfo.setTppRoles(TPP_ROLES);
        tppInfo.setTppRedirectUri(buildTppRedirectUri());
        return tppInfo;
    }

    private static TppRedirectUri buildTppRedirectUri() {
        return new TppRedirectUri(REDIRECT_URI, NOK_REDIRECT_URI);
    }

    private static List<TppRole> buildTppRoles() {
        return Arrays.asList(TppRole.AISP, TppRole.ASPSP, TppRole.PIISP, TppRole.PISP);
    }

    private static PsuData buildPsuData() {
        PsuData psuData = new PsuData();
        psuData.setPsuId(PSU_ID);
        psuData.setPsuIdType(PSU_ID_TYPE);
        psuData.setPsuCorporateId(PSU_CORPORATE_ID);
        psuData.setPsuCorporateIdType(PSU_CORPORATE_ID_TYPE);
        return psuData;
    }

    private static PsuIdData buildPsuIdData() {
        return new PsuIdData(PSU_ID, PSU_ID_TYPE, PSU_CORPORATE_ID, PSU_CORPORATE_ID_TYPE);
    }
}
