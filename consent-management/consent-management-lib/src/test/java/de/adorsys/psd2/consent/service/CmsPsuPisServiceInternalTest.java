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

import de.adorsys.psd2.consent.api.CmsAuthorisationType;
import de.adorsys.psd2.consent.api.pis.CmsPayment;
import de.adorsys.psd2.consent.api.pis.CmsPaymentResponse;
import de.adorsys.psd2.consent.api.pis.CmsSinglePayment;
import de.adorsys.psd2.consent.api.service.PisConsentService;
import de.adorsys.psd2.consent.domain.AccountReferenceEntity;
import de.adorsys.psd2.consent.domain.PsuData;
import de.adorsys.psd2.consent.domain.TppInfoEntity;
import de.adorsys.psd2.consent.domain.payment.PisConsent;
import de.adorsys.psd2.consent.domain.payment.PisConsentAuthorization;
import de.adorsys.psd2.consent.domain.payment.PisPaymentData;
import de.adorsys.psd2.consent.repository.PisConsentAuthorizationRepository;
import de.adorsys.psd2.consent.repository.PisPaymentDataRepository;
import de.adorsys.psd2.consent.repository.PsuDataRepository;
import de.adorsys.psd2.consent.service.mapper.CmsPsuPisMapper;
import de.adorsys.psd2.consent.service.mapper.PsuDataMapper;
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

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Currency;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CmsPsuPisServiceInternalTest {

    private static final String WRONG_PAYMENT_ID = "wrong payment id";
    private static final String AUTHORISATION_ID = "authorisation id";
    private static final String WRONG_AUTHORISATION_ID = "wrong authorisation id";
    private static final String CONSENT_ID = "consent id";
    private static final String PAYMENT_PRODUCT = "sepa-credit-transfers";
    private static final String FINALISED_PAYMENT_ID = "finalised payment id";
    private static final String FINALISED_AUTHORISATION_ID = "finalised authorisation id";
    private static final String EXPIRED_AUTHORISATION_ID = "expired authorisation id";
    private static final String TPP_OK_REDIRECT_URI = "tpp ok redirect uri";
    private static final String TPP_NOK_REDIRECT_URI = "tpp nok redirect uri";
    private final PsuIdData WRONG_PSU_ID_DATA = buildWrongPsuIdData();
    private final PsuIdData PSU_ID_DATA = buildPsuIdData();
    private static final String PAYMENT_ID = "payment id";

    @InjectMocks
    private CmsPsuPisServiceInternal cmsPsuPisServiceInternal;

    @Mock
    PisPaymentDataRepository pisPaymentDataRepository;
    @Mock
    PisConsentAuthorizationRepository pisConsentAuthorizationRepository;
    @Mock
    CmsPsuPisMapper cmsPsuPisMapper;
    @Mock
    PisConsentService pisConsentService;
    @Mock
    PsuDataRepository psuDataRepository;
    @Mock
    PsuDataMapper psuDataMapper;

    @Before
    public void setUp() {
        List<PisPaymentData> pisPaymentDataList = buildPisPaymentDataList();
        PisConsentAuthorization pisConsentAuthorisation = buildPisConsentAuthorisation();
        PsuData psuData = buildPsuData();
        PsuIdData psuIdData = buildPsuIdData();
        CmsPayment cmsPayment = buildCmsPayment();

        when(pisPaymentDataRepository.findByPaymentId(PAYMENT_ID))
            .thenReturn(Optional.of(pisPaymentDataList));
        when(pisPaymentDataRepository.findByPaymentId(WRONG_PAYMENT_ID))
            .thenReturn(Optional.empty());
        when(pisPaymentDataRepository.save(any(PisPaymentData.class)))
            .thenReturn(pisPaymentDataList.get(0));

        when(pisConsentAuthorizationRepository.findByExternalId(AUTHORISATION_ID))
            .thenReturn(Optional.of(pisConsentAuthorisation));
        when(pisConsentAuthorizationRepository.findByExternalId(WRONG_AUTHORISATION_ID))
            .thenReturn(Optional.empty());
        when(pisConsentAuthorizationRepository.save(any(PisConsentAuthorization.class)))
            .thenReturn(pisConsentAuthorisation);

        when(cmsPsuPisMapper.mapToCmsPayment(buildPisPaymentDataList()))
            .thenReturn(cmsPayment);

        when(pisConsentService.getPsuDataByPaymentId(PAYMENT_ID))
            .thenReturn(Optional.of(psuIdData));
        when(pisConsentService.getPsuDataByPaymentId(WRONG_PAYMENT_ID))
            .thenReturn(Optional.empty());
        when(pisConsentService.getDecryptedId(PAYMENT_ID))
            .thenReturn(Optional.of(PAYMENT_ID));
        when(pisConsentService.getDecryptedId(WRONG_PAYMENT_ID))
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
        // When
        boolean actualResult = cmsPsuPisServiceInternal.updatePsuInPayment(PSU_ID_DATA, PAYMENT_ID);

        // Then
        assertTrue(actualResult);
    }

    @Test
    public void updatePsuInPayment_Fail_WrongPaymentId() {
        // When
        boolean actualResult = cmsPsuPisServiceInternal.updatePsuInPayment(PSU_ID_DATA, WRONG_PAYMENT_ID);

        // Then
        assertFalse(actualResult);
    }

    @Test
    public void getPayment_Success() {
        // When
        Optional<CmsPayment> actualResult = cmsPsuPisServiceInternal.getPayment(PSU_ID_DATA, PAYMENT_ID);

        // Then
        assertTrue(actualResult.isPresent());
        assertThat(actualResult.get().getPaymentId()).isEqualTo(PAYMENT_ID);
    }

    @Test
    public void getPayment_Fail_WrongPaymentId() {
        // When
        Optional<CmsPayment> actualResult = cmsPsuPisServiceInternal.getPayment(PSU_ID_DATA, WRONG_PAYMENT_ID);

        // Then
        assertFalse(actualResult.isPresent());
    }

    @Test
    public void getPayment_Fail_WrongPsuIdData() {
        // When
        Optional<CmsPayment> actualResult = cmsPsuPisServiceInternal.getPayment(WRONG_PSU_ID_DATA, PAYMENT_ID);

        // Then
        assertFalse(actualResult.isPresent());
    }

    @Test
    public void updateAuthorisationStatus_Success() {
        // When
        boolean actualResult = cmsPsuPisServiceInternal.updateAuthorisationStatus(PSU_ID_DATA, PAYMENT_ID, AUTHORISATION_ID, ScaStatus.FAILED);

        // Then
        assertTrue(actualResult);
    }

    @Test
    public void updateAuthorisationStatus_WrongPaymentId() {
        // When
        boolean actualResult = cmsPsuPisServiceInternal.updateAuthorisationStatus(PSU_ID_DATA, WRONG_PAYMENT_ID, AUTHORISATION_ID, ScaStatus.FAILED);

        // Then
        assertFalse(actualResult);
    }

    @Test
    public void updateAuthorisationStatus_WrongPsuIdData() {
        // When
        boolean actualResult = cmsPsuPisServiceInternal.updateAuthorisationStatus(WRONG_PSU_ID_DATA, PAYMENT_ID, AUTHORISATION_ID, ScaStatus.FAILED);

        // Then
        assertFalse(actualResult);
    }

    @Test
    public void updateAuthorisationStatus_WrongAuthorisationId() {
        // When
        boolean actualResult = cmsPsuPisServiceInternal.updateAuthorisationStatus(PSU_ID_DATA, PAYMENT_ID, WRONG_AUTHORISATION_ID, ScaStatus.FAILED);

        // Then
        assertFalse(actualResult);
    }

    @Test
    public void updatePaymentStatus_Success() {
        // When
        boolean actualResult = cmsPsuPisServiceInternal.updatePaymentStatus(PAYMENT_ID, TransactionStatus.RCVD);

        // Then
        assertTrue(actualResult);
    }

    @Test
    public void updatePaymentStatus_Fail_WrongPaymentId() {
        // When
        boolean actualResult = cmsPsuPisServiceInternal.updatePaymentStatus(WRONG_PAYMENT_ID, TransactionStatus.CANC);

        // Then
        assertFalse(actualResult);
    }

    @Test
    public void updateAuthorisationStatus_Fail_FinalisedStatus() {
        //Given
        PisConsentAuthorization finalisedPisAuthorisation = buildFinalisedAuthorisation();
        when(pisConsentAuthorizationRepository.findByExternalId(FINALISED_AUTHORISATION_ID))
            .thenReturn(Optional.of(finalisedPisAuthorisation));

        // When
        boolean actualResult = cmsPsuPisServiceInternal.updateAuthorisationStatus(PSU_ID_DATA, PAYMENT_ID, FINALISED_AUTHORISATION_ID, ScaStatus.SCAMETHODSELECTED);

        // Then
        assertFalse(actualResult);
    }

    @Test
    public void updatePaymentStatus_Fail_FinalisedStatus() {
        //Given
        List<PisPaymentData> finalisedPisPaymentDataList = buildFinalisedPisPaymentDataList();
        when(pisConsentService.getDecryptedId(FINALISED_PAYMENT_ID)).thenReturn(Optional.of(FINALISED_PAYMENT_ID));
        when(pisPaymentDataRepository.findByPaymentId(FINALISED_PAYMENT_ID)).thenReturn(Optional.of(finalisedPisPaymentDataList));

        // When
        boolean actualResult = cmsPsuPisServiceInternal.updatePaymentStatus(FINALISED_PAYMENT_ID, TransactionStatus.CANC);

        // Then
        assertFalse(actualResult);
    }

    @Test
    public void getPaymentByAuthorisationId_Success() {
        //Given
        PisConsentAuthorization expectedAuthorisation = buildPisConsentAuthorisation();
        CmsPaymentResponse expectedCmsPaymentResponse = buildCmsPaymentResponse(expectedAuthorisation.getExternalId());
        when(pisConsentAuthorizationRepository.findByExternalId(AUTHORISATION_ID)).thenReturn(Optional.of(expectedAuthorisation));
        when(cmsPsuPisMapper.mapToCmsPaymentResponse(any(CmsPayment.class), any(String.class), any(String.class), any(String.class))).thenReturn(Optional.of(expectedCmsPaymentResponse));

        // When
        Optional<CmsPaymentResponse> actualResult = cmsPsuPisServiceInternal.checkRedirectAndGetPayment(PSU_ID_DATA, AUTHORISATION_ID);

        // Then
        assertTrue(actualResult.isPresent());
        assertThat(actualResult.get().getAuthorisationId()).isEqualTo(AUTHORISATION_ID);
    }

    @Test
    public void getPaymentByAuthorisationId_Fail_ExpiredRedirectUrl() {
        //Given
        PisConsentAuthorization expectedAuthorisation = buildExpiredAuthorisation();
        when(pisConsentService.getDecryptedId(EXPIRED_AUTHORISATION_ID)).thenReturn(Optional.of(EXPIRED_AUTHORISATION_ID));
        when(pisConsentAuthorizationRepository.findByExternalId(EXPIRED_AUTHORISATION_ID)).thenReturn(Optional.of(expectedAuthorisation));

        // When
        Optional<CmsPaymentResponse> actualResult = cmsPsuPisServiceInternal.checkRedirectAndGetPayment(PSU_ID_DATA, EXPIRED_AUTHORISATION_ID);

        // Then
        assertThat(actualResult).isEqualTo(Optional.empty());
    }

    @Test
    public void getPaymentByAuthorisationId_Fail_WrongId() {
        // When
        Optional<CmsPaymentResponse> actualResult = cmsPsuPisServiceInternal.checkRedirectAndGetPayment(PSU_ID_DATA, WRONG_AUTHORISATION_ID);

        // Then
        assertThat(actualResult).isEqualTo(Optional.empty());
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

    private PisConsentAuthorization buildPisConsentAuthorisation() {
        PisConsentAuthorization pisConsentAuthorisation = new PisConsentAuthorization();
        pisConsentAuthorisation.setScaStatus(ScaStatus.PSUAUTHENTICATED);
        pisConsentAuthorisation.setAuthorizationType(CmsAuthorisationType.CREATED);
        pisConsentAuthorisation.setConsent(buildPisConsent());
        pisConsentAuthorisation.setExternalId(AUTHORISATION_ID);
        pisConsentAuthorisation.setPsuData(buildPsuData());
        pisConsentAuthorisation.setRedirectUrlExpirationTimestamp(OffsetDateTime.parse("2022-12-03T10:15:30+01:00"));

        return pisConsentAuthorisation;
    }

    private PisConsent buildPisConsent() {
        PisConsent pisConsent = new PisConsent();
        pisConsent.setPsuData(buildPsuData());
        pisConsent.setExternalId(CONSENT_ID);
        pisConsent.setPaymentType(PaymentType.SINGLE);
        pisConsent.setPaymentProduct(PAYMENT_PRODUCT);
        pisConsent.setPayments(buildPisPaymentDataListForConsent());
        pisConsent.setTppInfo(buildTppInfo());

        return pisConsent;
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
        pisPaymentData.setConsent(buildPisConsent());
        pisPaymentData.setTransactionStatus(TransactionStatus.RCVD);
        pisPaymentData.setDebtorAccount(buildAccountReference());
        pisPaymentData.setCreditorAccount(buildAccountReference());
        pisPaymentData.setAmount(new BigDecimal("1000"));
        pisPaymentData.setCurrency(Currency.getInstance("EUR"));

        return Collections.singletonList(pisPaymentData);
    }

    private List<PisPaymentData> buildPisPaymentDataListForConsent() {
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

    private PisConsentAuthorization buildFinalisedAuthorisation() {
        PisConsentAuthorization pisConsentAuthorisation = new PisConsentAuthorization();
        pisConsentAuthorisation.setScaStatus(ScaStatus.FINALISED);
        pisConsentAuthorisation.setAuthorizationType(CmsAuthorisationType.CREATED);
        pisConsentAuthorisation.setConsent(buildPisConsent());
        pisConsentAuthorisation.setExternalId(AUTHORISATION_ID);
        pisConsentAuthorisation.setPsuData(buildPsuData());

        return pisConsentAuthorisation;
    }

    private List<PisPaymentData> buildFinalisedPisPaymentDataList() {
        PisPaymentData pisPaymentData = new PisPaymentData();
        pisPaymentData.setPaymentId(PAYMENT_ID);
        pisPaymentData.setConsent(buildPisConsent());
        pisPaymentData.setTransactionStatus(TransactionStatus.RJCT);
        pisPaymentData.setDebtorAccount(buildAccountReference());
        pisPaymentData.setCreditorAccount(buildAccountReference());
        pisPaymentData.setAmount(new BigDecimal("1000"));
        pisPaymentData.setCurrency(Currency.getInstance("EUR"));

        return Collections.singletonList(pisPaymentData);
    }


    private PisConsentAuthorization buildExpiredAuthorisation() {
        PisConsentAuthorization pisConsentAuthorisation = new PisConsentAuthorization();
        pisConsentAuthorisation.setScaStatus(ScaStatus.STARTED);
        pisConsentAuthorisation.setAuthorizationType(CmsAuthorisationType.CREATED);
        pisConsentAuthorisation.setConsent(buildPisConsent());
        pisConsentAuthorisation.setExternalId(EXPIRED_AUTHORISATION_ID);
        pisConsentAuthorisation.setPsuData(buildPsuData());
        pisConsentAuthorisation.setRedirectUrlExpirationTimestamp(OffsetDateTime.parse("2017-12-03T10:15:30+01:00"));

        return pisConsentAuthorisation;
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
