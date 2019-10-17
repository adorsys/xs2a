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

package de.adorsys.psd2.consent.service.aspsp;

import de.adorsys.psd2.consent.aspsp.api.piis.CreatePiisConsentRequest;
import de.adorsys.psd2.consent.domain.AccountReferenceEntity;
import de.adorsys.psd2.consent.domain.PsuData;
import de.adorsys.psd2.consent.domain.piis.PiisConsentEntity;
import de.adorsys.psd2.consent.repository.PiisConsentRepository;
import de.adorsys.psd2.consent.repository.specification.PiisConsentEntitySpecification;
import de.adorsys.psd2.consent.service.aspsp.CmsAspspPiisServiceInternal;
import de.adorsys.psd2.consent.service.mapper.PiisConsentMapper;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.piis.PiisConsent;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;


@RunWith(MockitoJUnitRunner.class)
public class CmsAspspPiisServiceInternalTest {
    private static final long CONSENT_INTERNAL_ID = 1;
    private static final String CONSENT_EXTERNAL_ID = "5bcf664f-68ce-498d-9a93-fe0cce32f6b6";
    private static final String CONSENT_EXTERNAL_ID_WRONG = "efe6d8bd-c6bc-4866-81a3-87ac755ffa4b";
    private static final String PSU_ID = "PSU-ID-1";
    private static final String PSU_ID_WRONG = "PSU-ID-2";
    private static final LocalDate EXPIRE_DATE = LocalDate.now().plusDays(100);
    private static final String DEFAULT_SERVICE_INSTANCE_ID = "UNDEFINED";
    private static final OffsetDateTime CREATION_TIMESTAMP = OffsetDateTime.of(2019, 2, 4, 12, 0, 0, 0, ZoneOffset.UTC);
    private static final String TPP_AUTHORISATION_NUMBER = "authorisation number";
    private static final String CARD_NUMBER = "1234567891234";
    private static final LocalDate CARD_EXPIRY_DATE = LocalDate.now().plusDays(1);
    private static final String CARD_INFORMATION = "MyMerchant Loyalty Card";
    private static final String REGISTRATION_INFORMATION = "Your contract Number 1234 with MyMerchant is completed with the registration with your bank.";
    private static final LocalDate VALID_UNTIL_DATE = LocalDate.now().plusDays(1);

    @Mock
    private PiisConsentRepository piisConsentRepository;
    @Mock
    private PiisConsentMapper piisConsentMapper;
    @Mock
    private PiisConsentEntitySpecification piisConsentEntitySpecification;
    @InjectMocks
    private CmsAspspPiisServiceInternal cmsAspspPiisServiceInternal;
    private PsuIdData psuIdData;

    @Before
    public void setUp() {
        psuIdData = buildPsuIdData();
        when(piisConsentMapper.mapToPiisConsent(buildPiisConsentEntity())).thenReturn(buildPiisConsent());
        when(piisConsentRepository.save(any(PiisConsentEntity.class))).thenReturn(buildPiisConsentEntity());
    }

    @Test
    public void createConsent_success() {
        PiisConsentEntity piisConsentEntity = buildConsent();
        CreatePiisConsentRequest request = buildCreatePiisConsentRequest(buildAccountReference(), VALID_UNTIL_DATE);
        when(piisConsentMapper.mapToPiisConsentEntity(psuIdData, request)).thenReturn(piisConsentEntity);
        when(piisConsentRepository.save(any(PiisConsentEntity.class)))
            .thenReturn(piisConsentEntity);

        // Given
        ArgumentCaptor<PiisConsentEntity> argumentCaptor = ArgumentCaptor.forClass(PiisConsentEntity.class);

        // When
        Optional<String> actual = cmsAspspPiisServiceInternal.createConsent(psuIdData, request);

        // Then
        assertThat(actual.isPresent()).isTrue();
        assertThat(StringUtils.isNotBlank(actual.get())).isTrue();
        verify(piisConsentRepository).save(argumentCaptor.capture());

        PiisConsentEntity piisConsent = argumentCaptor.getValue();
        assertThat(StringUtils.isNotBlank(piisConsent.getExternalId())).isTrue();
        Assert.assertEquals(buildAccountReferenceEntity(), piisConsent.getAccount());
        Assert.assertEquals(VALID_UNTIL_DATE, piisConsent.getExpireDate());
        Assert.assertEquals(request.getCardNumber(), piisConsent.getCardNumber());
        Assert.assertEquals(request.getCardExpiryDate(), piisConsent.getCardExpiryDate());
        Assert.assertEquals(request.getCardInformation(), piisConsent.getCardInformation());
        Assert.assertEquals(request.getRegistrationInformation(), piisConsent.getRegistrationInformation());
    }

    @Test
    public void createConsentClosePreviousPiisConsents_success() {
        PiisConsentEntity piisConsentEntity = buildConsent();
        CreatePiisConsentRequest request = buildCreatePiisConsentRequest(buildAccountReference(), VALID_UNTIL_DATE);
        when(piisConsentMapper.mapToPiisConsentEntity(psuIdData, request)).thenReturn(piisConsentEntity);
        when(piisConsentRepository.save(any(PiisConsentEntity.class)))
            .thenReturn(piisConsentEntity);

        // Given
        AccountReference accountReference = buildAccountReference();

        when(piisConsentEntitySpecification.byPsuIdDataAndAuthorisationNumberAndAccountReference(psuIdData, TPP_AUTHORISATION_NUMBER, accountReference))
            .thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        List<PiisConsentEntity> piisConsentEntities = Arrays.asList(buildPiisConsentEntity(), buildPiisConsentEntity());
        //noinspection unchecked
        when(piisConsentRepository.findAll(any(Specification.class))).thenReturn(piisConsentEntities);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<PiisConsentEntity>> argumentCaptor = ArgumentCaptor.forClass(List.class);

        // When
        cmsAspspPiisServiceInternal.createConsent(psuIdData, request);

        // Then
        verify(piisConsentRepository).saveAll(argumentCaptor.capture());

        List<PiisConsentEntity> previousPiisConsent = argumentCaptor.getValue();
        assertEquals(piisConsentEntities.size(), previousPiisConsent.size());

        verify(piisConsentEntitySpecification, times(1))
            .byPsuIdDataAndAuthorisationNumberAndAccountReference(psuIdData, TPP_AUTHORISATION_NUMBER, accountReference);

        Set<ConsentStatus> consentStatuses = previousPiisConsent.stream()
                                                 .map(PiisConsentEntity::getConsentStatus)
                                                 .collect(Collectors.toSet());

        assertEquals(1, consentStatuses.size());
        assertTrue(consentStatuses.contains(ConsentStatus.REVOKED_BY_PSU));
    }

    @Test
    public void createConsent_shouldNotCloseFinalisedConsents() {
        PiisConsentEntity piisConsentEntity = buildConsent();
        CreatePiisConsentRequest request = buildCreatePiisConsentRequest(buildAccountReference(), VALID_UNTIL_DATE);
        when(piisConsentMapper.mapToPiisConsentEntity(psuIdData, request)).thenReturn(piisConsentEntity);
        when(piisConsentRepository.save(any(PiisConsentEntity.class)))
            .thenReturn(piisConsentEntity);

        // Given
        AccountReference accountReference = buildAccountReference();

        @SuppressWarnings("unchecked")
        Specification<PiisConsentEntity> mockSpecification = Mockito.mock(Specification.class);
        when(piisConsentEntitySpecification.byPsuIdDataAndAuthorisationNumberAndAccountReference(psuIdData, TPP_AUTHORISATION_NUMBER, accountReference))
            .thenReturn(mockSpecification);
        List<PiisConsentEntity> piisConsentEntities = Arrays.asList(buildPiisConsentEntity(ConsentStatus.TERMINATED_BY_ASPSP), buildPiisConsentEntity());
        when(piisConsentRepository.findAll(mockSpecification)).thenReturn(piisConsentEntities);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<PiisConsentEntity>> argumentCaptor = ArgumentCaptor.forClass(List.class);

        // When
        cmsAspspPiisServiceInternal.createConsent(psuIdData, request);

        // Then
        verify(piisConsentRepository).saveAll(argumentCaptor.capture());

        List<PiisConsentEntity> closedPiisConsents = argumentCaptor.getValue();
        assertEquals(1, closedPiisConsents.size());
        assertEquals(ConsentStatus.REVOKED_BY_PSU, closedPiisConsents.get(0).getConsentStatus());
    }

    @Test
    public void createConsent_withExpireDateToday_success() {
        // Given
        PiisConsentEntity piisConsentEntity = buildConsent();
        CreatePiisConsentRequest request = buildCreatePiisConsentRequest(buildAccountReference(), VALID_UNTIL_DATE);
        when(piisConsentMapper.mapToPiisConsentEntity(psuIdData, request)).thenReturn(piisConsentEntity);
        when(piisConsentRepository.save(any(PiisConsentEntity.class)))
            .thenReturn(piisConsentEntity);

        // When
        Optional<String> actual = cmsAspspPiisServiceInternal.createConsent(psuIdData, request);

        // Then
        assertThat(actual.isPresent()).isTrue();
        assertThat(StringUtils.isNotBlank(actual.get())).isTrue();
    }

    @Test
    public void createConsent_savingFailed_shouldFail() {
        // Given
        PiisConsentEntity piisConsentEntity = buildConsent(null);
        CreatePiisConsentRequest request = buildCreatePiisConsentRequest(buildAccountReference(), EXPIRE_DATE);
        when(piisConsentMapper.mapToPiisConsentEntity(psuIdData, request)).thenReturn(piisConsentEntity);
        when(piisConsentRepository.save(any(PiisConsentEntity.class)))
            .thenReturn(piisConsentEntity);

        // When
        Optional<String> actual = cmsAspspPiisServiceInternal.createConsent(psuIdData, request);

        // Then
        assertThat(actual.isPresent()).isFalse();
    }

    @Test
    public void createConsent_withEmptyPsuIdDate_shouldFail() {
        // Given
        PsuIdData emptyPsuIdData = new PsuIdData(null, null, null, null);
        CreatePiisConsentRequest request = buildCreatePiisConsentRequest(null, EXPIRE_DATE);

        // When
        Optional<String> actual = cmsAspspPiisServiceInternal.createConsent(emptyPsuIdData, request);

        // Then
        assertThat(actual.isPresent()).isFalse();
        verify(piisConsentRepository, never()).save(any(PiisConsentEntity.class));
    }

    @Test
    public void createConsent_withNullAccounts_shouldFail() {
        // Given
        PsuIdData psuIdData = buildPsuIdData();
        CreatePiisConsentRequest request = buildCreatePiisConsentRequest(null, EXPIRE_DATE);

        // When
        Optional<String> actual = cmsAspspPiisServiceInternal.createConsent(psuIdData, request);

        // Then
        assertThat(actual.isPresent()).isFalse();
        verify(piisConsentRepository, never()).save(any(PiisConsentEntity.class));
    }

    @Test
    public void createConsent_withInvalidExpireDate_shouldFail() {
        // Given
        PsuIdData psuIdData = buildPsuIdData();
        LocalDate yesterdayDate = LocalDate.now().minusDays(1);
        CreatePiisConsentRequest request = buildCreatePiisConsentRequest(null, yesterdayDate);

        // When
        Optional<String> actual = cmsAspspPiisServiceInternal.createConsent(psuIdData, request);

        // Then
        assertThat(actual.isPresent()).isFalse();
        verify(piisConsentRepository, never()).save(any(PiisConsentEntity.class));
    }

    @Test
    public void createConsent_withNullExpireDate_shouldFail() {
        // Given
        PsuIdData psuIdData = buildPsuIdData();
        CreatePiisConsentRequest request = buildCreatePiisConsentRequest(buildAccountReference(), null);

        // When
        Optional<String> actual = cmsAspspPiisServiceInternal.createConsent(psuIdData, request);

        // Then
        assertThat(actual.isPresent()).isFalse();
        verify(piisConsentRepository, never()).save(any(PiisConsentEntity.class));
    }

    @Test
    public void createConsent_withCardExpireDate_shouldFail() {
        // Given
        PsuIdData psuIdData = buildPsuIdData();
        LocalDate yesterdayDate = LocalDate.now().minusDays(1);
        CreatePiisConsentRequest request = buildCreatePiisConsentRequest(null, yesterdayDate);
        request.setCardExpiryDate(LocalDate.now().minusDays(1));

        // When
        Optional<String> actual = cmsAspspPiisServiceInternal.createConsent(psuIdData, request);

        // Then
        assertThat(actual.isPresent()).isFalse();
        verify(piisConsentRepository, never()).save(any(PiisConsentEntity.class));
    }

    @Test
    public void getConsentsForPsu_Success() {
        // Given
        PsuIdData psuIdData = buildPsuIdData(PSU_ID);
        when(piisConsentEntitySpecification.byPsuDataAndInstanceId(psuIdData, DEFAULT_SERVICE_INSTANCE_ID)).thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        //noinspection unchecked
        when(piisConsentRepository.findAll(any(Specification.class)))
            .thenReturn(Collections.singletonList(buildPiisConsentEntity()));
        PiisConsent expected = buildPiisConsent();

        // When
        List<PiisConsent> actual = cmsAspspPiisServiceInternal.getConsentsForPsu(psuIdData, DEFAULT_SERVICE_INSTANCE_ID);

        // Then
        assertThat(actual.isEmpty()).isFalse();
        assertThat(actual.get(0)).isEqualTo(expected);
        verify(piisConsentEntitySpecification, times(1))
            .byPsuDataAndInstanceId(psuIdData, DEFAULT_SERVICE_INSTANCE_ID);
    }

    @Test
    public void getConsentsForPsu_Failure_WrongPsuId() {
        // Given
        PsuIdData psuIdData = buildPsuIdData(PSU_ID_WRONG);

        // When
        List<PiisConsent> actual = cmsAspspPiisServiceInternal.getConsentsForPsu(psuIdData, DEFAULT_SERVICE_INSTANCE_ID);

        // Then
        assertThat(actual.isEmpty()).isTrue();
        verify(piisConsentEntitySpecification, times(1))
            .byPsuDataAndInstanceId(psuIdData, DEFAULT_SERVICE_INSTANCE_ID);
    }

    @Test
    public void getConsentsForPsu_withEmptyPsuId_shouldReturnEmpty() {
        // Given
        PsuIdData emptyPsuIdData = buildPsuIdData(null);

        // When
        List<PiisConsent> actual = cmsAspspPiisServiceInternal.getConsentsForPsu(emptyPsuIdData, DEFAULT_SERVICE_INSTANCE_ID);

        // Then
        assertThat(actual.isEmpty()).isTrue();
        verify(piisConsentRepository, never()).findAll(any());
    }

    @Test
    public void terminateConsent_Success() {
        // Given
        when(piisConsentEntitySpecification.byConsentIdAndInstanceId(CONSENT_EXTERNAL_ID, DEFAULT_SERVICE_INSTANCE_ID))
            .thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        //noinspection unchecked
        when(piisConsentRepository.findOne(any(Specification.class))).thenReturn(Optional.of(buildPiisConsentEntity()));
        ArgumentCaptor<PiisConsentEntity> argumentCaptor = ArgumentCaptor.forClass(PiisConsentEntity.class);

        // When
        boolean actual = cmsAspspPiisServiceInternal.terminateConsent(CONSENT_EXTERNAL_ID, DEFAULT_SERVICE_INSTANCE_ID);

        // Then
        assertThat(actual).isTrue();
        verify(piisConsentRepository).save(argumentCaptor.capture());
        assertThat(argumentCaptor.getValue().getConsentStatus()).isEqualTo(ConsentStatus.TERMINATED_BY_ASPSP);
        verify(piisConsentEntitySpecification, times(1))
            .byConsentIdAndInstanceId(CONSENT_EXTERNAL_ID, DEFAULT_SERVICE_INSTANCE_ID);
    }

    @Test
    public void terminateConsent_Failure_WrongConsentId() {
        // Given

        // When
        boolean actual = cmsAspspPiisServiceInternal.terminateConsent(CONSENT_EXTERNAL_ID_WRONG, DEFAULT_SERVICE_INSTANCE_ID);

        // Then
        assertThat(actual).isFalse();
        verify(piisConsentRepository, never()).save(any(PiisConsentEntity.class));
        verify(piisConsentEntitySpecification, times(1))
            .byConsentIdAndInstanceId(CONSENT_EXTERNAL_ID_WRONG, DEFAULT_SERVICE_INSTANCE_ID);
    }

    private PiisConsentEntity buildConsent() {
        return buildConsent(CONSENT_INTERNAL_ID);
    }

    private PiisConsentEntity buildConsent(Long id) {
        PiisConsentEntity piisConsent = new PiisConsentEntity();
        piisConsent.setId(id);
        piisConsent.setAccount(buildAccountReferenceEntity());
        piisConsent.setExternalId(CONSENT_EXTERNAL_ID);
        piisConsent.setRequestDateTime(OffsetDateTime.now());
        piisConsent.setPsuData(buildPsuData());
        piisConsent.setConsentStatus(ConsentStatus.RECEIVED);
        piisConsent.setExpireDate(VALID_UNTIL_DATE);
        piisConsent.setCardNumber(CARD_NUMBER);
        piisConsent.setCardExpiryDate(CARD_EXPIRY_DATE);
        piisConsent.setCardInformation(CARD_INFORMATION);
        piisConsent.setRegistrationInformation(REGISTRATION_INFORMATION);
        return piisConsent;
    }

    private PsuData buildPsuData() {
        return new PsuData(PSU_ID, null, null, null);
    }

    private PsuIdData buildPsuIdData() {
        return buildPsuIdData(PSU_ID);
    }

    private PsuIdData buildPsuIdData(String psuId) {
        return new PsuIdData(psuId, null, null, null);
    }

    private AccountReference buildAccountReference() {
        return new AccountReference("aspspAccountId", "resourceId",
                                    "DE89370400440532013000",
                                    null,
                                    null,
                                    null,
                                    null,
                                    null);
    }

    private AccountReferenceEntity buildAccountReferenceEntity() {
        AccountReference accountReference = buildAccountReference();
        AccountReferenceEntity accountReferenceEntity = new AccountReferenceEntity();
        accountReferenceEntity.setAspspAccountId(accountReference.getAspspAccountId());
        accountReferenceEntity.setIban(accountReference.getIban());
        return accountReferenceEntity;
    }

    private PiisConsentEntity buildPiisConsentEntity() {
        return buildPiisConsentEntity(ConsentStatus.RECEIVED);
    }

    private PiisConsentEntity buildPiisConsentEntity(ConsentStatus consentStatus) {
        PiisConsentEntity consentEntity = new PiisConsentEntity();
        consentEntity.setExternalId(CONSENT_EXTERNAL_ID);
        consentEntity.setCreationTimestamp(CREATION_TIMESTAMP);
        consentEntity.setConsentStatus(consentStatus);
        return consentEntity;
    }

    private PiisConsent buildPiisConsent() {
        PiisConsent consent = new PiisConsent();
        consent.setId(CONSENT_EXTERNAL_ID);
        consent.setCreationTimestamp(CREATION_TIMESTAMP);
        consent.setConsentStatus(ConsentStatus.RECEIVED);
        return consent;
    }

    private CreatePiisConsentRequest buildCreatePiisConsentRequest(AccountReference account, LocalDate validUntil) {
        CreatePiisConsentRequest request = new CreatePiisConsentRequest();
        request.setAccount(account);
        request.setValidUntil(validUntil);
        request.setCardNumber(CARD_NUMBER);
        request.setCardExpiryDate(CARD_EXPIRY_DATE);
        request.setCardInformation(CARD_INFORMATION);
        request.setRegistrationInformation(REGISTRATION_INFORMATION);
        request.setTppAuthorisationNumber(TPP_AUTHORISATION_NUMBER);
        return request;
    }
}
