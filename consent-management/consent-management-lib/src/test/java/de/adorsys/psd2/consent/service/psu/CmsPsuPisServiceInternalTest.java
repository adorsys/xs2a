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

package de.adorsys.psd2.consent.service.psu;

import de.adorsys.psd2.consent.api.CmsError;
import de.adorsys.psd2.consent.api.CmsResponse;
import de.adorsys.psd2.consent.api.pis.CmsBasePaymentResponse;
import de.adorsys.psd2.consent.api.pis.CmsCommonPayment;
import de.adorsys.psd2.consent.api.pis.CmsPaymentResponse;
import de.adorsys.psd2.consent.api.pis.CmsSinglePayment;
import de.adorsys.psd2.consent.api.service.PisCommonPaymentService;
import de.adorsys.psd2.consent.domain.*;
import de.adorsys.psd2.consent.domain.payment.PisCommonPaymentData;
import de.adorsys.psd2.consent.domain.payment.PisPaymentData;
import de.adorsys.psd2.consent.psu.api.CmsPsuAuthorisation;
import de.adorsys.psd2.consent.psu.api.pis.CmsPisPsuDataAuthorisation;
import de.adorsys.psd2.consent.repository.AuthorisationRepository;
import de.adorsys.psd2.consent.repository.PisCommonPaymentDataRepository;
import de.adorsys.psd2.consent.repository.PisPaymentDataRepository;
import de.adorsys.psd2.consent.repository.specification.AuthorisationSpecification;
import de.adorsys.psd2.consent.repository.specification.PisPaymentDataSpecification;
import de.adorsys.psd2.consent.service.CommonPaymentDataService;
import de.adorsys.psd2.consent.service.CorePaymentsConvertService;
import de.adorsys.psd2.consent.service.mapper.CmsPsuAuthorisationMapper;
import de.adorsys.psd2.consent.service.mapper.CmsPsuPisMapper;
import de.adorsys.psd2.consent.service.mapper.PsuDataMapper;
import de.adorsys.psd2.consent.service.psu.util.PageRequestBuilder;
import de.adorsys.psd2.consent.service.psu.util.PsuDataUpdater;
import de.adorsys.psd2.xs2a.core.authorisation.AuthorisationType;
import de.adorsys.psd2.xs2a.core.exception.AuthorisationIsExpiredException;
import de.adorsys.psd2.xs2a.core.exception.RedirectUrlIsExpiredException;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.AuthenticationDataHolder;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CmsPsuPisServiceInternalTest {
    public static final Integer DEFAULT_PAGE_INDEX = 0;
    public static final Integer DEFAULT_ITEMS_PER_PAGE = 20;
    private static final String WRONG_PAYMENT_ID = "wrong payment id";
    private static final String AUTHORISATION_ID = "authorisation id";
    private static final String WRONG_AUTHORISATION_ID = "wrong authorisation id";
    private static final String PAYMENT_PRODUCT = "sepa-credit-transfers";
    private static final String FINALISED_PAYMENT_ID = "finalised payment id";
    private static final String FINALISED_AUTHORISATION_ID = "finalised authorisation id";
    private static final String EXPIRED_AUTHORISATION_ID = "expired authorisation id";
    private final PsuIdData WRONG_PSU_ID_DATA = buildWrongPsuIdData();
    private final PsuIdData PSU_ID_DATA = buildPsuIdData();
    private static final String PAYMENT_ID = "payment id";
    private static final String DEFAULT_SERVICE_INSTANCE_ID = "UNDEFINED";
    private static final String METHOD_ID = "SMS";
    private static final String AUTHENTICATION_DATA = "123456";

    @InjectMocks
    private CmsPsuPisServiceInternal cmsPsuPisServiceInternal;

    @Mock
    private PisPaymentDataRepository pisPaymentDataRepository;
    @Mock
    private AuthorisationRepository authorisationRepository;
    @Mock
    private CmsPsuPisMapper cmsPsuPisMapper;
    @Mock
    private PisCommonPaymentService pisCommonPaymentService;
    @Mock
    private PsuDataMapper psuDataMapper;
    @Mock
    private CommonPaymentDataService commonPaymentDataService;
    @Mock
    private AuthorisationSpecification authorisationSpecification;
    @Mock
    private PisPaymentDataSpecification pisPaymentDataSpecification;
    @Mock
    private CorePaymentsConvertService corePaymentsConvertService;
    @Mock
    private PisCommonPaymentDataRepository pisCommonPaymentDataRepository;
    @Mock
    private CmsPsuService cmsPsuService;
    @Mock
    private CmsPsuAuthorisationMapper cmsPsuAuthorisationMapper;
    @Mock
    private PsuDataUpdater psuDataUpdater;
    @Mock
    private PageRequestBuilder pageRequestBuilder;

    private AuthenticationDataHolder authenticationDataHolder;
    private PsuData psuData;
    private PsuIdData psuIdData;
    private CmsBasePaymentResponse cmsPayment;

    @BeforeEach
    void setUp() {
        psuData = buildPsuData();
        psuIdData = buildPsuIdData();
        cmsPayment = buildCmsPayment();
        authenticationDataHolder = new AuthenticationDataHolder(METHOD_ID, AUTHENTICATION_DATA);
    }

    @Test
    void getAuthorisationByAuthorisationId_Success() {
        when(authorisationSpecification.byExternalIdAndInstanceId(AUTHORISATION_ID, DEFAULT_SERVICE_INSTANCE_ID))
            .thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        AuthorisationEntity authorisationEntity = buildPisAuthorisation();
        //noinspection unchecked
        when(authorisationRepository.findOne(any(Specification.class))).thenReturn(Optional.of(authorisationEntity));
        when(cmsPsuAuthorisationMapper.mapToCmsPsuAuthorisation(authorisationEntity)).thenReturn(buildCmsPsuAuthorisation());

        Optional<CmsPsuAuthorisation> cmsPsuAuthorisationOptional = cmsPsuPisServiceInternal.getAuthorisationByAuthorisationId(AUTHORISATION_ID, DEFAULT_SERVICE_INSTANCE_ID);

        assertTrue(cmsPsuAuthorisationOptional.isPresent());
        assertEquals(cmsPsuAuthorisationOptional.get(), buildCmsPsuAuthorisation());
    }

    @Test
    void getAuthorisationbyAuthorisationId_authorisationNotFound() {
        when(authorisationSpecification.byExternalIdAndInstanceId(AUTHORISATION_ID, DEFAULT_SERVICE_INSTANCE_ID))
            .thenReturn(((root, criteriaQuery, criteriaBuilder) -> null));
        //noinspection unchecked
        when(authorisationRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());

        Optional<CmsPsuAuthorisation> cmsPsuAuthorisationOptional = cmsPsuPisServiceInternal.getAuthorisationByAuthorisationId(AUTHORISATION_ID, DEFAULT_SERVICE_INSTANCE_ID);

        assertFalse(cmsPsuAuthorisationOptional.isPresent());
    }

    @Test
    void updatePsuInPayment_Success() throws AuthorisationIsExpiredException {
        // Given
        when(psuDataMapper.mapToPsuData(psuIdData, DEFAULT_SERVICE_INSTANCE_ID)).thenReturn(psuData);
        when(authorisationSpecification.byExternalIdAndInstanceId(AUTHORISATION_ID, DEFAULT_SERVICE_INSTANCE_ID))
            .thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        //noinspection unchecked
        when(authorisationRepository.findOne(any(Specification.class))).thenReturn(Optional.of(buildPisAuthorisation()));

        // When
        boolean actualResult = cmsPsuPisServiceInternal.updatePsuInPayment(PSU_ID_DATA, AUTHORISATION_ID, DEFAULT_SERVICE_INSTANCE_ID);

        // Then
        assertTrue(actualResult);
        verify(authorisationSpecification, times(1))
            .byExternalIdAndInstanceId(AUTHORISATION_ID, DEFAULT_SERVICE_INSTANCE_ID);
    }

    @Test
    void updatePsuInPayment_authorisationIsExpired() {
        // Given
        when(authorisationSpecification.byExternalIdAndInstanceId(AUTHORISATION_ID, DEFAULT_SERVICE_INSTANCE_ID))
            .thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        AuthorisationEntity pisAuthorization = buildPisAuthorisation();
        pisAuthorization.setAuthorisationExpirationTimestamp(OffsetDateTime.now().minusDays(1));
        //noinspection unchecked
        when(authorisationRepository.findOne(any(Specification.class))).thenReturn(Optional.of(pisAuthorization));

        // When
        assertThrows(
            AuthorisationIsExpiredException.class,
            () -> cmsPsuPisServiceInternal.updatePsuInPayment(PSU_ID_DATA, AUTHORISATION_ID, DEFAULT_SERVICE_INSTANCE_ID)
        );

        // Then
        verify(authorisationSpecification, times(1))
            .byExternalIdAndInstanceId(AUTHORISATION_ID, DEFAULT_SERVICE_INSTANCE_ID);
    }

    @Test
    void updatePsuInPayment_authorisationNotFound() throws AuthorisationIsExpiredException {
        when(authorisationSpecification.byExternalIdAndInstanceId(AUTHORISATION_ID, DEFAULT_SERVICE_INSTANCE_ID))
            .thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        //noinspection unchecked
        when(authorisationRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());

        boolean actualResult = cmsPsuPisServiceInternal.updatePsuInPayment(PSU_ID_DATA, AUTHORISATION_ID, DEFAULT_SERVICE_INSTANCE_ID);

        assertFalse(actualResult);
        verify(authorisationSpecification, times(1))
            .byExternalIdAndInstanceId(AUTHORISATION_ID, DEFAULT_SERVICE_INSTANCE_ID);
    }

    @Test
    void updatePsuInPayment_newPsuDataIsEmpty() throws AuthorisationIsExpiredException {
        // Given
        PsuIdData emptyPsuIdData = buildEmptyPsuIdData();
        when(psuDataMapper.mapToPsuData(emptyPsuIdData, DEFAULT_SERVICE_INSTANCE_ID)).thenReturn(buildEmptyPsuData());
        when(authorisationSpecification.byExternalIdAndInstanceId(AUTHORISATION_ID, DEFAULT_SERVICE_INSTANCE_ID))
            .thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        //noinspection unchecked
        when(authorisationRepository.findOne(any(Specification.class))).thenReturn(Optional.of(buildPisAuthorisation()));

        // When
        boolean actualResult = cmsPsuPisServiceInternal.updatePsuInPayment(emptyPsuIdData, AUTHORISATION_ID, DEFAULT_SERVICE_INSTANCE_ID);

        // Then
        assertFalse(actualResult);
        verify(authorisationSpecification, times(1))
            .byExternalIdAndInstanceId(AUTHORISATION_ID, DEFAULT_SERVICE_INSTANCE_ID);
    }

    @Test
    void updatePsuInPayment_authorisationHasNoPsuData_parentHasNotBeenFound() throws AuthorisationIsExpiredException {
        // Given
        when(psuDataMapper.mapToPsuData(psuIdData, DEFAULT_SERVICE_INSTANCE_ID)).thenReturn(psuData);
        when(authorisationSpecification.byExternalIdAndInstanceId(AUTHORISATION_ID, DEFAULT_SERVICE_INSTANCE_ID))
            .thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        AuthorisationEntity authorisationEntityWithoutPsuData = buildPisAuthorisation();
        authorisationEntityWithoutPsuData.setPsuData(null);
        authorisationEntityWithoutPsuData.setParentExternalId(WRONG_PAYMENT_ID);
        //noinspection unchecked
        when(authorisationRepository.findOne(any(Specification.class))).thenReturn(Optional.of(authorisationEntityWithoutPsuData));

        // When
        boolean actualResult = cmsPsuPisServiceInternal.updatePsuInPayment(PSU_ID_DATA, AUTHORISATION_ID, DEFAULT_SERVICE_INSTANCE_ID);

        // Then
        assertFalse(actualResult);
        verify(authorisationSpecification, times(1))
            .byExternalIdAndInstanceId(AUTHORISATION_ID, DEFAULT_SERVICE_INSTANCE_ID);
        verify(pisCommonPaymentDataRepository, times(1)).findByPaymentId(WRONG_PAYMENT_ID);
    }

    @Test
    void updatePsuInPayment_authorisationHasNoPsuData_newPsuDataIsNotInPsuDataListOfParent() throws AuthorisationIsExpiredException {
        // Given
        when(psuDataMapper.mapToPsuData(psuIdData, DEFAULT_SERVICE_INSTANCE_ID)).thenReturn(psuData);
        when(authorisationSpecification.byExternalIdAndInstanceId(AUTHORISATION_ID, DEFAULT_SERVICE_INSTANCE_ID))
            .thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        AuthorisationEntity authorisationEntityWithoutPsuData = buildPisAuthorisation();
        authorisationEntityWithoutPsuData.setPsuData(null);
        //noinspection unchecked
        when(authorisationRepository.findOne(any(Specification.class))).thenReturn(Optional.of(authorisationEntityWithoutPsuData));
        PisCommonPaymentData pisCommonPaymentDataWithoutPsuData = buildPisCommonPaymentData();
        pisCommonPaymentDataWithoutPsuData.setPsuDataList(Collections.emptyList());
        when(pisCommonPaymentDataRepository.findByPaymentId(PAYMENT_ID)).thenReturn(Optional.of(pisCommonPaymentDataWithoutPsuData));
        when(cmsPsuService.definePsuDataForAuthorisation(psuData, pisCommonPaymentDataWithoutPsuData.getPsuDataList())).thenReturn(Optional.empty());

        // When
        boolean actualResult = cmsPsuPisServiceInternal.updatePsuInPayment(PSU_ID_DATA, AUTHORISATION_ID, DEFAULT_SERVICE_INSTANCE_ID);

        // Then
        assertTrue(actualResult);
        verify(authorisationSpecification, times(1))
            .byExternalIdAndInstanceId(AUTHORISATION_ID, DEFAULT_SERVICE_INSTANCE_ID);
    }

    @Test
    void updatePsuInPayment_authorisationHasNoPsuData_newPsuDataIsInPsuDataListOfParent() throws AuthorisationIsExpiredException {
        // Given
        when(psuDataMapper.mapToPsuData(psuIdData, DEFAULT_SERVICE_INSTANCE_ID)).thenReturn(psuData);
        when(authorisationSpecification.byExternalIdAndInstanceId(AUTHORISATION_ID, DEFAULT_SERVICE_INSTANCE_ID))
            .thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        AuthorisationEntity authorisationEntityWithoutPsuData = buildPisAuthorisation();
        authorisationEntityWithoutPsuData.setPsuData(null);
        //noinspection unchecked
        when(authorisationRepository.findOne(any(Specification.class))).thenReturn(Optional.of(authorisationEntityWithoutPsuData));
        PisCommonPaymentData pisCommonPaymentDataWithPsuData = buildPisCommonPaymentData();
        when(pisCommonPaymentDataRepository.findByPaymentId(PAYMENT_ID)).thenReturn(Optional.of(pisCommonPaymentDataWithPsuData));
        when(cmsPsuService.definePsuDataForAuthorisation(psuData, pisCommonPaymentDataWithPsuData.getPsuDataList())).thenReturn(Optional.of(psuData));

        // When
        boolean actualResult = cmsPsuPisServiceInternal.updatePsuInPayment(PSU_ID_DATA, AUTHORISATION_ID, DEFAULT_SERVICE_INSTANCE_ID);

        // Then
        assertTrue(actualResult);
        verify(authorisationSpecification, times(1))
            .byExternalIdAndInstanceId(AUTHORISATION_ID, DEFAULT_SERVICE_INSTANCE_ID);
    }

    @Test
    void updatePsuInPayment_Fail_WrongPaymentId() throws AuthorisationIsExpiredException {
        // Given

        // When
        boolean actualResult = cmsPsuPisServiceInternal.updatePsuInPayment(PSU_ID_DATA, WRONG_AUTHORISATION_ID, DEFAULT_SERVICE_INSTANCE_ID);

        // Then
        assertFalse(actualResult);
        verify(authorisationSpecification, times(1))
            .byExternalIdAndInstanceId(WRONG_AUTHORISATION_ID, DEFAULT_SERVICE_INSTANCE_ID);
    }

    @Test
    void getPayment_Success() {
        // Given
        when(cmsPsuPisMapper.mapToCmsPayment(buildPisPaymentDataList())).thenReturn(cmsPayment);
        when(pisCommonPaymentService.getPsuDataListByPaymentId(PAYMENT_ID))
            .thenReturn(CmsResponse.<List<PsuIdData>>builder()
                            .payload(Collections.singletonList(psuIdData))
                            .build());
        when(pisPaymentDataSpecification.byPaymentIdAndInstanceId(PAYMENT_ID, DEFAULT_SERVICE_INSTANCE_ID))
            .thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        //noinspection unchecked
        when(pisPaymentDataRepository.findAll(any(Specification.class))).thenReturn(buildPisPaymentDataList());

        // When
        Optional<CmsBasePaymentResponse> actualResult = cmsPsuPisServiceInternal.getPayment(PSU_ID_DATA, PAYMENT_ID, DEFAULT_SERVICE_INSTANCE_ID);

        // Then
        assertTrue(actualResult.isPresent());
        assertEquals(PAYMENT_ID, actualResult.get().getPaymentId());
        verify(pisPaymentDataSpecification, times(1))
            .byPaymentIdAndInstanceId(PAYMENT_ID, DEFAULT_SERVICE_INSTANCE_ID);
        verify(commonPaymentDataService, never()).getPisCommonPaymentData(any(), any());
    }

    @Test
    void getPayment_emptyList_Success() {
        // Given
        when(pisCommonPaymentService.getPsuDataListByPaymentId(PAYMENT_ID))
            .thenReturn(CmsResponse.<List<PsuIdData>>builder()
                            .payload(Collections.singletonList(psuIdData))
                            .build());
        when(pisPaymentDataSpecification.byPaymentIdAndInstanceId(PAYMENT_ID, DEFAULT_SERVICE_INSTANCE_ID))
            .thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        //noinspection unchecked
        when(pisPaymentDataRepository.findAll(any(Specification.class))).thenReturn(Collections.emptyList());
        PisCommonPaymentData pisCommonPaymentData = new PisCommonPaymentData();
        when(commonPaymentDataService.getPisCommonPaymentData(PAYMENT_ID, DEFAULT_SERVICE_INSTANCE_ID)).thenReturn(Optional.of(pisCommonPaymentData));

        CmsCommonPayment cmsPayment = new CmsCommonPayment(PAYMENT_PRODUCT);
        cmsPayment.setPaymentId(PAYMENT_ID);

        when(cmsPsuPisMapper.mapToCmsPayment(pisCommonPaymentData)).thenReturn(cmsPayment);
        when(corePaymentsConvertService.expandCommonPaymentWithCorePayment(cmsPayment)).thenReturn(cmsPayment);

        // When
        Optional<CmsBasePaymentResponse> actualResult = cmsPsuPisServiceInternal.getPayment(PSU_ID_DATA, PAYMENT_ID, DEFAULT_SERVICE_INSTANCE_ID);

        // Then
        assertTrue(actualResult.isPresent());
        assertEquals(PAYMENT_ID, actualResult.get().getPaymentId());
        verify(pisPaymentDataSpecification, times(1))
            .byPaymentIdAndInstanceId(PAYMENT_ID, DEFAULT_SERVICE_INSTANCE_ID);
        verify(commonPaymentDataService, times(1))
            .getPisCommonPaymentData(PAYMENT_ID, DEFAULT_SERVICE_INSTANCE_ID);
    }

    @Test
    void getPayment_Fail_WrongPaymentId() {
        // Given
        when(pisCommonPaymentService.getPsuDataListByPaymentId(WRONG_PAYMENT_ID))
            .thenReturn(CmsResponse.<List<PsuIdData>>builder()
                            .error(CmsError.LOGICAL_ERROR)
                            .build());
        // When
        Optional<CmsBasePaymentResponse> actualResult = cmsPsuPisServiceInternal.getPayment(PSU_ID_DATA, WRONG_PAYMENT_ID, DEFAULT_SERVICE_INSTANCE_ID);

        // Then
        assertFalse(actualResult.isPresent());
        verify(pisCommonPaymentService, times(1))
            .getPsuDataListByPaymentId(WRONG_PAYMENT_ID);
        verify(commonPaymentDataService, never()).getPisCommonPaymentData(any(), any());
    }

    @Test
    void getPayment_Fail_WrongPsuIdData() {
        // Given
        when(pisCommonPaymentService.getPsuDataListByPaymentId(PAYMENT_ID))
            .thenReturn(CmsResponse.<List<PsuIdData>>builder()
                            .payload(Collections.singletonList(psuIdData))
                            .build());
        // When
        Optional<CmsBasePaymentResponse> actualResult = cmsPsuPisServiceInternal.getPayment(WRONG_PSU_ID_DATA, PAYMENT_ID, DEFAULT_SERVICE_INSTANCE_ID);

        // Then
        assertFalse(actualResult.isPresent());
        verify(pisPaymentDataSpecification, never())
            .byPaymentIdAndInstanceId(PAYMENT_ID, DEFAULT_SERVICE_INSTANCE_ID);
        verify(commonPaymentDataService, never()).getPisCommonPaymentData(any(), any());
    }

    @Test
    void updateAuthorisationStatus_Success() throws AuthorisationIsExpiredException {
        // Given
        when(pisCommonPaymentService.getPsuDataListByPaymentId(PAYMENT_ID))
            .thenReturn(CmsResponse.<List<PsuIdData>>builder()
                            .payload(Collections.singletonList(psuIdData))
                            .build());
        when(authorisationSpecification.byExternalIdAndInstanceId(AUTHORISATION_ID, DEFAULT_SERVICE_INSTANCE_ID))
            .thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        AuthorisationEntity pisAuthorization = buildPisAuthorisation();
        //noinspection unchecked
        when(authorisationRepository.findOne(any(Specification.class))).thenReturn(Optional.of(pisAuthorization));

        // When
        boolean actualResult = cmsPsuPisServiceInternal.updateAuthorisationStatus(PSU_ID_DATA, PAYMENT_ID, AUTHORISATION_ID, ScaStatus.FAILED, DEFAULT_SERVICE_INSTANCE_ID,
                                                                                  authenticationDataHolder);

        // Then
        assertTrue(actualResult);
        verify(authorisationSpecification, times(1))
            .byExternalIdAndInstanceId(AUTHORISATION_ID, DEFAULT_SERVICE_INSTANCE_ID);
    }

    @Test
    void updateAuthorisationStatus_scaStatusOfAuthorisation_isFinalised_shouldReturnError() throws AuthorisationIsExpiredException {
        // Given
        when(pisCommonPaymentService.getPsuDataListByPaymentId(PAYMENT_ID))
            .thenReturn(CmsResponse.<List<PsuIdData>>builder()
                            .payload(Collections.singletonList(psuIdData))
                            .build());
        when(authorisationSpecification.byExternalIdAndInstanceId(AUTHORISATION_ID, DEFAULT_SERVICE_INSTANCE_ID))
            .thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        AuthorisationEntity pisAuthorization = buildPisAuthorisation();
        pisAuthorization.setScaStatus(ScaStatus.FAILED);
        //noinspection unchecked
        when(authorisationRepository.findOne(any(Specification.class))).thenReturn(Optional.of(pisAuthorization));

        // When
        boolean actualResult = cmsPsuPisServiceInternal.updateAuthorisationStatus(PSU_ID_DATA, PAYMENT_ID, AUTHORISATION_ID, ScaStatus.FAILED, DEFAULT_SERVICE_INSTANCE_ID,
                                                                                  authenticationDataHolder);

        // Then
        assertFalse(actualResult);
        verify(authorisationSpecification, times(1))
            .byExternalIdAndInstanceId(AUTHORISATION_ID, DEFAULT_SERVICE_INSTANCE_ID);
    }

    @Test
    void updateAuthorisationStatus_Fail_InvalidRequestData() throws AuthorisationIsExpiredException {
        // Given
        when(pisCommonPaymentService.getPsuDataListByPaymentId(WRONG_PAYMENT_ID))
            .thenReturn(CmsResponse.<List<PsuIdData>>builder()
                            .error(CmsError.LOGICAL_ERROR)
                            .build());
        when(authorisationSpecification.byExternalIdAndInstanceId(AUTHORISATION_ID, DEFAULT_SERVICE_INSTANCE_ID))
            .thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        AuthorisationEntity pisAuthorization = buildPisAuthorisation();
        //noinspection unchecked
        when(authorisationRepository.findOne(any(Specification.class))).thenReturn(Optional.of(pisAuthorization));

        // When
        boolean actualResult = cmsPsuPisServiceInternal.updateAuthorisationStatus(PSU_ID_DATA, WRONG_PAYMENT_ID, AUTHORISATION_ID, ScaStatus.FAILED, DEFAULT_SERVICE_INSTANCE_ID,
                                                                                  authenticationDataHolder);

        // Then
        assertFalse(actualResult);
        verify(authorisationSpecification, times(1))
            .byExternalIdAndInstanceId(AUTHORISATION_ID, DEFAULT_SERVICE_INSTANCE_ID);
    }

    @Test
    void updateAuthorisationStatus_WrongPaymentId() throws AuthorisationIsExpiredException {
        // Given

        // When
        boolean actualResult = cmsPsuPisServiceInternal.updateAuthorisationStatus(PSU_ID_DATA, WRONG_PAYMENT_ID, AUTHORISATION_ID, ScaStatus.FAILED, DEFAULT_SERVICE_INSTANCE_ID,
                                                                                  authenticationDataHolder);

        // Then
        assertFalse(actualResult);
        verify(authorisationSpecification, times(1))
            .byExternalIdAndInstanceId(AUTHORISATION_ID, DEFAULT_SERVICE_INSTANCE_ID);
    }

    @Test
    void updateAuthorisationStatus_WrongPsuIdData() throws AuthorisationIsExpiredException {
        // Given

        // When
        boolean actualResult = cmsPsuPisServiceInternal.updateAuthorisationStatus(WRONG_PSU_ID_DATA, PAYMENT_ID, AUTHORISATION_ID, ScaStatus.FAILED, DEFAULT_SERVICE_INSTANCE_ID,
                                                                                  authenticationDataHolder);

        // Then
        assertFalse(actualResult);
        verify(authorisationSpecification, times(1))
            .byExternalIdAndInstanceId(AUTHORISATION_ID, DEFAULT_SERVICE_INSTANCE_ID);
    }

    @Test
    void updateAuthorisationStatus_WrongAuthorisationId() throws AuthorisationIsExpiredException {
        // Given

        // When
        boolean actualResult = cmsPsuPisServiceInternal.updateAuthorisationStatus(PSU_ID_DATA, PAYMENT_ID, WRONG_AUTHORISATION_ID, ScaStatus.FAILED, DEFAULT_SERVICE_INSTANCE_ID,
                                                                                  authenticationDataHolder);

        // Then
        assertFalse(actualResult);
        verify(authorisationSpecification, times(1))
            .byExternalIdAndInstanceId(WRONG_AUTHORISATION_ID, DEFAULT_SERVICE_INSTANCE_ID);
    }

    @Test
    void updatePaymentStatus_Success() {
        // Given
        when(commonPaymentDataService.getPisCommonPaymentData(PAYMENT_ID, DEFAULT_SERVICE_INSTANCE_ID))
            .thenReturn(Optional.of(buildPisCommonPaymentData()));
        when(commonPaymentDataService.updateStatusInPaymentData(buildPisCommonPaymentData(), TransactionStatus.RCVD)).thenReturn(true);

        // When
        boolean actualResult = cmsPsuPisServiceInternal.updatePaymentStatus(PAYMENT_ID, TransactionStatus.RCVD, DEFAULT_SERVICE_INSTANCE_ID);

        // Then
        assertTrue(actualResult);
    }

    @Test
    void updatePaymentStatus_Fail_WrongPaymentId() {
        // Given
        when(commonPaymentDataService.getPisCommonPaymentData(WRONG_PAYMENT_ID, DEFAULT_SERVICE_INSTANCE_ID))
            .thenReturn(Optional.empty());

        // When
        boolean actualResult = cmsPsuPisServiceInternal.updatePaymentStatus(WRONG_PAYMENT_ID, TransactionStatus.CANC, DEFAULT_SERVICE_INSTANCE_ID);

        // Then
        assertFalse(actualResult);
    }

    @Test
    void getPsuDataAuthorisations_Success() {
        // Given
        when(commonPaymentDataService.getPisCommonPaymentData(PAYMENT_ID, DEFAULT_SERVICE_INSTANCE_ID))
            .thenReturn(Optional.of(buildPisCommonPaymentData()));
        when(authorisationRepository.findAllByParentExternalIdAndTypeIn(PAYMENT_ID, EnumSet.of(AuthorisationType.PIS_CREATION, AuthorisationType.PIS_CANCELLATION)))
            .thenReturn(Collections.singletonList(buildPisAuthorisation()));

        // When
        Optional<List<CmsPisPsuDataAuthorisation>> actualResult = cmsPsuPisServiceInternal.getPsuDataAuthorisations(PAYMENT_ID, DEFAULT_SERVICE_INSTANCE_ID, null, null);

        // Then
        assertTrue(actualResult.isPresent());
        assertEquals(1, actualResult.get().size());
    }

    @Test
    void getPsuDataAuthorisations_SuccessPagination() {
        // Given
        when(commonPaymentDataService.getPisCommonPaymentData(PAYMENT_ID, DEFAULT_SERVICE_INSTANCE_ID))
            .thenReturn(Optional.of(buildPisCommonPaymentData()));

        PageRequest pageRequest = PageRequest.of(DEFAULT_PAGE_INDEX, DEFAULT_ITEMS_PER_PAGE);
        when(pageRequestBuilder.getPageable(DEFAULT_PAGE_INDEX, DEFAULT_ITEMS_PER_PAGE)).thenReturn(pageRequest);
        when(authorisationRepository.findAllByParentExternalIdAndTypeIn(PAYMENT_ID, EnumSet.of(AuthorisationType.PIS_CREATION, AuthorisationType.PIS_CANCELLATION), pageRequest))
            .thenReturn(Collections.singletonList(buildPisAuthorisation()));

        // When
        Optional<List<CmsPisPsuDataAuthorisation>> actualResult = cmsPsuPisServiceInternal.getPsuDataAuthorisations(PAYMENT_ID, DEFAULT_SERVICE_INSTANCE_ID, DEFAULT_PAGE_INDEX, DEFAULT_ITEMS_PER_PAGE);

        // Then
        assertTrue(actualResult.isPresent());
        assertEquals(1, actualResult.get().size());
    }

    @Test
    void getPsuDataAuthorisationsEmptyPsuData_Success() {
        // Given
        when(commonPaymentDataService.getPisCommonPaymentData(PAYMENT_ID, DEFAULT_SERVICE_INSTANCE_ID))
            .thenReturn(Optional.of(buildPisCommonPaymentDataWithAuthorisationEmptyPsuData()));

        // When
        Optional<List<CmsPisPsuDataAuthorisation>> actualResult = cmsPsuPisServiceInternal.getPsuDataAuthorisations(PAYMENT_ID, DEFAULT_SERVICE_INSTANCE_ID, DEFAULT_PAGE_INDEX, DEFAULT_ITEMS_PER_PAGE);

        // Then
        assertTrue(actualResult.isPresent());
        assertTrue(actualResult.get().isEmpty());
    }

    @Test
    void updateAuthorisationStatus_Fail_FinalisedStatus() throws AuthorisationIsExpiredException {
        // Given

        // When
        boolean actualResult = cmsPsuPisServiceInternal.updateAuthorisationStatus(PSU_ID_DATA, PAYMENT_ID, FINALISED_AUTHORISATION_ID, ScaStatus.SCAMETHODSELECTED, DEFAULT_SERVICE_INSTANCE_ID, authenticationDataHolder);

        // Then
        assertFalse(actualResult);
        verify(authorisationSpecification, times(1))
            .byExternalIdAndInstanceId(FINALISED_AUTHORISATION_ID, DEFAULT_SERVICE_INSTANCE_ID);
    }

    @Test
    void updatePaymentStatus_Fail_FinalisedStatus() {
        // Given
        when(commonPaymentDataService.getPisCommonPaymentData(FINALISED_PAYMENT_ID, DEFAULT_SERVICE_INSTANCE_ID))
            .thenReturn(Optional.of(buildFinalisedPisCommonPaymentData()));

        // When
        boolean actualResult = cmsPsuPisServiceInternal.updatePaymentStatus(FINALISED_PAYMENT_ID, TransactionStatus.CANC, DEFAULT_SERVICE_INSTANCE_ID);

        // Then
        assertFalse(actualResult);
    }

    @Test
    void getPaymentByAuthorisationId_Success() throws RedirectUrlIsExpiredException {
        // Given
        when(authorisationSpecification.byExternalIdAndInstanceId(AUTHORISATION_ID, DEFAULT_SERVICE_INSTANCE_ID))
            .thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        AuthorisationEntity expectedAuthorisation = buildPisAuthorisation();
        //noinspection unchecked
        when(authorisationRepository.findOne(any(Specification.class))).thenReturn(Optional.of(expectedAuthorisation));

        when(pisCommonPaymentDataRepository.findByPaymentId(PAYMENT_ID)).thenReturn(Optional.of(buildPisCommonPaymentData()));

        // When
        Optional<CmsPaymentResponse> actualResult = cmsPsuPisServiceInternal.checkRedirectAndGetPayment(AUTHORISATION_ID, DEFAULT_SERVICE_INSTANCE_ID);

        // Then
        assertTrue(actualResult.isPresent());
        assertEquals(AUTHORISATION_ID, actualResult.get().getAuthorisationId());
        verify(authorisationSpecification, times(1))
            .byExternalIdAndInstanceId(AUTHORISATION_ID, DEFAULT_SERVICE_INSTANCE_ID);
    }

    @Test
    void getPaymentByAuthorisationId_emptyPayment() throws RedirectUrlIsExpiredException {
        // Given
        when(authorisationSpecification.byExternalIdAndInstanceId(AUTHORISATION_ID, DEFAULT_SERVICE_INSTANCE_ID))
            .thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        AuthorisationEntity expectedAuthorisation = buildPisAuthorisation();
        //noinspection unchecked
        when(authorisationRepository.findOne(any(Specification.class))).thenReturn(Optional.of(expectedAuthorisation));

        when(pisCommonPaymentDataRepository.findByPaymentId(PAYMENT_ID)).thenReturn(Optional.empty());

        // When
        Optional<CmsPaymentResponse> actualResult = cmsPsuPisServiceInternal.checkRedirectAndGetPayment(AUTHORISATION_ID, DEFAULT_SERVICE_INSTANCE_ID);

        // Then
        assertFalse(actualResult.isPresent());
        verify(authorisationSpecification, times(1))
            .byExternalIdAndInstanceId(AUTHORISATION_ID, DEFAULT_SERVICE_INSTANCE_ID);
    }

    @Test
    void getPaymentByAuthorisationId_Fail_ExpiredRedirectUrl() {
        // Given
        when(authorisationSpecification.byExternalIdAndInstanceId(EXPIRED_AUTHORISATION_ID, DEFAULT_SERVICE_INSTANCE_ID))
            .thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        AuthorisationEntity expectedAuthorisation = buildExpiredAuthorisation();
        //noinspection unchecked
        when(authorisationRepository.findOne(any(Specification.class))).thenReturn(Optional.of(expectedAuthorisation));

        // When
        assertThrows(
            RedirectUrlIsExpiredException.class,
            () -> cmsPsuPisServiceInternal.checkRedirectAndGetPayment(EXPIRED_AUTHORISATION_ID, DEFAULT_SERVICE_INSTANCE_ID)
        );

        // Then
        verify(authorisationSpecification, times(1))
            .byExternalIdAndInstanceId(EXPIRED_AUTHORISATION_ID, DEFAULT_SERVICE_INSTANCE_ID);
    }

    @Test
    void getPaymentByAuthorisationId_Fail_WrongId() throws RedirectUrlIsExpiredException {
        // When
        Optional<CmsPaymentResponse> actualResult = cmsPsuPisServiceInternal.checkRedirectAndGetPayment(WRONG_AUTHORISATION_ID, DEFAULT_SERVICE_INSTANCE_ID);

        // Then
        assertEquals(Optional.empty(), actualResult);
        verify(authorisationSpecification, times(1))
            .byExternalIdAndInstanceId(WRONG_AUTHORISATION_ID, DEFAULT_SERVICE_INSTANCE_ID);
    }

    @Test
    void checkRedirectAndGetPaymentForCancellation_Success() throws RedirectUrlIsExpiredException {
        // Given
        when(authorisationSpecification.byExternalIdAndInstanceId(AUTHORISATION_ID, DEFAULT_SERVICE_INSTANCE_ID))
            .thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        AuthorisationEntity expectedAuthorisation = buildPisAuthorisation();
        //noinspection unchecked
        when(authorisationRepository.findOne(any(Specification.class))).thenReturn(Optional.of(expectedAuthorisation));

        when(pisCommonPaymentDataRepository.findByPaymentId(PAYMENT_ID)).thenReturn(Optional.of(buildPisCommonPaymentData()));

        // When
        Optional<CmsPaymentResponse> actualResult = cmsPsuPisServiceInternal.checkRedirectAndGetPaymentForCancellation(AUTHORISATION_ID, DEFAULT_SERVICE_INSTANCE_ID);

        // Then
        assertTrue(actualResult.isPresent());
        assertEquals(AUTHORISATION_ID, actualResult.get().getAuthorisationId());
        verify(authorisationSpecification, times(1))
            .byExternalIdAndInstanceId(AUTHORISATION_ID, DEFAULT_SERVICE_INSTANCE_ID);
    }

    @Test
    void checkRedirectAndGetPaymentForCancellation_emptyPayment() throws RedirectUrlIsExpiredException {
        // Given
        when(authorisationSpecification.byExternalIdAndInstanceId(AUTHORISATION_ID, DEFAULT_SERVICE_INSTANCE_ID))
            .thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        AuthorisationEntity expectedAuthorisation = buildPisAuthorisation();
        //noinspection unchecked
        when(authorisationRepository.findOne(any(Specification.class))).thenReturn(Optional.of(expectedAuthorisation));

        when(pisCommonPaymentDataRepository.findByPaymentId(PAYMENT_ID)).thenReturn(Optional.empty());

        // When
        Optional<CmsPaymentResponse> actualResult = cmsPsuPisServiceInternal.checkRedirectAndGetPaymentForCancellation(AUTHORISATION_ID, DEFAULT_SERVICE_INSTANCE_ID);

        // Then
        assertFalse(actualResult.isPresent());
        verify(authorisationSpecification, times(1))
            .byExternalIdAndInstanceId(AUTHORISATION_ID, DEFAULT_SERVICE_INSTANCE_ID);
    }

    @Test
    void checkRedirectAndGetPaymentForCancellation_Fail_ExpiredRedirectUrl() {
        // Given
        when(authorisationSpecification.byExternalIdAndInstanceId(EXPIRED_AUTHORISATION_ID, DEFAULT_SERVICE_INSTANCE_ID))
            .thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        AuthorisationEntity expectedAuthorisation = buildExpiredAuthorisation();
        //noinspection unchecked
        when(authorisationRepository.findOne(any(Specification.class))).thenReturn(Optional.of(expectedAuthorisation));

        // When
        assertThrows(
            RedirectUrlIsExpiredException.class,
            () -> cmsPsuPisServiceInternal.checkRedirectAndGetPaymentForCancellation(EXPIRED_AUTHORISATION_ID, DEFAULT_SERVICE_INSTANCE_ID)
        );

        // Then
        verify(authorisationSpecification, times(1))
            .byExternalIdAndInstanceId(EXPIRED_AUTHORISATION_ID, DEFAULT_SERVICE_INSTANCE_ID);
    }

    @Test
    void checkRedirectAndGetPaymentForCancellation_Fail_WrongId() throws RedirectUrlIsExpiredException {
        // When
        Optional<CmsPaymentResponse> actualResult = cmsPsuPisServiceInternal.checkRedirectAndGetPaymentForCancellation(WRONG_AUTHORISATION_ID, DEFAULT_SERVICE_INSTANCE_ID);

        // Then
        assertEquals(Optional.empty(), actualResult);
        verify(authorisationSpecification, times(1))
            .byExternalIdAndInstanceId(WRONG_AUTHORISATION_ID, DEFAULT_SERVICE_INSTANCE_ID);
    }

    private PsuIdData buildPsuIdData() {
        return new PsuIdData(
            "psuId",
            "psuIdType",
            "psuCorporateId",
            "psuCorporateIdType",
            "psuIpAddress"
        );
    }

    private PsuIdData buildEmptyPsuIdData() {
        return new PsuIdData(
            null,
            null,
            null,
            null,
            null
        );
    }

    private PsuIdData buildWrongPsuIdData() {
        return new PsuIdData(
            "wrong psuId",
            "psuIdType",
            "wrong psuCorporateId",
            "psuCorporateIdType",
            "psuIpAddress"
        );
    }

    private CmsPsuAuthorisation buildCmsPsuAuthorisation() {
        CmsPsuAuthorisation cmsPsuAuthorisation = new CmsPsuAuthorisation();
        cmsPsuAuthorisation.setScaStatus(ScaStatus.FAILED);
        return cmsPsuAuthorisation;
    }

    private AuthorisationEntity buildPisAuthorisation() {
        AuthorisationEntity pisAuthorisation = new AuthorisationEntity();
        pisAuthorisation.setType(AuthorisationType.PIS_CREATION);
        pisAuthorisation.setScaStatus(ScaStatus.PSUAUTHENTICATED);
        pisAuthorisation.setParentExternalId(PAYMENT_ID);
        pisAuthorisation.setExternalId(AUTHORISATION_ID);
        pisAuthorisation.setPsuData(buildPsuData());
        pisAuthorisation.setRedirectUrlExpirationTimestamp(OffsetDateTime.now().plusHours(1));
        pisAuthorisation.setAuthorisationExpirationTimestamp(OffsetDateTime.now().plusHours(1));
        pisAuthorisation.setAuthenticationMethodId(METHOD_ID);
        pisAuthorisation.setScaAuthenticationData(AUTHENTICATION_DATA);

        return pisAuthorisation;
    }

    private PisCommonPaymentData buildPisCommonPaymentData() {
        PisCommonPaymentData pisCommonPaymentData = new PisCommonPaymentData();
        pisCommonPaymentData.setTransactionStatus(TransactionStatus.RCVD);
        pisCommonPaymentData.setPsuDataList(Collections.singletonList(buildPsuData()));
        pisCommonPaymentData.setPaymentType(PaymentType.SINGLE);
        pisCommonPaymentData.setPaymentProduct(PAYMENT_PRODUCT);
        pisCommonPaymentData.setPayments(buildPisPaymentDataListForCommonData());
        pisCommonPaymentData.setTppInfo(new TppInfoEntity());
        pisCommonPaymentData.setAuthorisationTemplate(buildAuthorisationTemplate());
        pisCommonPaymentData.setPaymentId(PAYMENT_ID);
        pisCommonPaymentData.setCreationTimestamp(OffsetDateTime.of(2018, 10, 10, 10, 10, 10, 10, ZoneOffset.UTC));
        return pisCommonPaymentData;
    }

    private PisCommonPaymentData buildPisCommonPaymentDataWithAuthorisationEmptyPsuData() {
        PisCommonPaymentData pisCommonPaymentData = new PisCommonPaymentData();
        pisCommonPaymentData.setTransactionStatus(TransactionStatus.RCVD);
        pisCommonPaymentData.setPaymentType(PaymentType.SINGLE);
        pisCommonPaymentData.setPaymentProduct(PAYMENT_PRODUCT);
        pisCommonPaymentData.setPayments(buildPisPaymentDataListForCommonData());
        pisCommonPaymentData.setTppInfo(new TppInfoEntity());
        pisCommonPaymentData.setAuthorisationTemplate(buildAuthorisationTemplate());
        pisCommonPaymentData.setPaymentId(PAYMENT_ID);
        pisCommonPaymentData.setCreationTimestamp(OffsetDateTime.of(2018, 10, 10, 10, 10, 10, 10, ZoneOffset.UTC));
        return pisCommonPaymentData;
    }

    private PisCommonPaymentData buildFinalisedPisCommonPaymentData() {
        PisCommonPaymentData pisCommonPaymentData = new PisCommonPaymentData();
        pisCommonPaymentData.setTransactionStatus(TransactionStatus.RJCT);
        pisCommonPaymentData.setPsuDataList(Collections.singletonList(buildPsuData()));
        pisCommonPaymentData.setPaymentType(PaymentType.SINGLE);
        pisCommonPaymentData.setPaymentProduct(PAYMENT_PRODUCT);
        pisCommonPaymentData.setPayments(buildPisPaymentDataListForCommonData());
        pisCommonPaymentData.setTppInfo(new TppInfoEntity());
        pisCommonPaymentData.setAuthorisationTemplate(buildAuthorisationTemplate());
        pisCommonPaymentData.setPaymentId(PAYMENT_ID);
        pisCommonPaymentData.setCreationTimestamp(OffsetDateTime.of(2018, 10, 10, 10, 10, 10, 10, ZoneOffset.UTC));
        return pisCommonPaymentData;
    }

    private AuthorisationTemplateEntity buildAuthorisationTemplate() {
        AuthorisationTemplateEntity entity = new AuthorisationTemplateEntity();
        entity.setNokRedirectUri("tpp nok redirect uri");
        entity.setRedirectUri("tpp ok redirect uri");

        return entity;
    }

    private PsuData buildPsuData() {
        PsuIdData psuIdData = buildPsuIdData();
        PsuData psuData = new PsuData(
            psuIdData.getPsuId(),
            psuIdData.getPsuIdType(),
            psuIdData.getPsuCorporateId(),
            psuIdData.getPsuCorporateIdType(),
            psuIdData.getPsuIpAddress()
        );
        psuData.setId(1L);

        return psuData;
    }

    private PsuData buildEmptyPsuData() {
        PsuIdData emptyPsuIdData = buildEmptyPsuIdData();
        PsuData emptyPsuData = new PsuData(
            emptyPsuIdData.getPsuId(),
            emptyPsuIdData.getPsuIdType(),
            emptyPsuIdData.getPsuCorporateId(),
            emptyPsuIdData.getPsuCorporateIdType(),
            emptyPsuIdData.getPsuIpAddress()
        );
        emptyPsuData.setId(1L);

        return emptyPsuData;
    }

    private List<PisPaymentData> buildPisPaymentDataList() {
        PisPaymentData pisPaymentData = new PisPaymentData();
        pisPaymentData.setPaymentId(PAYMENT_ID);
        pisPaymentData.setPaymentData(buildPisCommonPaymentData());
        pisPaymentData.setDebtorAccount(buildAccountReference());
        pisPaymentData.setCreditorAccount(buildAccountReference());
        pisPaymentData.setAmount(new BigDecimal("1000"));
        pisPaymentData.setCurrency(Currency.getInstance("EUR"));

        return Collections.singletonList(pisPaymentData);
    }

    private List<PisPaymentData> buildPisPaymentDataListForCommonData() {
        PisPaymentData pisPaymentData = new PisPaymentData();
        pisPaymentData.setPaymentId(PAYMENT_ID);
        pisPaymentData.setDebtorAccount(buildAccountReference());
        pisPaymentData.setCreditorAccount(buildAccountReference());
        pisPaymentData.setAmount(new BigDecimal("1000"));
        pisPaymentData.setCurrency(Currency.getInstance("EUR"));

        return Collections.singletonList(pisPaymentData);
    }

    private AccountReferenceEntity buildAccountReference() {
        AccountReferenceEntity pisAccountReference = new AccountReferenceEntity();
        pisAccountReference.setIban("iban");
        pisAccountReference.setCurrency(Currency.getInstance("EUR"));

        return pisAccountReference;
    }

    private CmsBasePaymentResponse buildCmsPayment() {
        CmsSinglePayment cmsPayment = new CmsSinglePayment(PAYMENT_PRODUCT);
        cmsPayment.setPaymentId(PAYMENT_ID);

        return cmsPayment;
    }

    private AuthorisationEntity buildExpiredAuthorisation() {
        AuthorisationEntity pisAuthorisation = new AuthorisationEntity();
        pisAuthorisation.setType(AuthorisationType.PIS_CREATION);
        pisAuthorisation.setScaStatus(ScaStatus.RECEIVED);
        pisAuthorisation.setExternalId(EXPIRED_AUTHORISATION_ID);
        pisAuthorisation.setPsuData(buildPsuData());
        pisAuthorisation.setRedirectUrlExpirationTimestamp(OffsetDateTime.now().minusDays(1));
        pisAuthorisation.setAuthorisationExpirationTimestamp(OffsetDateTime.now().plusHours(1));

        return pisAuthorisation;
    }
}
