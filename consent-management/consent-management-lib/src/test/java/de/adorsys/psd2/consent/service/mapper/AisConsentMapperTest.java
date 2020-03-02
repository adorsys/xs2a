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

package de.adorsys.psd2.consent.service.mapper;

import de.adorsys.psd2.consent.api.ais.AdditionalAccountInformationType;
import de.adorsys.psd2.consent.api.ais.AisAccountAccess;
import de.adorsys.psd2.consent.api.ais.AisAccountConsentAuthorisation;
import de.adorsys.psd2.consent.api.ais.CmsAisAccountConsent;
import de.adorsys.psd2.consent.domain.AuthorisationEntity;
import de.adorsys.psd2.consent.domain.AuthorisationTemplateEntity;
import de.adorsys.psd2.consent.domain.PsuData;
import de.adorsys.psd2.consent.domain.TppInfoEntity;
import de.adorsys.psd2.consent.domain.consent.ConsentEntity;
import de.adorsys.psd2.consent.domain.consent.ConsentTppInformationEntity;
import de.adorsys.psd2.consent.service.AisConsentUsageService;
import de.adorsys.psd2.core.data.AccountAccess;
import de.adorsys.psd2.core.data.ais.AisConsent;
import de.adorsys.psd2.core.data.ais.AisConsentData;
import de.adorsys.psd2.core.mapper.ConsentDataMapper;
import de.adorsys.psd2.xs2a.core.ais.AccountAccessType;
import de.adorsys.psd2.xs2a.core.authorisation.AccountConsentAuthorization;
import de.adorsys.psd2.xs2a.core.authorisation.AuthorisationTemplate;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.consent.ConsentTppInformation;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.core.profile.AccountReferenceType;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.core.tpp.TppRedirectUri;
import de.adorsys.psd2.xs2a.core.tpp.TppRole;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AisConsentMapperTest {
    private static final String EXTERNAL_ID = "ed1d8022-1c38-49ae-898e-78f29234557c";
    private static final String INTERNAL_REQUEST_ID = UUID.randomUUID().toString();
    private static final String ACCOUNT_IBAN = "DE89876442804656108109";
    private static final String RESOURCE_ID = "resource id";
    private static final String ASPSP_ACCOUNT_ID = "aspsp account id";
    private static final Currency CURRENCY = Currency.getInstance("EUR");
    private static final String PSU_ID = "PSU_ID";
    private static final String PSU_ID_TYPE = "PSU_ID_TYPE";
    private static final String PSU_CORPORATE_ID = "PSU_CORPORATE_ID";
    private static final String PSU_CORPORATE_ID_TYPE = "PSU_CORPORATE_ID_TYPE";
    private static final String PSU_IP_ADDRESS = "PSU_IP_ADDRESS";
    private static final String TPP_AUTHORISATION_NUMBER = "TPP_AUTHORISATION_NUMBER";
    private static final String TPP_AUTHORITY_ID = "TPP_AUTHORITY_ID";
    private static final List<TppRole> TPP_ROLES = buildTppRoles();
    private static final TppInfoEntity TPP_INFO_ENTITY = buildTppInfoEntity();
    private static final TppInfo TPP_INFO = buildTppInfo();
    private static final PsuData PSU_DATA = buildPsuData();
    private static final List<PsuData> PSU_DATA_LIST = Collections.singletonList(PSU_DATA);
    private static final PsuIdData PSU_ID_DATA = buildPsuIdData();
    private static final List<PsuIdData> PSU_ID_DATA_LIST = Collections.singletonList(PSU_ID_DATA);
    private static final Map<String, Integer> USAGE_COUNTER = Collections.singletonMap("/accounts", 9);
    private static final AisConsentData AIS_CONSENT_DATA = buildAisConsentData(null, null, null, false);
    private static final OffsetDateTime CREATION_TIMESTAMP = OffsetDateTime.now();
    private static final OffsetDateTime STATUS_CHANGE_TIMESTAMP = OffsetDateTime.now();
    private static final LocalDate LAST_ACTION_DATE = LocalDate.now();
    private static final String REDIRECT_URI = "redirect uri";
    private static final String NOK_REDIRECT_URI = "non redirect uri";
    private static final String CANCEL_REDIRECT_URI = "cancel redirect uri";
    private static final String CANCEL_NOK_REDIRECT_URI = "cancel nok redirect uri";
    private static final AisConsent AIS_CONSENT = buildAisConsent(AIS_CONSENT_DATA, Collections.emptyList(), buildTppAccountAccessAccounts(), buildAspspAccountAccessAccounts());
    private static final String TPP_BRAND_LOGGING_INFORMATION = "tppBrandLoggingInformation";

    private JsonReader jsonReader = new JsonReader();

    @Mock
    private AuthorisationTemplateMapper authorisationTemplateMapper;
    @Mock
    private PsuDataMapper psuDataMapper;
    @Mock
    private TppInfoMapper tppInfoMapper;
    @Mock
    private AisConsentUsageService aisConsentUsageService;
    @Mock
    private ConsentDataMapper consentDataMapper;
    @Mock
    private ConsentTppInformationMapper consentTppInformationMapper;
    @Mock
    private AccessMapper accessMapper;

    @InjectMocks
    private AisConsentMapper aisConsentMapper;

    @Test
    void mapToCmsAisAccountConsent_emptyAuthorisations() {
        when(psuDataMapper.mapToPsuIdDataList(PSU_DATA_LIST)).thenReturn(PSU_ID_DATA_LIST);
        when(tppInfoMapper.mapToTppInfo(TPP_INFO_ENTITY)).thenReturn(TPP_INFO);
        when(consentDataMapper.mapToAisConsentData(any())).thenReturn(AIS_CONSENT_DATA);
        when(consentTppInformationMapper.mapToConsentTppInformation(any())).thenReturn(buildConsentTppInformation());
        when(authorisationTemplateMapper.mapToAuthorisationTemplate(any())).thenReturn(buildAuthorisationTemplate());

        // Given
        ConsentEntity consent = buildConsent();
        AisAccountAccess expectedAccess = buildAisAccountAccessAccountsWithResourceId();
        when(aisConsentUsageService.getUsageCounterMap(consent)).thenReturn(USAGE_COUNTER);
        when(accessMapper.mapTppAccessesToAccountAccess(consent.getTppAccountAccesses(), AdditionalAccountInformationType.NONE)).thenReturn(AIS_CONSENT.getTppAccountAccesses());
        when(accessMapper.mapAspspAccessesToAccountAccess(consent.getAspspAccountAccesses(), AdditionalAccountInformationType.NONE)).thenReturn(AIS_CONSENT.getAspspAccountAccesses());

        List<AuthorisationEntity> authorisations = Collections.emptyList();

        // When
        CmsAisAccountConsent result = aisConsentMapper.mapToCmsAisAccountConsent(consent, authorisations);

        // Then
        assertConsentsEquals(expectedAccess, authorisations, consent, result, AIS_CONSENT);
    }

    @Test
    void mapToCmsAisAccountConsent_globalAccountAccessAndEmptyAspspAccountAccesses() {
        when(psuDataMapper.mapToPsuIdDataList(PSU_DATA_LIST)).thenReturn(PSU_ID_DATA_LIST);
        when(psuDataMapper.mapToPsuIdData(PSU_DATA)).thenReturn(PSU_ID_DATA);
        when(tppInfoMapper.mapToTppInfo(TPP_INFO_ENTITY)).thenReturn(TPP_INFO);
        when(consentTppInformationMapper.mapToConsentTppInformation(any())).thenReturn(buildConsentTppInformation());
        when(authorisationTemplateMapper.mapToAuthorisationTemplate(any())).thenReturn(buildAuthorisationTemplate());
        AisConsentData aisConsentData = buildAisConsentData(null, AccountAccessType.ALL_ACCOUNTS, null);
        AisConsent aisConsent = buildAisConsent(aisConsentData,
                                                Collections.emptyList(),
                                                buildAccountAccessAccounts(Collections.singletonList(new AccountReference(AccountReferenceType.IBAN, ACCOUNT_IBAN, CURRENCY)), Collections.emptyList(), Collections.emptyList()),
                                                buildAccountAccessAccounts(Collections.emptyList(), Collections.emptyList(), Collections.emptyList()));
        when(consentDataMapper.mapToAisConsentData(any())).thenReturn(aisConsentData);

        // Given
        ConsentEntity consent = buildConsent();
        AisAccountAccess expectedAccess = buildAisAccountAccessAccountWithoutResourceId(true);
        when(aisConsentUsageService.getUsageCounterMap(consent)).thenReturn(USAGE_COUNTER);
        when(accessMapper.mapTppAccessesToAccountAccess(consent.getTppAccountAccesses(), AdditionalAccountInformationType.NONE)).thenReturn(aisConsent.getTppAccountAccesses());
        when(accessMapper.mapAspspAccessesToAccountAccess(consent.getAspspAccountAccesses(), AdditionalAccountInformationType.NONE)).thenReturn(aisConsent.getAspspAccountAccesses());

        List<AuthorisationEntity> authorisations = Collections.singletonList(buildAisConsentAuthorisation());

        // When
        CmsAisAccountConsent result = aisConsentMapper.mapToCmsAisAccountConsent(consent, authorisations);

        // Then
        assertConsentsEquals(expectedAccess, authorisations, consent, result, aisConsent);
    }

    @Test
    void mapToCmsAisAccountConsent() {
        when(psuDataMapper.mapToPsuIdDataList(PSU_DATA_LIST)).thenReturn(PSU_ID_DATA_LIST);
        when(psuDataMapper.mapToPsuIdData(PSU_DATA)).thenReturn(PSU_ID_DATA);
        when(tppInfoMapper.mapToTppInfo(TPP_INFO_ENTITY)).thenReturn(TPP_INFO);
        when(consentDataMapper.mapToAisConsentData(any())).thenReturn(AIS_CONSENT_DATA);
        when(consentTppInformationMapper.mapToConsentTppInformation(any())).thenReturn(buildConsentTppInformation());
        when(authorisationTemplateMapper.mapToAuthorisationTemplate(any())).thenReturn(buildAuthorisationTemplate());

        // Given
        ConsentEntity consent = buildConsent();
        AisAccountAccess expectedAccess = buildAisAccountAccessAccountsWithResourceId();
        when(aisConsentUsageService.getUsageCounterMap(consent)).thenReturn(USAGE_COUNTER);
        when(accessMapper.mapTppAccessesToAccountAccess(consent.getTppAccountAccesses(), AdditionalAccountInformationType.NONE)).thenReturn(AIS_CONSENT.getTppAccountAccesses());
        when(accessMapper.mapAspspAccessesToAccountAccess(consent.getAspspAccountAccesses(), AdditionalAccountInformationType.NONE)).thenReturn(AIS_CONSENT.getAspspAccountAccesses());

        List<AuthorisationEntity> authorisations = Collections.singletonList(buildAisConsentAuthorisation());

        // When
        CmsAisAccountConsent result = aisConsentMapper.mapToCmsAisAccountConsent(consent, authorisations);

        // Then
        assertConsentsEquals(expectedAccess, authorisations, consent, result, AIS_CONSENT);
    }

    @Test
    void mapToAccountAccess() {
        // Given
        AccountAccess expected = buildAspspAccountAccessAccounts();

        // When
        AccountAccess accountAccess = aisConsentMapper.mapToAccountAccess(buildAisAccountAccessAccountsWithResourceId());

        // Then
        assertEquals(accountAccess.getAccounts(), expected.getAccounts());
        assertEquals(accountAccess.getBalances(), expected.getBalances());
        assertEquals(accountAccess.getTransactions(), expected.getTransactions());
    }

    @Test
    void mapToAisConsent_emptyAuthorisations() {
        when(psuDataMapper.mapToPsuIdDataList(PSU_DATA_LIST)).thenReturn(PSU_ID_DATA_LIST);
        when(consentDataMapper.mapToAisConsentData(any())).thenReturn(AIS_CONSENT_DATA);
        when(authorisationTemplateMapper.mapToAuthorisationTemplate(any())).thenReturn(buildAuthorisationTemplate());
        when(consentTppInformationMapper.mapToConsentTppInformation(any())).thenReturn(buildConsentTppInformation());

        // Given
        ConsentEntity consent = buildConsent();
        List<AuthorisationEntity> authorisationEntities = Collections.emptyList();
        when(aisConsentUsageService.getUsageCounterMap(consent)).thenReturn(USAGE_COUNTER);
        when(accessMapper.mapTppAccessesToAccountAccess(consent.getTppAccountAccesses(), AdditionalAccountInformationType.NONE)).thenReturn(AIS_CONSENT.getTppAccountAccesses());
        when(accessMapper.mapAspspAccessesToAccountAccess(consent.getAspspAccountAccesses(), AdditionalAccountInformationType.NONE)).thenReturn(AIS_CONSENT.getAspspAccountAccesses());

        //When
        AisConsent result = aisConsentMapper.mapToAisConsent(consent, authorisationEntities);
        assertConsentsEqual(result, AIS_CONSENT);
    }

    @Test
    void mapToAisConsent() {
        when(psuDataMapper.mapToPsuIdDataList(PSU_DATA_LIST)).thenReturn(PSU_ID_DATA_LIST);
        when(psuDataMapper.mapToPsuIdData(PSU_DATA)).thenReturn(PSU_ID_DATA);
        when(consentDataMapper.mapToAisConsentData(any())).thenReturn(AIS_CONSENT_DATA);
        when(authorisationTemplateMapper.mapToAuthorisationTemplate(any())).thenReturn(buildAuthorisationTemplate());
        when(consentTppInformationMapper.mapToConsentTppInformation(any())).thenReturn(buildConsentTppInformation());

        // Given
        ConsentEntity consent = buildConsent();
        AisConsent expected = buildAisConsent(AIS_CONSENT_DATA, Collections.singletonList(buildAccountConsentAuthorization()), buildTppAccountAccessAccounts(), buildAspspAccountAccessAccounts());
        List<AuthorisationEntity> authorisationEntities = Collections.singletonList(buildAisConsentAuthorisation());
        when(aisConsentUsageService.getUsageCounterMap(consent)).thenReturn(USAGE_COUNTER);
        when(accessMapper.mapTppAccessesToAccountAccess(consent.getTppAccountAccesses(), AdditionalAccountInformationType.NONE)).thenReturn(AIS_CONSENT.getTppAccountAccesses());
        when(accessMapper.mapAspspAccessesToAccountAccess(consent.getAspspAccountAccesses(), AdditionalAccountInformationType.NONE)).thenReturn(AIS_CONSENT.getAspspAccountAccesses());

        //When
        AisConsent result = aisConsentMapper.mapToAisConsent(consent, authorisationEntities);
        assertConsentsEqual(result, expected);
    }

    private static AisConsentData buildAisConsentData(AccountAccessType availableAccounts, AccountAccessType allPsd2, AccountAccessType availableAccountsWithBalances) {
        return buildAisConsentData(availableAccounts, allPsd2, availableAccountsWithBalances, false);
    }

    private static AccountAccess buildAspspAccountAccessAccounts() {
        return buildAccountAccessAccounts(Collections.singletonList(new AccountReference(AccountReferenceType.IBAN, ACCOUNT_IBAN, CURRENCY, RESOURCE_ID, ASPSP_ACCOUNT_ID)), Collections.emptyList(), Collections.emptyList());
    }

    private static AccountAccess buildTppAccountAccessAccounts() {
        return buildAccountAccessAccounts(Collections.singletonList(new AccountReference(AccountReferenceType.IBAN, ACCOUNT_IBAN, CURRENCY)), Collections.emptyList(), Collections.emptyList());
    }

    private static AccountAccess buildAccountAccessAccounts(List<AccountReference> accounts, List<AccountReference> balances, List<AccountReference> transactions) {
        return new AccountAccess(
            accounts,
            balances,
            transactions,
            null
        );
    }

    private static AisConsentData buildAisConsentData(AccountAccessType availableAccounts, AccountAccessType allPsd2, AccountAccessType availableAccountsWithBalances, boolean combinedServiceIndicator) {
        return new AisConsentData(
            availableAccounts,
            allPsd2,
            availableAccountsWithBalances,
            combinedServiceIndicator
        );
    }

    private void assertConsentsEqual(AisConsent result, AisConsent expected) {
        assertEquals(result.getConsentData(), expected.getConsentData());
        assertEquals(result.getId(), expected.getId());
        assertEquals(result.getInternalRequestId(), expected.getInternalRequestId());
        assertEquals(result.getConsentStatus(), expected.getConsentStatus());
        assertEquals(result.getFrequencyPerDay(), expected.getFrequencyPerDay());
        assertEquals(result.isRecurringIndicator(), expected.isRecurringIndicator());
        assertEquals(result.isMultilevelScaRequired(), expected.isMultilevelScaRequired());
        assertEquals(result.getValidUntil(), expected.getValidUntil());
        assertEquals(result.getExpireDate(), expected.getExpireDate());
        assertEquals(result.getLastActionDate(), expected.getLastActionDate());
        assertEquals(result.getCreationTimestamp(), expected.getCreationTimestamp());
        assertEquals(result.getStatusChangeTimestamp(), expected.getStatusChangeTimestamp());
        assertEquals(result.getConsentTppInformation(), expected.getConsentTppInformation());
        assertEquals(result.getAuthorisationTemplate(), expected.getAuthorisationTemplate());
        assertEquals(result.getPsuIdDataList(), expected.getPsuIdDataList());
        assertEquals(result.getAuthorisations(), expected.getAuthorisations());
        assertEquals(result.getUsages(), expected.getUsages());
        assertEquals(result.getConsentType(), expected.getConsentType());
    }


    private void assertConsentsEquals(AisAccountAccess expectedAccess, List<AuthorisationEntity> expectedAuthorisations, ConsentEntity consentEntity, CmsAisAccountConsent aisAccountConsent, AisConsent aisConsent) {
        if (!expectedAuthorisations.isEmpty() && !aisAccountConsent.getAccountConsentAuthorizations().isEmpty()) {
            AuthorisationEntity expectedAuthorisation = expectedAuthorisations.get(0);
            AisAccountConsentAuthorisation aisAccountConsentAuthorisation = aisAccountConsent.getAccountConsentAuthorizations().get(0);
            assertEquals(PSU_ID_DATA, aisAccountConsentAuthorisation.getPsuIdData());
            assertEquals(expectedAuthorisation.getScaStatus(), aisAccountConsentAuthorisation.getScaStatus());
            assertFalse(aisAccountConsent.getAccountConsentAuthorizations().isEmpty());
        } else {
            assertTrue(aisAccountConsent.getAccountConsentAuthorizations().isEmpty());
        }

        assertEquals(expectedAccess, aisAccountConsent.getAccess());
        assertEquals(consentEntity.getExternalId(), aisAccountConsent.getId());
        assertEquals(consentEntity.isRecurringIndicator(), aisAccountConsent.isRecurringIndicator());
        assertEquals(consentEntity.getValidUntil(), aisAccountConsent.getValidUntil());
        assertEquals(consentEntity.getFrequencyPerDay(), aisAccountConsent.getFrequencyPerDay());
        assertEquals(consentEntity.getLastActionDate(), aisAccountConsent.getLastActionDate());
        assertEquals(consentEntity.getConsentStatus(), aisAccountConsent.getConsentStatus());
        AccountAccess aspspAccountAccess = aisConsent.getAspspAccountAccesses();
        assertEquals(!aspspAccountAccess.getBalances().isEmpty(), aisAccountConsent.isWithBalance());
        assertEquals(consentEntity.getTppInformation().isTppRedirectPreferred(), aisAccountConsent.isTppRedirectPreferred());
        assertEquals(aisConsent.getConsentRequestType(), aisAccountConsent.getAisConsentRequestType());
        assertEquals(PSU_ID_DATA_LIST, aisAccountConsent.getPsuIdDataList());
        assertEquals(TPP_INFO, aisAccountConsent.getTppInfo());
        assertEquals(consentEntity.isMultilevelScaRequired(), aisAccountConsent.isMultilevelScaRequired());
        assertEquals(expectedAuthorisations.size(), aisAccountConsent.getAccountConsentAuthorizations().size());

        assertEquals(USAGE_COUNTER, aisAccountConsent.getUsageCounterMap());
        assertEquals(consentEntity.getCreationTimestamp(), aisAccountConsent.getCreationTimestamp());
        assertEquals(consentEntity.getStatusChangeTimestamp(), aisAccountConsent.getStatusChangeTimestamp());
        assertEquals(consentEntity.getTppInformation().getTppBrandLoggingInformation(), aisAccountConsent.getTppBrandLoggingInformation());
    }

    private static AisConsent buildAisConsent(AisConsentData aisConsentData, List<AccountConsentAuthorization> accountConsentAuthorizations, AccountAccess tppAccountAccesses, AccountAccess aspspAccountAccesses) {
        return new AisConsent(
            aisConsentData,
            EXTERNAL_ID,
            INTERNAL_REQUEST_ID,
            ConsentStatus.VALID,
            7,
            true,
            true,
            LocalDate.now().plusDays(3),
            null,
            LAST_ACTION_DATE,
            CREATION_TIMESTAMP,
            STATUS_CHANGE_TIMESTAMP,
            buildConsentTppInformation(),
            buildAuthorisationTemplate(),
            PSU_ID_DATA_LIST,
            accountConsentAuthorizations,
            USAGE_COUNTER,
            tppAccountAccesses,
            aspspAccountAccesses
        );
    }

    private static ConsentTppInformation buildConsentTppInformation() {
        ConsentTppInformation consentTppInformation = new ConsentTppInformation();
        consentTppInformation.setTppRedirectPreferred(true);
        consentTppInformation.setTppInfo(TPP_INFO);
        consentTppInformation.setTppFrequencyPerDay(7);
        return consentTppInformation;
    }

    private ConsentTppInformationEntity buildConsentTppInformationEntity() {
        ConsentTppInformationEntity consentTppInformationEntity = new ConsentTppInformationEntity();
        consentTppInformationEntity.setTppRedirectPreferred(true);
        consentTppInformationEntity.setTppInfo(TPP_INFO_ENTITY);
        consentTppInformationEntity.setTppFrequencyPerDay(7);
        consentTppInformationEntity.setTppBrandLoggingInformation(TPP_BRAND_LOGGING_INFORMATION);
        return consentTppInformationEntity;
    }

    private static AuthorisationTemplate buildAuthorisationTemplate() {
        AuthorisationTemplate authorisationTemplate = new AuthorisationTemplate();
        authorisationTemplate.setTppRedirectUri(new TppRedirectUri(REDIRECT_URI, NOK_REDIRECT_URI));
        authorisationTemplate.setCancelTppRedirectUri(new TppRedirectUri(CANCEL_REDIRECT_URI, CANCEL_NOK_REDIRECT_URI));
        return authorisationTemplate;
    }

    private AuthorisationTemplateEntity buildAuthorisationTemplateEntity() {
        AuthorisationTemplateEntity authorisationTemplateEntity = new AuthorisationTemplateEntity();
        authorisationTemplateEntity.setRedirectUri(REDIRECT_URI);
        authorisationTemplateEntity.setNokRedirectUri(NOK_REDIRECT_URI);
        authorisationTemplateEntity.setCancelRedirectUri(CANCEL_REDIRECT_URI);
        authorisationTemplateEntity.setCancelNokRedirectUri(CANCEL_NOK_REDIRECT_URI);
        return null;
    }

    private ConsentEntity buildConsent() {
        ConsentEntity consentEntity = new ConsentEntity();
        consentEntity.setExternalId(EXTERNAL_ID);
        consentEntity.setInternalRequestId(INTERNAL_REQUEST_ID);
        consentEntity.setCreationTimestamp(CREATION_TIMESTAMP);
        consentEntity.setStatusChangeTimestamp(STATUS_CHANGE_TIMESTAMP);
        consentEntity.setRecurringIndicator(true);
        consentEntity.setValidUntil(LocalDate.now().plusDays(3));
        consentEntity.setPsuDataList(Collections.singletonList(PSU_DATA));
        consentEntity.setConsentStatus(ConsentStatus.VALID);
        consentEntity.setFrequencyPerDay(7);
        consentEntity.setMultilevelScaRequired(true);
        consentEntity.setLastActionDate(LAST_ACTION_DATE);
        consentEntity.setAuthorisationTemplate(buildAuthorisationTemplateEntity());
        consentEntity.setTppInformation(buildConsentTppInformationEntity());
        return consentEntity;
    }

    private AuthorisationEntity buildAisConsentAuthorisation() {
        AuthorisationEntity authorization = new AuthorisationEntity();
        authorization.setExternalId(EXTERNAL_ID);
        authorization.setPsuData(PSU_DATA);
        authorization.setScaStatus(ScaStatus.RECEIVED);
        return authorization;
    }

    private AccountConsentAuthorization buildAccountConsentAuthorization() {
        AccountConsentAuthorization accountConsentAuthorization = new AccountConsentAuthorization();
        accountConsentAuthorization.setId(EXTERNAL_ID);
        accountConsentAuthorization.setPsuIdData(PSU_ID_DATA);
        accountConsentAuthorization.setScaStatus(ScaStatus.RECEIVED);
        return accountConsentAuthorization;
    }

    private AisAccountAccess buildAisAccountAccessAccountWithoutResourceId(boolean global) {
        AccountReference accountReference = new AccountReference(AccountReferenceType.IBAN, ACCOUNT_IBAN, CURRENCY);
        List<AccountReference> accountReferences = Collections.singletonList(accountReference);
        return new AisAccountAccess(accountReferences, Collections.emptyList(), Collections.emptyList(), null, global ? AccountAccessType.ALL_ACCOUNTS.toString() : null, null, null);
    }

    private AisAccountAccess buildAisAccountAccessAccountsWithResourceId() {
        AccountReference accountReference = new AccountReference(AccountReferenceType.IBAN, ACCOUNT_IBAN, CURRENCY,
                                                                 RESOURCE_ID, ASPSP_ACCOUNT_ID);
        List<AccountReference> accountReferences = Collections.singletonList(accountReference);
        return new AisAccountAccess(accountReferences, Collections.emptyList(), Collections.emptyList(), null, null, null, null);
    }

    private static TppInfoEntity buildTppInfoEntity() {
        TppInfoEntity tppInfoEntity = new TppInfoEntity();
        tppInfoEntity.setAuthorisationNumber(TPP_AUTHORISATION_NUMBER);
        tppInfoEntity.setAuthorityId(TPP_AUTHORITY_ID);
        tppInfoEntity.setTppRoles(TPP_ROLES);
        return tppInfoEntity;
    }

    private static TppInfo buildTppInfo() {
        TppInfo tppInfo = new TppInfo();
        tppInfo.setAuthorisationNumber(TPP_AUTHORISATION_NUMBER);
        tppInfo.setAuthorityId(TPP_AUTHORITY_ID);
        tppInfo.setTppRoles(TPP_ROLES);
        return tppInfo;
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
        return new PsuIdData(PSU_ID, PSU_ID_TYPE, PSU_CORPORATE_ID, PSU_CORPORATE_ID_TYPE, PSU_IP_ADDRESS);
    }
}
