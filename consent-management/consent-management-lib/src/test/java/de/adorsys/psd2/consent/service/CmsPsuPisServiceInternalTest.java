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


package de.adorsys.psd2.consent.service;

import de.adorsys.psd2.consent.api.CmsAuthorisationType;
import de.adorsys.psd2.consent.api.pis.CmsPayment;
import de.adorsys.psd2.consent.api.pis.CmsPaymentResponse;
import de.adorsys.psd2.consent.api.pis.CmsSinglePayment;
import de.adorsys.psd2.consent.api.service.PisCommonPaymentService;
import de.adorsys.psd2.consent.domain.AccountReferenceEntity;
import de.adorsys.psd2.consent.domain.PsuData;
import de.adorsys.psd2.consent.domain.TppInfoEntity;
import de.adorsys.psd2.consent.domain.payment.PisAuthorization;
import de.adorsys.psd2.consent.domain.payment.PisCommonPaymentData;
import de.adorsys.psd2.consent.domain.payment.PisPaymentData;
import de.adorsys.psd2.consent.repository.PisAuthorisationRepository;
import de.adorsys.psd2.consent.repository.PisCommonPaymentDataRepository;
import de.adorsys.psd2.consent.repository.PisPaymentDataRepository;
import de.adorsys.psd2.consent.repository.PsuDataRepository;
import de.adorsys.psd2.consent.repository.specification.PisAuthorisationSpecification;
import de.adorsys.psd2.consent.repository.specification.PisPaymentDataSpecification;
import de.adorsys.psd2.consent.service.mapper.CmsPsuPisMapper;
import de.adorsys.psd2.consent.service.mapper.PsuDataMapper;
import de.adorsys.psd2.consent.service.psu.CmsPsuPisServiceInternal;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.Currency;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CmsPsuPisServiceInternalTest {
    private static final String WRONG_PAYMENT_ID = "wrong payment id";
    private static final String AUTHORISATION_ID = "authorisation id";
    private static final String WRONG_AUTHORISATION_ID = "wrong authorisation id";
    private static final String PAYMENT_PRODUCT = "sepa-credit-transfers";
    private static final String FINALISED_PAYMENT_ID = "finalised payment id";
    private static final String FINALISED_AUTHORISATION_ID = "finalised authorisation id";
    private static final String EXPIRED_AUTHORISATION_ID = "expired authorisation id";
    private static final String TPP_OK_REDIRECT_URI = "tpp ok redirect uri";
    private static final String TPP_NOK_REDIRECT_URI = "tpp nok redirect uri";
    private final PsuIdData WRONG_PSU_ID_DATA = buildWrongPsuIdData();
    private final PsuIdData PSU_ID_DATA = buildPsuIdData();
    private static final String PAYMENT_ID = "payment id";
    private static final String DEFAULT_SERVICE_INSTANCE_ID = "UNDEFINED";

    @InjectMocks
    private CmsPsuPisServiceInternal cmsPsuPisServiceInternal;

    @Mock
    PisPaymentDataRepository pisPaymentDataRepository;
    @Mock
    PisCommonPaymentDataRepository pisCommonPaymentDataRepository;
    @Mock
    PisAuthorisationRepository pisAuthorisationRepository;
    @Mock
    CmsPsuPisMapper cmsPsuPisMapper;
    @Mock
    PisCommonPaymentService pisCommonPaymentService;
    @Mock
    PsuDataRepository psuDataRepository;
    @Mock
    PsuDataMapper psuDataMapper;
    @Mock
    CommonPaymentDataService commonPaymentDataService;
    @Mock
    private PisAuthorisationSpecification pisAuthorisationSpecification;
    @Mock
    private PisPaymentDataSpecification pisPaymentDataSpecification;

    @Before
    public void setUp() {
        List<PisPaymentData> pisPaymentDataList = buildPisPaymentDataList();
        PisAuthorization pisAuthorisation = buildPisAuthorisation();
        PsuData psuData = buildPsuData();
        PsuIdData psuIdData = buildPsuIdData();
        CmsPayment cmsPayment = buildCmsPayment();

        when(pisCommonPaymentDataRepository.save(buildPisCommonPaymentData()))
            .thenReturn(buildPisCommonPaymentData());
        when(pisPaymentDataRepository.save(any(PisPaymentData.class)))
            .thenReturn(pisPaymentDataList.get(0));
        when(pisAuthorisationRepository.save(any(PisAuthorization.class)))
            .thenReturn(pisAuthorisation);

        when(cmsPsuPisMapper.mapToCmsPayment(buildPisPaymentDataList()))
            .thenReturn(cmsPayment);

        when(pisCommonPaymentService.getPsuDataListByPaymentId(PAYMENT_ID))
            .thenReturn(Optional.of(Collections.singletonList(psuIdData)));
        when(pisCommonPaymentService.getPsuDataListByPaymentId(WRONG_PAYMENT_ID))
            .thenReturn(Optional.empty());

        when(commonPaymentDataService.getPisCommonPaymentData(WRONG_PAYMENT_ID, DEFAULT_SERVICE_INSTANCE_ID))
            .thenReturn(Optional.empty());

        when(psuDataRepository.save(any(PsuData.class)))
            .thenReturn(psuData);
        when(psuDataMapper.mapToPsuData(psuIdData))
            .thenReturn(psuData);
        when(psuDataMapper.mapToPsuIdData(any(PsuData.class)))
            .thenReturn(psuIdData);
    }

    @Test
    public void updatePsuInPayment_Success() {
        // Given
        //noinspection unchecked
        when(pisAuthorisationRepository.findOne(any(Specification.class))).thenReturn(buildPisAuthorisation());

        // When
        boolean actualResult = cmsPsuPisServiceInternal.updatePsuInPayment(PSU_ID_DATA, AUTHORISATION_ID, DEFAULT_SERVICE_INSTANCE_ID);

        // Then
        assertTrue(actualResult);
        verify(pisAuthorisationSpecification, times(1))
            .byExternalIdAndInstanceId(AUTHORISATION_ID, DEFAULT_SERVICE_INSTANCE_ID);
    }

    @Test
    public void updatePsuInPayment_Fail_WrongPaymentId() {
        // Given
        //noinspection unchecked
        when(pisAuthorisationRepository.findOne(any(Specification.class))).thenReturn(null);

        // When
        boolean actualResult = cmsPsuPisServiceInternal.updatePsuInPayment(PSU_ID_DATA, WRONG_AUTHORISATION_ID, DEFAULT_SERVICE_INSTANCE_ID);

        // Then
        assertFalse(actualResult);
        verify(pisAuthorisationSpecification, times(1))
            .byExternalIdAndInstanceId(WRONG_AUTHORISATION_ID, DEFAULT_SERVICE_INSTANCE_ID);
    }

    @Test
    public void getPayment_Success() {
        // Given
        //noinspection unchecked
        when(pisPaymentDataRepository.findAll(any(Specification.class))).thenReturn(buildPisPaymentDataList());

        // When
        Optional<CmsPayment> actualResult = cmsPsuPisServiceInternal.getPayment(PSU_ID_DATA, PAYMENT_ID, DEFAULT_SERVICE_INSTANCE_ID);

        // Then
        assertTrue(actualResult.isPresent());
        assertThat(actualResult.get().getPaymentId()).isEqualTo(PAYMENT_ID);
        verify(pisPaymentDataSpecification, times(1))
            .byPaymentIdAndInstanceId(PAYMENT_ID, DEFAULT_SERVICE_INSTANCE_ID);
    }

    @Test
    public void getPayment_Fail_WrongPaymentId() {
        // Given
        //noinspection unchecked
        when(pisPaymentDataRepository.findAll(any(Specification.class))).thenReturn(Collections.emptyList());

        // When
        Optional<CmsPayment> actualResult = cmsPsuPisServiceInternal.getPayment(PSU_ID_DATA, WRONG_PAYMENT_ID, DEFAULT_SERVICE_INSTANCE_ID);

        // Then
        assertFalse(actualResult.isPresent());
        verify(pisCommonPaymentService, times(1))
            .getPsuDataListByPaymentId(WRONG_PAYMENT_ID);
    }

    @Test
    public void getPayment_Fail_WrongPsuIdData() {
        // Given
        //noinspection unchecked
        when(pisPaymentDataRepository.findAll(any(Specification.class))).thenReturn(buildPisPaymentDataList());

        // When
        Optional<CmsPayment> actualResult = cmsPsuPisServiceInternal.getPayment(WRONG_PSU_ID_DATA, PAYMENT_ID, DEFAULT_SERVICE_INSTANCE_ID);

        // Then
        assertFalse(actualResult.isPresent());
        verify(pisPaymentDataSpecification, never())
            .byPaymentIdAndInstanceId(PAYMENT_ID, DEFAULT_SERVICE_INSTANCE_ID);
    }

    @Test
    public void updateAuthorisationStatus_Success() {
        // Given
        //noinspection unchecked
        when(pisAuthorisationRepository.findOne(any(Specification.class))).thenReturn(buildPisAuthorisation());

        // When
        boolean actualResult = cmsPsuPisServiceInternal.updateAuthorisationStatus(PSU_ID_DATA, PAYMENT_ID, AUTHORISATION_ID, ScaStatus.FAILED, DEFAULT_SERVICE_INSTANCE_ID);

        // Then
        assertTrue(actualResult);
        verify(pisAuthorisationSpecification, times(1))
            .byExternalIdAndInstanceId(AUTHORISATION_ID, DEFAULT_SERVICE_INSTANCE_ID);
    }

    @Test
    public void updateAuthorisationStatus_WrongPaymentId() {
        // Given
        //noinspection unchecked
        when(pisAuthorisationRepository.findOne(any(Specification.class))).thenReturn(buildPisAuthorisation());

        // When
        boolean actualResult = cmsPsuPisServiceInternal.updateAuthorisationStatus(PSU_ID_DATA, WRONG_PAYMENT_ID, AUTHORISATION_ID, ScaStatus.FAILED, DEFAULT_SERVICE_INSTANCE_ID);

        // Then
        assertFalse(actualResult);
        verify(pisAuthorisationSpecification, times(1))
            .byExternalIdAndInstanceId(AUTHORISATION_ID, DEFAULT_SERVICE_INSTANCE_ID);
    }

    @Test
    public void updateAuthorisationStatus_WrongPsuIdData() {
        // Given
        //noinspection unchecked
        when(pisAuthorisationRepository.findOne(any(Specification.class))).thenReturn(buildPisAuthorisation());

        // When
        boolean actualResult = cmsPsuPisServiceInternal.updateAuthorisationStatus(WRONG_PSU_ID_DATA, PAYMENT_ID, AUTHORISATION_ID, ScaStatus.FAILED, DEFAULT_SERVICE_INSTANCE_ID);

        // Then
        assertFalse(actualResult);
        verify(pisAuthorisationSpecification, times(1))
            .byExternalIdAndInstanceId(AUTHORISATION_ID, DEFAULT_SERVICE_INSTANCE_ID);
    }

    @Test
    public void updateAuthorisationStatus_WrongAuthorisationId() {
        // Given
        //noinspection unchecked
        when(pisAuthorisationRepository.findOne(any(Specification.class))).thenReturn(null);

        // When
        boolean actualResult = cmsPsuPisServiceInternal.updateAuthorisationStatus(PSU_ID_DATA, PAYMENT_ID, WRONG_AUTHORISATION_ID, ScaStatus.FAILED, DEFAULT_SERVICE_INSTANCE_ID);

        // Then
        assertFalse(actualResult);
        verify(pisAuthorisationSpecification, times(1))
            .byExternalIdAndInstanceId(WRONG_AUTHORISATION_ID, DEFAULT_SERVICE_INSTANCE_ID);
    }

    @Test
    public void updatePaymentStatus_Success() {
        // Given
        //noinspection unchecked
        when(pisPaymentDataRepository.findAll(any(Specification.class))).thenReturn(buildPisPaymentDataList());

        // When
        boolean actualResult = cmsPsuPisServiceInternal.updatePaymentStatus(PAYMENT_ID, TransactionStatus.RCVD, DEFAULT_SERVICE_INSTANCE_ID);

        // Then
        assertTrue(actualResult);
        verify(pisPaymentDataSpecification, times(1))
            .byPaymentIdAndInstanceId(PAYMENT_ID, DEFAULT_SERVICE_INSTANCE_ID);
    }

    @Test
    public void updatePaymentStatus_Fail_WrongPaymentId() {
        // Given
        //noinspection unchecked
        when(pisPaymentDataRepository.findAll(any(Specification.class))).thenReturn(Collections.emptyList());

        // When
        boolean actualResult = cmsPsuPisServiceInternal.updatePaymentStatus(WRONG_PAYMENT_ID, TransactionStatus.CANC, DEFAULT_SERVICE_INSTANCE_ID);

        // Then
        assertFalse(actualResult);
        verify(pisPaymentDataSpecification, times(1))
            .byPaymentIdAndInstanceId(WRONG_PAYMENT_ID, DEFAULT_SERVICE_INSTANCE_ID);
    }

    @Test
    public void updateAuthorisationStatus_Fail_FinalisedStatus() {
        //Given
        PisAuthorization finalisedPisAuthorisation = buildFinalisedAuthorisation();
        //noinspection unchecked
        when(pisAuthorisationRepository.findOne(any(Specification.class))).thenReturn(buildFinalisedAuthorisation());

        // When
        boolean actualResult = cmsPsuPisServiceInternal.updateAuthorisationStatus(PSU_ID_DATA, PAYMENT_ID, FINALISED_AUTHORISATION_ID, ScaStatus.SCAMETHODSELECTED, DEFAULT_SERVICE_INSTANCE_ID);

        // Then
        assertFalse(actualResult);
        verify(pisAuthorisationSpecification, times(1))
            .byExternalIdAndInstanceId(FINALISED_AUTHORISATION_ID, DEFAULT_SERVICE_INSTANCE_ID);
    }

    @Test
    public void updatePaymentStatus_Fail_FinalisedStatus() {
        //Given
        List<PisPaymentData> finalisedPisPaymentDataList = buildFinalisedPisPaymentDataList();
        //noinspection unchecked
        when(pisPaymentDataRepository.findAll(any(Specification.class))).thenReturn(finalisedPisPaymentDataList);

        // When
        boolean actualResult = cmsPsuPisServiceInternal.updatePaymentStatus(FINALISED_PAYMENT_ID, TransactionStatus.CANC, DEFAULT_SERVICE_INSTANCE_ID);

        // Then
        assertFalse(actualResult);
        verify(pisPaymentDataSpecification, times(1))
            .byPaymentIdAndInstanceId(FINALISED_PAYMENT_ID, DEFAULT_SERVICE_INSTANCE_ID);
    }

    @Test
    public void getPaymentByAuthorisationId_Success() {
        //Given
        PisAuthorization expectedAuthorisation = buildPisAuthorisation();
        //noinspection unchecked
        when(pisAuthorisationRepository.findOne(any(Specification.class))).thenReturn(expectedAuthorisation);

        // When
        Optional<CmsPaymentResponse> actualResult = cmsPsuPisServiceInternal.checkRedirectAndGetPayment(AUTHORISATION_ID, DEFAULT_SERVICE_INSTANCE_ID);

        // Then
        assertTrue(actualResult.isPresent());
        assertThat(actualResult.get().getAuthorisationId()).isEqualTo(AUTHORISATION_ID);
        verify(pisAuthorisationSpecification, times(1))
            .byExternalIdAndInstanceId(AUTHORISATION_ID, DEFAULT_SERVICE_INSTANCE_ID);
    }

    @Test
    public void getPaymentByAuthorisationId_Fail_ExpiredRedirectUrl() {
        //Given
        PisAuthorization expectedAuthorisation = buildExpiredAuthorisation();
        //noinspection unchecked
        when(pisAuthorisationRepository.findOne(any(Specification.class))).thenReturn(expectedAuthorisation);

        // When
        Optional<CmsPaymentResponse> actualResult = cmsPsuPisServiceInternal.checkRedirectAndGetPayment(EXPIRED_AUTHORISATION_ID, DEFAULT_SERVICE_INSTANCE_ID);

        // Then
        assertThat(actualResult).isEqualTo(Optional.of(new CmsPaymentResponse(TPP_NOK_REDIRECT_URI)));
        verify(pisAuthorisationSpecification, times(1))
            .byExternalIdAndInstanceId(EXPIRED_AUTHORISATION_ID, DEFAULT_SERVICE_INSTANCE_ID);
    }

    @Test
    public void getPaymentByAuthorisationId_Fail_WrongId() {
        // Given
        //noinspection unchecked
        when(pisAuthorisationRepository.findOne(any(Specification.class))).thenReturn(null);

        // When
        Optional<CmsPaymentResponse> actualResult = cmsPsuPisServiceInternal.checkRedirectAndGetPayment(WRONG_AUTHORISATION_ID, DEFAULT_SERVICE_INSTANCE_ID);

        // Then
        assertThat(actualResult).isEqualTo(Optional.empty());
        verify(pisAuthorisationSpecification, times(1))
            .byExternalIdAndInstanceId(WRONG_AUTHORISATION_ID, DEFAULT_SERVICE_INSTANCE_ID);
    }

    @Test
    public void checkRedirectAndGetPaymentForCancellation_Success() {
        //Given
        PisAuthorization expectedAuthorisation = buildPisAuthorisation();
        //noinspection unchecked
        when(pisAuthorisationRepository.findOne(any(Specification.class))).thenReturn(expectedAuthorisation);

        // When
        Optional<CmsPaymentResponse> actualResult = cmsPsuPisServiceInternal.checkRedirectAndGetPaymentForCancellation(AUTHORISATION_ID, DEFAULT_SERVICE_INSTANCE_ID);

        // Then
        assertTrue(actualResult.isPresent());
        assertThat(actualResult.get().getAuthorisationId()).isEqualTo(AUTHORISATION_ID);
        verify(pisAuthorisationSpecification, times(1))
            .byExternalIdAndInstanceId(AUTHORISATION_ID, DEFAULT_SERVICE_INSTANCE_ID);
    }

    @Test
    public void checkRedirectAndGetPaymentForCancellation_Fail_ExpiredRedirectUrl() {
        //Given
        PisAuthorization expectedAuthorisation = buildExpiredAuthorisation();
        //noinspection unchecked
        when(pisAuthorisationRepository.findOne(any(Specification.class))).thenReturn(expectedAuthorisation);

        // When
        Optional<CmsPaymentResponse> actualResult = cmsPsuPisServiceInternal.checkRedirectAndGetPaymentForCancellation(EXPIRED_AUTHORISATION_ID, DEFAULT_SERVICE_INSTANCE_ID);

        // Then
        assertThat(actualResult).isEqualTo(Optional.of(new CmsPaymentResponse(TPP_NOK_REDIRECT_URI)));
        verify(pisAuthorisationSpecification, times(1))
            .byExternalIdAndInstanceId(EXPIRED_AUTHORISATION_ID, DEFAULT_SERVICE_INSTANCE_ID);
    }

    @Test
    public void checkRedirectAndGetPaymentForCancellation_Fail_WrongId() {
        // Given
        //noinspection unchecked
        when(pisAuthorisationRepository.findOne(any(Specification.class))).thenReturn(null);

        // When
        Optional<CmsPaymentResponse> actualResult = cmsPsuPisServiceInternal.checkRedirectAndGetPaymentForCancellation(WRONG_AUTHORISATION_ID, DEFAULT_SERVICE_INSTANCE_ID);

        // Then
        assertThat(actualResult).isEqualTo(Optional.empty());
        verify(pisAuthorisationSpecification, times(1))
            .byExternalIdAndInstanceId(WRONG_AUTHORISATION_ID, DEFAULT_SERVICE_INSTANCE_ID);
    }

    private PsuIdData buildPsuIdData() {
        return new PsuIdData(
            "psuId",
            "psuIdType",
            "psuCorporateId",
            "psuCorporateIdType"
        );
    }

    private PsuIdData buildWrongPsuIdData() {
        return new PsuIdData(
            "wrong psuId",
            "psuIdType",
            "wrong psuCorporateId",
            "psuCorporateIdType"
        );
    }

    private PisAuthorization buildPisAuthorisation() {
        PisAuthorization pisAuthorisation = new PisAuthorization();
        pisAuthorisation.setScaStatus(ScaStatus.PSUAUTHENTICATED);
        pisAuthorisation.setAuthorizationType(CmsAuthorisationType.CREATED);
        pisAuthorisation.setPaymentData(buildPisCommonPaymentData());
        pisAuthorisation.setExternalId(AUTHORISATION_ID);
        pisAuthorisation.setPsuData(buildPsuData());
        pisAuthorisation.setRedirectUrlExpirationTimestamp(OffsetDateTime.parse("2022-12-03T10:15:30+01:00"));
        pisAuthorisation.setPaymentData(buildPisCommonPaymentData());

        return pisAuthorisation;
    }

    private PisCommonPaymentData buildPisCommonPaymentData() {
        PisCommonPaymentData pisCommonPaymentData = new PisCommonPaymentData();
        pisCommonPaymentData.setTransactionStatus(TransactionStatus.RCVD);
        pisCommonPaymentData.setPsuData(Collections.singletonList(buildPsuData()));
        pisCommonPaymentData.setPaymentType(PaymentType.SINGLE);
        pisCommonPaymentData.setPaymentProduct(PAYMENT_PRODUCT);
        pisCommonPaymentData.setPayments(buildPisPaymentDataListForCommonData());
        pisCommonPaymentData.setTppInfo(buildTppInfo());
        pisCommonPaymentData.setPaymentId(PAYMENT_ID);
        pisCommonPaymentData.setCreationTimestamp(OffsetDateTime.of(2018, 10, 10, 10, 10, 10, 10, ZoneOffset.UTC));
        return pisCommonPaymentData;
    }

    private TppInfoEntity buildTppInfo() {
        TppInfoEntity tppInfoEntity = new TppInfoEntity();
        tppInfoEntity.setNokRedirectUri("tpp nok redirect uri");
        tppInfoEntity.setRedirectUri("tpp ok redirect uri");

        return tppInfoEntity;
    }

    private PsuData buildPsuData() {
        PsuIdData psuIdData = buildPsuIdData();
        PsuData psuData = new PsuData(
            psuIdData.getPsuId(),
            psuIdData.getPsuIdType(),
            psuIdData.getPsuCorporateId(),
            psuIdData.getPsuCorporateIdType()
        );
        psuData.setId(1L);

        return psuData;
    }

    private List<PisPaymentData> buildPisPaymentDataList() {
        PisPaymentData pisPaymentData = new PisPaymentData();
        pisPaymentData.setPaymentId(PAYMENT_ID);
        pisPaymentData.setPaymentData(buildPisCommonPaymentData());
        pisPaymentData.setTransactionStatus(TransactionStatus.RCVD);
        pisPaymentData.setDebtorAccount(buildAccountReference());
        pisPaymentData.setCreditorAccount(buildAccountReference());
        pisPaymentData.setAmount(new BigDecimal("1000"));
        pisPaymentData.setCurrency(Currency.getInstance("EUR"));

        return Collections.singletonList(pisPaymentData);
    }

    private List<PisPaymentData> buildPisPaymentDataListForCommonData() {
        PisPaymentData pisPaymentData = new PisPaymentData();
        pisPaymentData.setPaymentId(PAYMENT_ID);
        pisPaymentData.setTransactionStatus(TransactionStatus.ACCP);
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

    private CmsPayment buildCmsPayment() {
        CmsSinglePayment cmsPayment = new CmsSinglePayment(PAYMENT_PRODUCT);
        cmsPayment.setPaymentId(PAYMENT_ID);

        return cmsPayment;
    }

    private PisAuthorization buildFinalisedAuthorisation() {
        PisAuthorization pisAuthorisation = new PisAuthorization();
        pisAuthorisation.setScaStatus(ScaStatus.FINALISED);
        pisAuthorisation.setAuthorizationType(CmsAuthorisationType.CREATED);
        pisAuthorisation.setPaymentData(buildPisCommonPaymentData());
        pisAuthorisation.setExternalId(AUTHORISATION_ID);
        pisAuthorisation.setPsuData(buildPsuData());

        return pisAuthorisation;
    }

    private List<PisPaymentData> buildFinalisedPisPaymentDataList() {
        PisPaymentData pisPaymentData = new PisPaymentData();
        pisPaymentData.setPaymentId(PAYMENT_ID);
        pisPaymentData.setPaymentData(buildPisCommonPaymentData());
        pisPaymentData.setTransactionStatus(TransactionStatus.RJCT);
        pisPaymentData.setDebtorAccount(buildAccountReference());
        pisPaymentData.setCreditorAccount(buildAccountReference());
        pisPaymentData.setAmount(new BigDecimal("1000"));
        pisPaymentData.setCurrency(Currency.getInstance("EUR"));

        return Collections.singletonList(pisPaymentData);
    }

    private PisAuthorization buildExpiredAuthorisation() {
        PisAuthorization pisAuthorisation = new PisAuthorization();
        pisAuthorisation.setScaStatus(ScaStatus.STARTED);
        pisAuthorisation.setAuthorizationType(CmsAuthorisationType.CREATED);
        pisAuthorisation.setPaymentData(buildPisCommonPaymentData());
        pisAuthorisation.setExternalId(EXPIRED_AUTHORISATION_ID);
        pisAuthorisation.setPsuData(buildPsuData());
        pisAuthorisation.setRedirectUrlExpirationTimestamp(OffsetDateTime.parse("2017-12-03T10:15:30+01:00"));

        return pisAuthorisation;
    }

    private CmsPaymentResponse buildCmsPaymentResponse(String authorisationId) {
        return new CmsPaymentResponse(
            buildCmsPayment(),
            authorisationId,
            TPP_OK_REDIRECT_URI,
            TPP_NOK_REDIRECT_URI
        );
    }
}

