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

package de.adorsys.psd2.consent.service.aspsp;

import de.adorsys.psd2.consent.api.TypeAccess;
import de.adorsys.psd2.consent.api.piis.v1.CmsPiisConsent;
import de.adorsys.psd2.consent.aspsp.api.PageData;
import de.adorsys.psd2.consent.aspsp.api.piis.CreatePiisConsentRequest;
import de.adorsys.psd2.consent.domain.PsuData;
import de.adorsys.psd2.consent.domain.TppInfoEntity;
import de.adorsys.psd2.consent.domain.account.AspspAccountAccess;
import de.adorsys.psd2.consent.domain.consent.ConsentEntity;
import de.adorsys.psd2.consent.repository.AspspAccountAccessRepository;
import de.adorsys.psd2.consent.repository.ConsentJpaRepository;
import de.adorsys.psd2.consent.repository.TppInfoRepository;
import de.adorsys.psd2.consent.repository.specification.PiisConsentEntitySpecification;
import de.adorsys.psd2.consent.service.mapper.PiisConsentMapper;
import de.adorsys.psd2.consent.service.migration.PiisConsentLazyMigrationService;
import de.adorsys.psd2.consent.service.psu.util.PageRequestBuilder;
import de.adorsys.psd2.core.mapper.ConsentDataMapper;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class CmsAspspPiisServiceInternalTest {
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
    private static final byte[] DATA = ArrayUtils.addAll(ArrayUtils.addAll(ArrayUtils.addAll(CARD_NUMBER.getBytes(), CARD_EXPIRY_DATE.toString().getBytes()), CARD_INFORMATION.getBytes()), REGISTRATION_INFORMATION.getBytes());

    @Mock
    private ConsentDataMapper consentDataMapper;
    @Mock
    private TppInfoRepository tppInfoRepository;
    @Mock
    private PiisConsentLazyMigrationService piisConsentLazyMigrationService;
    @Mock
    private ConsentJpaRepository consentJpaRepository;
    @Mock
    private PiisConsentMapper piisConsentMapper;
    @Mock
    private PiisConsentEntitySpecification piisConsentEntitySpecification;
    @Mock
    private AspspAccountAccessRepository aspspAccountAccessRepository;
    @Spy
    private PageRequestBuilder pageRequestBuilder = new PageRequestBuilder();
    @InjectMocks
    private CmsAspspPiisServiceInternal cmsAspspPiisServiceInternal;
    private PsuIdData psuIdData;

    @BeforeEach
    void setUp() {
        psuIdData = buildPsuIdData();
    }

    @Test
    void createConsent_success() {
        ConsentEntity piisConsentEntity = buildConsent();
        CreatePiisConsentRequest request = buildCreatePiisConsentRequest(buildAccountReference(), VALID_UNTIL_DATE);
        when(piisConsentMapper.mapToPiisConsentEntity(psuIdData, buildTppInfoEntity(), request, DEFAULT_SERVICE_INSTANCE_ID)).thenReturn(piisConsentEntity);
        when(consentJpaRepository.save(any(ConsentEntity.class)))
            .thenReturn(piisConsentEntity);
        when(tppInfoRepository.findByAuthorisationNumber(TPP_AUTHORISATION_NUMBER)).thenReturn(Optional.of(buildTppInfoEntity()));

        // Given
        ArgumentCaptor<ConsentEntity> argumentCaptor = ArgumentCaptor.forClass(ConsentEntity.class);

        // When
        Optional<String> actual = cmsAspspPiisServiceInternal.createConsent(psuIdData, request, DEFAULT_SERVICE_INSTANCE_ID);

        // Then
        assertTrue(actual.isPresent());
        assertTrue(StringUtils.isNotBlank(actual.get()));
        verify(consentJpaRepository).save(argumentCaptor.capture());

        ConsentEntity piisConsent = argumentCaptor.getValue();
        assertTrue(StringUtils.isNotBlank(piisConsent.getExternalId()));
        assertEquals(Collections.singletonList(buildAspspAccountAccess(buildAccountReference())), piisConsent.getAspspAccountAccesses());
        assertEquals(VALID_UNTIL_DATE, piisConsent.getValidUntil());
        assertEquals(DATA, piisConsent.getData());
    }

    @Test
    void createConsentClosePreviousPiisConsents_success() {
        ConsentEntity piisConsentEntity = buildConsent();
        CreatePiisConsentRequest request = buildCreatePiisConsentRequest(buildAccountReference(), VALID_UNTIL_DATE);
        when(piisConsentMapper.mapToPiisConsentEntity(psuIdData, buildTppInfoEntity(), request, DEFAULT_SERVICE_INSTANCE_ID)).thenReturn(piisConsentEntity);
        when(consentJpaRepository.save(any(ConsentEntity.class)))
            .thenReturn(piisConsentEntity);
        when(tppInfoRepository.findByAuthorisationNumber(TPP_AUTHORISATION_NUMBER)).thenReturn(Optional.of(buildTppInfoEntity()));

        // Given
        AccountReference accountReference = buildAccountReference();

        when(piisConsentEntitySpecification.byPsuIdDataAndAuthorisationNumberAndAccountReferenceAndInstanceId(psuIdData, TPP_AUTHORISATION_NUMBER, accountReference, DEFAULT_SERVICE_INSTANCE_ID))
            .thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        List<ConsentEntity> piisConsentEntities = Arrays.asList(buildPiisConsentEntity(), buildPiisConsentEntity());
        //noinspection unchecked
        when(consentJpaRepository.findAll(any(Specification.class))).thenReturn(piisConsentEntities);
        when(piisConsentLazyMigrationService.migrateIfNeeded(piisConsentEntities)).thenReturn(piisConsentEntities);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<ConsentEntity>> argumentCaptor = ArgumentCaptor.forClass(List.class);

        // When
        cmsAspspPiisServiceInternal.createConsent(psuIdData, request, DEFAULT_SERVICE_INSTANCE_ID);

        // Then
        verify(consentJpaRepository).saveAll(argumentCaptor.capture());

        List<ConsentEntity> previousPiisConsent = argumentCaptor.getValue();
        assertEquals(piisConsentEntities.size(), previousPiisConsent.size());

        verify(piisConsentEntitySpecification, times(1))
            .byPsuIdDataAndAuthorisationNumberAndAccountReferenceAndInstanceId(psuIdData, TPP_AUTHORISATION_NUMBER, accountReference, DEFAULT_SERVICE_INSTANCE_ID);

        Set<ConsentStatus> consentStatuses = previousPiisConsent.stream()
                                                 .map(ConsentEntity::getConsentStatus)
                                                 .collect(Collectors.toSet());

        assertEquals(1, consentStatuses.size());
        assertTrue(consentStatuses.contains(ConsentStatus.REVOKED_BY_PSU));
    }

    @Test
    void createConsent_shouldNotCloseFinalisedConsents() {
        ConsentEntity piisConsentEntity = buildConsent();
        CreatePiisConsentRequest request = buildCreatePiisConsentRequest(buildAccountReference(), VALID_UNTIL_DATE);
        when(piisConsentMapper.mapToPiisConsentEntity(psuIdData, buildTppInfoEntity(), request, DEFAULT_SERVICE_INSTANCE_ID)).thenReturn(piisConsentEntity);
        when(consentJpaRepository.save(any(ConsentEntity.class)))
            .thenReturn(piisConsentEntity);
        when(tppInfoRepository.findByAuthorisationNumber(TPP_AUTHORISATION_NUMBER)).thenReturn(Optional.of(buildTppInfoEntity()));

        // Given
        AccountReference accountReference = buildAccountReference();

        @SuppressWarnings("unchecked")
        Specification<ConsentEntity> mockSpecification = Mockito.mock(Specification.class);
        when(piisConsentEntitySpecification.byPsuIdDataAndAuthorisationNumberAndAccountReferenceAndInstanceId(psuIdData, TPP_AUTHORISATION_NUMBER, accountReference, DEFAULT_SERVICE_INSTANCE_ID))
            .thenReturn(mockSpecification);
        List<ConsentEntity> piisConsentEntities = Arrays.asList(buildPiisConsentEntity(ConsentStatus.TERMINATED_BY_ASPSP), buildPiisConsentEntity());
        when(consentJpaRepository.findAll(mockSpecification)).thenReturn(piisConsentEntities);
        when(piisConsentLazyMigrationService.migrateIfNeeded(piisConsentEntities)).thenReturn(piisConsentEntities);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<ConsentEntity>> argumentCaptor = ArgumentCaptor.forClass(List.class);

        // When
        cmsAspspPiisServiceInternal.createConsent(psuIdData, request, DEFAULT_SERVICE_INSTANCE_ID);

        // Then
        verify(consentJpaRepository).saveAll(argumentCaptor.capture());

        List<ConsentEntity> closedPiisConsents = argumentCaptor.getValue();
        assertEquals(1, closedPiisConsents.size());
        assertEquals(ConsentStatus.REVOKED_BY_PSU, closedPiisConsents.get(0).getConsentStatus());
    }

    @Test
    void createConsent_withExpireDateToday_success() {
        // Given
        ConsentEntity piisConsentEntity = buildConsent();
        CreatePiisConsentRequest request = buildCreatePiisConsentRequest(buildAccountReference(), VALID_UNTIL_DATE);
        when(piisConsentMapper.mapToPiisConsentEntity(psuIdData, buildTppInfoEntity(), request, DEFAULT_SERVICE_INSTANCE_ID)).thenReturn(piisConsentEntity);
        when(consentJpaRepository.save(any(ConsentEntity.class)))
            .thenReturn(piisConsentEntity);
        when(tppInfoRepository.findByAuthorisationNumber(TPP_AUTHORISATION_NUMBER)).thenReturn(Optional.of(buildTppInfoEntity()));

        // When
        Optional<String> actual = cmsAspspPiisServiceInternal.createConsent(psuIdData, request, DEFAULT_SERVICE_INSTANCE_ID);

        // Then
        assertTrue(actual.isPresent());
        assertTrue(StringUtils.isNotBlank(actual.get()));
    }

    @Test
    void createConsent_savingFailed_shouldFail() {
        // Given
        ConsentEntity piisConsentEntity = buildConsent(null);
        CreatePiisConsentRequest request = buildCreatePiisConsentRequest(buildAccountReference(), EXPIRE_DATE);
        when(piisConsentMapper.mapToPiisConsentEntity(psuIdData, buildTppInfoEntity(), request, DEFAULT_SERVICE_INSTANCE_ID)).thenReturn(piisConsentEntity);
        when(consentJpaRepository.save(any(ConsentEntity.class)))
            .thenReturn(piisConsentEntity);
        when(tppInfoRepository.findByAuthorisationNumber(TPP_AUTHORISATION_NUMBER)).thenReturn(Optional.of(buildTppInfoEntity()));

        // When
        Optional<String> actual = cmsAspspPiisServiceInternal.createConsent(psuIdData, request, DEFAULT_SERVICE_INSTANCE_ID);

        // Then
        assertFalse(actual.isPresent());
    }

    @Test
    void createConsent_withEmptyPsuIdDate_shouldFail() {
        // Given
        PsuIdData emptyPsuIdData = new PsuIdData(null, null, null, null, null);
        CreatePiisConsentRequest request = buildCreatePiisConsentRequest(null, EXPIRE_DATE);

        // When
        Optional<String> actual = cmsAspspPiisServiceInternal.createConsent(emptyPsuIdData, request, DEFAULT_SERVICE_INSTANCE_ID);

        // Then
        assertFalse(actual.isPresent());
        verify(consentJpaRepository, never()).save(any(ConsentEntity.class));
    }

    @Test
    void createConsent_withNullAccounts_shouldFail() {
        // Given
        PsuIdData psuIdData = buildPsuIdData();
        CreatePiisConsentRequest request = buildCreatePiisConsentRequest(null, EXPIRE_DATE);

        // When
        Optional<String> actual = cmsAspspPiisServiceInternal.createConsent(psuIdData, request, DEFAULT_SERVICE_INSTANCE_ID);

        // Then
        assertFalse(actual.isPresent());
        verify(consentJpaRepository, never()).save(any(ConsentEntity.class));
    }

    @Test
    void createConsent_withInvalidExpireDate_shouldFail() {
        // Given
        PsuIdData psuIdData = buildPsuIdData();
        LocalDate yesterdayDate = LocalDate.now().minusDays(1);
        CreatePiisConsentRequest request = buildCreatePiisConsentRequest(null, yesterdayDate);

        // When
        Optional<String> actual = cmsAspspPiisServiceInternal.createConsent(psuIdData, request, DEFAULT_SERVICE_INSTANCE_ID);

        // Then
        assertFalse(actual.isPresent());
        verify(consentJpaRepository, never()).save(any(ConsentEntity.class));
    }

    @Test
    void createConsent_withNullExpireDate_shouldFail() {
        // Given
        PsuIdData psuIdData = buildPsuIdData();
        CreatePiisConsentRequest request = buildCreatePiisConsentRequest(buildAccountReference(), null);

        // When
        Optional<String> actual = cmsAspspPiisServiceInternal.createConsent(psuIdData, request, DEFAULT_SERVICE_INSTANCE_ID);

        // Then
        assertFalse(actual.isPresent());
        verify(consentJpaRepository, never()).save(any(ConsentEntity.class));
    }

    @Test
    void createConsent_withCardExpireDate_shouldFail() {
        // Given
        PsuIdData psuIdData = buildPsuIdData();
        LocalDate yesterdayDate = LocalDate.now().minusDays(1);
        CreatePiisConsentRequest request = buildCreatePiisConsentRequest(null, yesterdayDate);
        request.setCardExpiryDate(LocalDate.now().minusDays(1));

        // When
        Optional<String> actual = cmsAspspPiisServiceInternal.createConsent(psuIdData, request, DEFAULT_SERVICE_INSTANCE_ID);

        // Then
        assertFalse(actual.isPresent());
        verify(consentJpaRepository, never()).save(any(ConsentEntity.class));
    }

    @Test
    void getConsentsForPsu_Success() {
        // Given
        PsuIdData psuIdData = buildPsuIdData(PSU_ID);
        when(piisConsentEntitySpecification.byPsuDataAndInstanceId(psuIdData, DEFAULT_SERVICE_INSTANCE_ID)).thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        //noinspection unchecked
        List<ConsentEntity> piisConsentEntities = Collections.singletonList(buildPiisConsentEntity());
        when(consentJpaRepository.findAll(any(Specification.class), eq(PageRequest.of(0, 20))))
            .thenReturn(new PageImpl<>(piisConsentEntities, PageRequest.of(0, 20), 1));
        when(piisConsentLazyMigrationService.migrateIfNeeded(buildPiisConsentEntity())).thenReturn(buildPiisConsentEntity());
        when(piisConsentMapper.mapToCmsPiisConsent(buildPiisConsentEntity())).thenReturn(buildCmsPiisConsent());
        CmsPiisConsent expected = buildCmsPiisConsent();

        // When
        PageData<List<CmsPiisConsent>> actual = cmsAspspPiisServiceInternal.getConsentsForPsu(psuIdData, DEFAULT_SERVICE_INSTANCE_ID, 0, 20);

        // Then
        assertFalse(actual.getData().isEmpty());
        assertEquals(expected, actual.getData().get(0));
        verify(piisConsentEntitySpecification, times(1))
            .byPsuDataAndInstanceId(psuIdData, DEFAULT_SERVICE_INSTANCE_ID);
    }

    @Test
    void getConsentsForPsu_SuccessPagination() {
        // Given
        PsuIdData psuIdData = buildPsuIdData(PSU_ID);
        when(piisConsentEntitySpecification.byPsuDataAndInstanceId(psuIdData, DEFAULT_SERVICE_INSTANCE_ID)).thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        //noinspection unchecked
        List<ConsentEntity> piisConsentEntities = Collections.singletonList(buildPiisConsentEntity());
        when(consentJpaRepository.findAll(any(Specification.class), eq(PageRequest.of(0, 20))))
            .thenReturn(new PageImpl<>(piisConsentEntities, PageRequest.of(0, 20), 1));
        when(piisConsentLazyMigrationService.migrateIfNeeded(buildPiisConsentEntity())).thenReturn(buildPiisConsentEntity());
        when(piisConsentMapper.mapToCmsPiisConsent(buildPiisConsentEntity())).thenReturn(buildCmsPiisConsent());
        CmsPiisConsent expected = buildCmsPiisConsent();

        // When
        PageData<List<CmsPiisConsent>> actual = cmsAspspPiisServiceInternal.getConsentsForPsu(psuIdData, DEFAULT_SERVICE_INSTANCE_ID, 0, 20);

        // Then
        assertFalse(actual.getData().isEmpty());
        assertEquals(expected, actual.getData().get(0));
        verify(piisConsentEntitySpecification, times(1))
            .byPsuDataAndInstanceId(psuIdData, DEFAULT_SERVICE_INSTANCE_ID);
    }

    @Test
    void getConsentsForPsu_Failure_WrongPsuId() {
        // Given
        PsuIdData psuIdData = buildPsuIdData(PSU_ID_WRONG);

        when(piisConsentEntitySpecification.byPsuDataAndInstanceId(psuIdData, DEFAULT_SERVICE_INSTANCE_ID)).thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        //noinspection unchecked
        when(consentJpaRepository.findAll(any(Specification.class), eq(PageRequest.of(0, 20))))
            .thenReturn(new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 20), 1));

        // When
        PageData<List<CmsPiisConsent>> actual = cmsAspspPiisServiceInternal.getConsentsForPsu(psuIdData, DEFAULT_SERVICE_INSTANCE_ID, 0, 20);

        // Then
        assertTrue(actual.getData().isEmpty());
        verify(piisConsentEntitySpecification, times(1))
            .byPsuDataAndInstanceId(psuIdData, DEFAULT_SERVICE_INSTANCE_ID);
    }

    @Test
    void getConsentsForPsu_withEmptyPsuId_shouldReturnEmpty() {
        // Given
        PsuIdData emptyPsuIdData = buildPsuIdData(null);

        // When
        PageData<List<CmsPiisConsent>> actual = cmsAspspPiisServiceInternal.getConsentsForPsu(emptyPsuIdData, DEFAULT_SERVICE_INSTANCE_ID, 0, 20);

        // Then
        assertTrue(actual.getData().isEmpty());
        verify(consentJpaRepository, never()).findAll(any());
    }

    @Test
    void terminateConsent_Success() {
        // Given
        when(piisConsentEntitySpecification.byConsentIdAndInstanceId(CONSENT_EXTERNAL_ID, DEFAULT_SERVICE_INSTANCE_ID))
            .thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        //noinspection unchecked
        when(consentJpaRepository.findOne(any(Specification.class))).thenReturn(Optional.of(buildPiisConsentEntity()));
        ArgumentCaptor<ConsentEntity> argumentCaptor = ArgumentCaptor.forClass(ConsentEntity.class);
        ConsentEntity modified = buildPiisConsentEntity();
        modified.setLastActionDate(LocalDate.now());
        modified.setConsentStatus(ConsentStatus.TERMINATED_BY_ASPSP);
        when(piisConsentLazyMigrationService.migrateIfNeeded(any(ConsentEntity.class))).thenReturn(modified);

        // When
        boolean actual = cmsAspspPiisServiceInternal.terminateConsent(CONSENT_EXTERNAL_ID, DEFAULT_SERVICE_INSTANCE_ID);

        // Then
        assertTrue(actual);
        verify(piisConsentLazyMigrationService).migrateIfNeeded(argumentCaptor.capture());
        assertEquals(ConsentStatus.TERMINATED_BY_ASPSP, argumentCaptor.getValue().getConsentStatus());
        verify(piisConsentEntitySpecification, times(1))
            .byConsentIdAndInstanceId(CONSENT_EXTERNAL_ID, DEFAULT_SERVICE_INSTANCE_ID);
    }

    @Test
    void terminateConsent_Failure_WrongConsentId() {
        // Given

        // When
        boolean actual = cmsAspspPiisServiceInternal.terminateConsent(CONSENT_EXTERNAL_ID_WRONG, DEFAULT_SERVICE_INSTANCE_ID);

        // Then
        assertFalse(actual);
        verify(consentJpaRepository, never()).save(any(ConsentEntity.class));
        verify(piisConsentEntitySpecification, times(1))
            .byConsentIdAndInstanceId(CONSENT_EXTERNAL_ID_WRONG, DEFAULT_SERVICE_INSTANCE_ID);
    }

    private TppInfoEntity buildTppInfoEntity() {
        TppInfoEntity tppInfoEntity = new TppInfoEntity();
        tppInfoEntity.setAuthorisationNumber(TPP_AUTHORISATION_NUMBER);
        return tppInfoEntity;
    }

    private ConsentEntity buildConsent() {
        return buildConsent(CONSENT_INTERNAL_ID);
    }

    private ConsentEntity buildConsent(Long id) {
        ConsentEntity piisConsent = new ConsentEntity();
        piisConsent.setId(id);
        piisConsent.setAspspAccountAccesses(Collections.singletonList(buildAspspAccountAccess(buildAccountReference())));
        piisConsent.setExternalId(CONSENT_EXTERNAL_ID);
        piisConsent.setRequestDateTime(OffsetDateTime.now());
        piisConsent.setPsuDataList(Collections.singletonList(buildPsuData()));
        piisConsent.setConsentStatus(ConsentStatus.RECEIVED);
        piisConsent.setValidUntil(VALID_UNTIL_DATE);
        piisConsent.setData(DATA);
        return piisConsent;
    }

    private PsuData buildPsuData() {
        return new PsuData(PSU_ID, null, null, null, null);
    }

    private PsuIdData buildPsuIdData() {
        return buildPsuIdData(PSU_ID);
    }

    private PsuIdData buildPsuIdData(String psuId) {
        return new PsuIdData(psuId, null, null, null, null);
    }

    private AccountReference buildAccountReference() {
        return new AccountReference("aspspAccountId", "resourceId",
                                    "DE89370400440532013000",
                                    null,
                                    null,
                                    null,
                                    null,
                                    null,
                                    null);
    }

    private ConsentEntity buildPiisConsentEntity() {
        return buildPiisConsentEntity(ConsentStatus.RECEIVED);
    }

    private ConsentEntity buildPiisConsentEntity(ConsentStatus consentStatus) {
        ConsentEntity consentEntity = new ConsentEntity();
        consentEntity.setExternalId(CONSENT_EXTERNAL_ID);
        consentEntity.setCreationTimestamp(CREATION_TIMESTAMP);
        consentEntity.setConsentStatus(consentStatus);
        return consentEntity;
    }

    private CmsPiisConsent buildCmsPiisConsent() {
        CmsPiisConsent consent = new CmsPiisConsent();
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

    public AspspAccountAccess buildAspspAccountAccess(AccountReference accountReference) {
        return new AspspAccountAccess(accountReference.getId(), buildPiisConsentEntity(ConsentStatus.VALID), accountReference.getUsedAccountReferenceSelector().getAccountValue(),
                                      TypeAccess.ACCOUNT,
                                      accountReference.getUsedAccountReferenceSelector().getAccountReferenceType(),
                                      accountReference.getCurrency(),
                                      accountReference.getResourceId(),
                                      accountReference.getAspspAccountId());
    }
}
