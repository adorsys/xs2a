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

import de.adorsys.psd2.consent.api.pis.CmsPayment;
import de.adorsys.psd2.consent.api.pis.CmsSinglePayment;
import de.adorsys.psd2.consent.aspsp.api.PageData;
import de.adorsys.psd2.consent.domain.PsuData;
import de.adorsys.psd2.consent.domain.payment.PisCommonPaymentData;
import de.adorsys.psd2.consent.domain.payment.PisPaymentData;
import de.adorsys.psd2.consent.repository.PisCommonPaymentDataRepository;
import de.adorsys.psd2.consent.repository.specification.PisCommonPaymentDataSpecification;
import de.adorsys.psd2.consent.service.mapper.CmsPsuPisMapper;
import de.adorsys.psd2.consent.service.psu.util.PageRequestBuilder;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.Collections;
import java.util.Currency;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CmsAspspPisExportServiceInternalTest {
    public static final Integer PAGE_INDEX = 0;
    public static final Integer ITEMS_PER_PAGE = 20;
    private static final String TPP_AUTHORISATION_NUMBER = "authorisation number";
    private static final String WRONG_TPP_AUTHORISATION_NUMBER = "wrong authorisation number";
    private static final LocalDate CREATION_DATE_FROM = LocalDate.of(2019, 1, 1);
    private static final LocalDate CREATION_DATE_TO = LocalDate.of(2020, 12, 1);
    private static final String DEFAULT_SERVICE_INSTANCE_ID = "UNDEFINED";
    private static final String PSU_ID = "psu id";
    private static final String WRONG_PSU_ID = "wrong psu id";

    private static final String ASPSP_ACCOUNT_ID = "aspsp account id";
    private static final String WRONG_ASPSP_ACCOUNT_ID = "wrong aspsp account id";

    private static final String PAYMENT_ID = "payment id";
    private static final String PAYMENT_PRODUCT = "sepa-credit-transfers";

    private PsuIdData psuIdData;
    private PsuIdData wrongPsuIdData;

    @InjectMocks
    private CmsAspspPisExportServiceInternal cmsAspspPisExportServiceInternal;
    @Mock
    private PisCommonPaymentDataSpecification pisCommonPaymentDataSpecification;
    @Mock
    private PisCommonPaymentDataRepository pisCommonPaymentDataRepository;
    @Mock
    private CmsPsuPisMapper cmsPsuPisMapper;
    @Spy
    private PageRequestBuilder pageRequestBuilder = new PageRequestBuilder();

    @BeforeEach
    void setUp() {
        psuIdData = buildPsuIdData(PSU_ID);
        wrongPsuIdData = buildPsuIdData(WRONG_PSU_ID);
    }

    @Test
    void exportPaymentsByTpp_success() {
        // Given
        when(pisCommonPaymentDataSpecification.byTppIdAndCreationPeriodAndPsuIdDataAndInstanceId(TPP_AUTHORISATION_NUMBER,
                                                                                                 CREATION_DATE_FROM,
                                                                                                 CREATION_DATE_TO,
                                                                                                 psuIdData,
                                                                                                 DEFAULT_SERVICE_INSTANCE_ID)).thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        //noinspection unchecked
        when(pisCommonPaymentDataRepository.findAll(any(Specification.class), eq(Pageable.unpaged())))
            .thenReturn(new PageImpl<>(Collections.singletonList(buildPisCommonPaymentData()), PageRequest.of(0, 20), 1));
        CmsPayment expectedPayment = buildCmsPayment();
        when(cmsPsuPisMapper.mapPaymentDataToCmsPayment(buildPisCommonPaymentData()))
            .thenReturn(buildCmsPayment());

        // When
        PageData<Collection<CmsPayment>> payments =
            cmsAspspPisExportServiceInternal.exportPaymentsByTpp(TPP_AUTHORISATION_NUMBER, CREATION_DATE_FROM,
                                                                 CREATION_DATE_TO, psuIdData, DEFAULT_SERVICE_INSTANCE_ID, null, null);

        // Then
        assertFalse(payments.getData().isEmpty());
        assertTrue(payments.getData().contains(expectedPayment));
        verify(pisCommonPaymentDataSpecification, times(1))
            .byTppIdAndCreationPeriodAndPsuIdDataAndInstanceId(TPP_AUTHORISATION_NUMBER, CREATION_DATE_FROM,
                                                               CREATION_DATE_TO, psuIdData, DEFAULT_SERVICE_INSTANCE_ID);
    }

    @Test
    void exportPaymentsByTpp_successPagination() {
        // Given
        when(pisCommonPaymentDataSpecification.byTppIdAndCreationPeriodAndPsuIdDataAndInstanceId(TPP_AUTHORISATION_NUMBER,
                                                                                                 CREATION_DATE_FROM,
                                                                                                 CREATION_DATE_TO,
                                                                                                 psuIdData,
                                                                                                 DEFAULT_SERVICE_INSTANCE_ID)).thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        //noinspection unchecked
        when(pisCommonPaymentDataRepository.findAll(any(Specification.class), eq(PageRequest.of(PAGE_INDEX, ITEMS_PER_PAGE))))
            .thenReturn(new PageImpl<>(Collections.singletonList(buildPisCommonPaymentData()), PageRequest.of(PAGE_INDEX, ITEMS_PER_PAGE), 1));
        CmsPayment expectedPayment = buildCmsPayment();
        when(cmsPsuPisMapper.mapPaymentDataToCmsPayment(buildPisCommonPaymentData()))
            .thenReturn(buildCmsPayment());

        // When
        PageData<Collection<CmsPayment>> payments =
            cmsAspspPisExportServiceInternal.exportPaymentsByTpp(TPP_AUTHORISATION_NUMBER, CREATION_DATE_FROM,
                                                                 CREATION_DATE_TO, psuIdData, DEFAULT_SERVICE_INSTANCE_ID, PAGE_INDEX, ITEMS_PER_PAGE);

        // Then
        assertFalse(payments.getData().isEmpty());
        assertTrue(payments.getData().contains(expectedPayment));
        verify(pisCommonPaymentDataSpecification, times(1))
            .byTppIdAndCreationPeriodAndPsuIdDataAndInstanceId(TPP_AUTHORISATION_NUMBER, CREATION_DATE_FROM,
                                                               CREATION_DATE_TO, psuIdData, DEFAULT_SERVICE_INSTANCE_ID);
    }

    @Test
    void exportPaymentsByTpp_failure_wrongTppAuthorisationNumber() {
        // Given
        when(pisCommonPaymentDataSpecification.byTppIdAndCreationPeriodAndPsuIdDataAndInstanceId(WRONG_TPP_AUTHORISATION_NUMBER,
                                                                                                 CREATION_DATE_FROM,
                                                                                                 CREATION_DATE_TO,
                                                                                                 psuIdData,
                                                                                                 DEFAULT_SERVICE_INSTANCE_ID)).thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        //noinspection unchecked
        when(pisCommonPaymentDataRepository.findAll(any(Specification.class), eq(Pageable.unpaged())))
            .thenReturn(new PageImpl<>(Collections.emptyList(), PageRequest.of(PAGE_INDEX, ITEMS_PER_PAGE), 0));

        // When
        PageData<Collection<CmsPayment>> payments =
            cmsAspspPisExportServiceInternal.exportPaymentsByTpp(WRONG_TPP_AUTHORISATION_NUMBER, CREATION_DATE_FROM,
                                                                 CREATION_DATE_TO, psuIdData, DEFAULT_SERVICE_INSTANCE_ID, null, null);

        // Then
        assertTrue(payments.getData().isEmpty());
        verify(pisCommonPaymentDataSpecification, times(1))
            .byTppIdAndCreationPeriodAndPsuIdDataAndInstanceId(WRONG_TPP_AUTHORISATION_NUMBER, CREATION_DATE_FROM,
                                                               CREATION_DATE_TO, psuIdData, DEFAULT_SERVICE_INSTANCE_ID);
    }

    @Test
    void exportPaymentsByTpp_failure_nullTppAuthorisationNumber() {
        // Given

        // When
        PageData<Collection<CmsPayment>> payments =
            cmsAspspPisExportServiceInternal.exportPaymentsByTpp(null, CREATION_DATE_FROM,
                                                                 CREATION_DATE_TO, psuIdData, DEFAULT_SERVICE_INSTANCE_ID, PAGE_INDEX, ITEMS_PER_PAGE);

        // Then
        assertTrue(payments.getData().isEmpty());
        verify(pisCommonPaymentDataSpecification, never())
            .byTppIdAndCreationPeriodAndPsuIdDataAndInstanceId(WRONG_TPP_AUTHORISATION_NUMBER, CREATION_DATE_FROM,
                                                               CREATION_DATE_TO, psuIdData, DEFAULT_SERVICE_INSTANCE_ID);
    }

    @Test
    void exportPaymentsByPsu_success() {
        // Given
        when(pisCommonPaymentDataSpecification.byPsuIdDataAndCreationPeriodAndInstanceId(psuIdData,
                                                                                         CREATION_DATE_FROM,
                                                                                         CREATION_DATE_TO,
                                                                                         DEFAULT_SERVICE_INSTANCE_ID)).thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        //noinspection unchecked
        when(pisCommonPaymentDataRepository.findAll(any(Specification.class), eq(Pageable.unpaged())))
            .thenReturn(new PageImpl<>(Collections.singletonList(buildPisCommonPaymentData()), PageRequest.of(0, ITEMS_PER_PAGE), 1));
        CmsPayment expectedPayment = buildCmsPayment();
        when(cmsPsuPisMapper.mapPaymentDataToCmsPayment(buildPisCommonPaymentData()))
            .thenReturn(buildCmsPayment());

        // When
        PageData<Collection<CmsPayment>> payments =
            cmsAspspPisExportServiceInternal.exportPaymentsByPsu(psuIdData, CREATION_DATE_FROM,
                                                                 CREATION_DATE_TO, DEFAULT_SERVICE_INSTANCE_ID, null, null);

        // Then
        assertFalse(payments.getData().isEmpty());
        assertTrue(payments.getData().contains(expectedPayment));
        verify(pisCommonPaymentDataSpecification, times(1))
            .byPsuIdDataAndCreationPeriodAndInstanceId(psuIdData, CREATION_DATE_FROM,
                                                       CREATION_DATE_TO, DEFAULT_SERVICE_INSTANCE_ID);
    }

    @Test
    void exportPaymentsByPsu_successPagination() {
        // Given
        when(pisCommonPaymentDataSpecification.byPsuIdDataAndCreationPeriodAndInstanceId(psuIdData,
                                                                                         CREATION_DATE_FROM,
                                                                                         CREATION_DATE_TO,
                                                                                         DEFAULT_SERVICE_INSTANCE_ID)).thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        //noinspection unchecked
        when(pisCommonPaymentDataRepository.findAll(any(Specification.class), eq(PageRequest.of(PAGE_INDEX, ITEMS_PER_PAGE))))
            .thenReturn(new PageImpl<>(Collections.singletonList(buildPisCommonPaymentData()), PageRequest.of(PAGE_INDEX, ITEMS_PER_PAGE), 1));
        CmsPayment expectedPayment = buildCmsPayment();
        when(cmsPsuPisMapper.mapPaymentDataToCmsPayment(buildPisCommonPaymentData()))
            .thenReturn(buildCmsPayment());

        // When
        PageData<Collection<CmsPayment>> payments =
            cmsAspspPisExportServiceInternal.exportPaymentsByPsu(psuIdData, CREATION_DATE_FROM,
                                                                 CREATION_DATE_TO, DEFAULT_SERVICE_INSTANCE_ID, PAGE_INDEX, ITEMS_PER_PAGE);

        // Then
        assertFalse(payments.getData().isEmpty());
        assertTrue(payments.getData().contains(expectedPayment));
        verify(pisCommonPaymentDataSpecification, times(1))
            .byPsuIdDataAndCreationPeriodAndInstanceId(psuIdData, CREATION_DATE_FROM,
                                                       CREATION_DATE_TO, DEFAULT_SERVICE_INSTANCE_ID);
    }

    @Test
    void exportPaymentsByPsu_failure_wrongPsuIdData() {
        // Given
        when(pisCommonPaymentDataSpecification.byPsuIdDataAndCreationPeriodAndInstanceId(wrongPsuIdData,
                                                                                         CREATION_DATE_FROM,
                                                                                         CREATION_DATE_TO,
                                                                                         DEFAULT_SERVICE_INSTANCE_ID)).thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        //noinspection unchecked
        when(pisCommonPaymentDataRepository.findAll(any(Specification.class), eq(Pageable.unpaged())))
            .thenReturn(new PageImpl<>(Collections.emptyList(), PageRequest.of(0, ITEMS_PER_PAGE), 0));

        // When
        PageData<Collection<CmsPayment>> payments =
            cmsAspspPisExportServiceInternal.exportPaymentsByPsu(wrongPsuIdData, CREATION_DATE_FROM,
                                                                 CREATION_DATE_TO, DEFAULT_SERVICE_INSTANCE_ID, null, null);

        // Then
        assertTrue(payments.getData().isEmpty());
        verify(pisCommonPaymentDataSpecification, times(1))
            .byPsuIdDataAndCreationPeriodAndInstanceId(wrongPsuIdData, CREATION_DATE_FROM,
                                                       CREATION_DATE_TO, DEFAULT_SERVICE_INSTANCE_ID);
    }

    @Test
    void exportPaymentsByPsu_failure_nullPsuIdData() {
        // When
        PageData<Collection<CmsPayment>> payments =
            cmsAspspPisExportServiceInternal.exportPaymentsByPsu(null, CREATION_DATE_FROM,
                                                                 CREATION_DATE_TO, DEFAULT_SERVICE_INSTANCE_ID, PAGE_INDEX, ITEMS_PER_PAGE);

        // Then
        assertTrue(payments.getData().isEmpty());
        verify(pisCommonPaymentDataSpecification, never())
            .byPsuIdDataAndCreationPeriodAndInstanceId(any(), any(), any(), any());
    }

    @Test
    void exportPaymentsByPsu_failure_emptyPsuIdData() {
        // Given
        PsuIdData emptyPsuIdData = buildEmptyPsuIdData();

        // When
        PageData<Collection<CmsPayment>> payments =
            cmsAspspPisExportServiceInternal.exportPaymentsByPsu(emptyPsuIdData, CREATION_DATE_FROM,
                                                                 CREATION_DATE_TO, DEFAULT_SERVICE_INSTANCE_ID, PAGE_INDEX, ITEMS_PER_PAGE);

        // Then
        assertTrue(payments.getData().isEmpty());
        verify(pisCommonPaymentDataSpecification, never())
            .byPsuIdDataAndCreationPeriodAndInstanceId(any(), any(), any(), any());
    }

    @Test
    void exportPaymentsByAccountId_success() {
        // Given
        when(pisCommonPaymentDataSpecification.byAspspAccountIdAndCreationPeriodAndInstanceId(ASPSP_ACCOUNT_ID,
                                                                                              CREATION_DATE_FROM,
                                                                                              CREATION_DATE_TO,
                                                                                              DEFAULT_SERVICE_INSTANCE_ID)).thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        //noinspection unchecked
        when(pisCommonPaymentDataRepository.findAll(any(Specification.class), eq(Pageable.unpaged())))
            .thenReturn(new PageImpl<>(Collections.singletonList(buildPisCommonPaymentData()), PageRequest.of(PAGE_INDEX, ITEMS_PER_PAGE), 1));
        CmsPayment expectedPayment = buildCmsPayment();
        when(cmsPsuPisMapper.mapPaymentDataToCmsPayment(buildPisCommonPaymentData()))
            .thenReturn(buildCmsPayment());

        // When
        PageData<Collection<CmsPayment>> payments =
            cmsAspspPisExportServiceInternal.exportPaymentsByAccountId(ASPSP_ACCOUNT_ID, CREATION_DATE_FROM,
                                                                       CREATION_DATE_TO, DEFAULT_SERVICE_INSTANCE_ID, null, null);

        // Then
        assertFalse(payments.getData().isEmpty());
        assertTrue(payments.getData().contains(expectedPayment));
        verify(pisCommonPaymentDataSpecification, times(1))
            .byAspspAccountIdAndCreationPeriodAndInstanceId(ASPSP_ACCOUNT_ID, CREATION_DATE_FROM,
                                                            CREATION_DATE_TO, DEFAULT_SERVICE_INSTANCE_ID);
    }

    @Test
    void exportPaymentsByAccountId_successPagination() {
        // Given
        when(pisCommonPaymentDataSpecification.byAspspAccountIdAndCreationPeriodAndInstanceId(ASPSP_ACCOUNT_ID,
                                                                                              CREATION_DATE_FROM,
                                                                                              CREATION_DATE_TO,
                                                                                              DEFAULT_SERVICE_INSTANCE_ID)).thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        //noinspection unchecked
        when(pisCommonPaymentDataRepository.findAll(any(Specification.class), eq(PageRequest.of(PAGE_INDEX, ITEMS_PER_PAGE))))
            .thenReturn(new PageImpl<>(Collections.singletonList(buildPisCommonPaymentData()), PageRequest.of(PAGE_INDEX, ITEMS_PER_PAGE), 1));
        CmsPayment expectedPayment = buildCmsPayment();
        when(cmsPsuPisMapper.mapPaymentDataToCmsPayment(buildPisCommonPaymentData()))
            .thenReturn(buildCmsPayment());

        // When
        PageData<Collection<CmsPayment>> payments =
            cmsAspspPisExportServiceInternal.exportPaymentsByAccountId(ASPSP_ACCOUNT_ID, CREATION_DATE_FROM,
                                                                       CREATION_DATE_TO, DEFAULT_SERVICE_INSTANCE_ID, PAGE_INDEX, ITEMS_PER_PAGE);

        // Then
        assertFalse(payments.getData().isEmpty());
        assertTrue(payments.getData().contains(expectedPayment));
        verify(pisCommonPaymentDataSpecification, times(1))
            .byAspspAccountIdAndCreationPeriodAndInstanceId(ASPSP_ACCOUNT_ID, CREATION_DATE_FROM,
                                                            CREATION_DATE_TO, DEFAULT_SERVICE_INSTANCE_ID);
    }

    @Test
    void exportPaymentsByAccountId_failure_wrongAspspAccountId() {
        // Given
        when(pisCommonPaymentDataSpecification.byAspspAccountIdAndCreationPeriodAndInstanceId(WRONG_ASPSP_ACCOUNT_ID,
                                                                                              CREATION_DATE_FROM,
                                                                                              CREATION_DATE_TO,
                                                                                              DEFAULT_SERVICE_INSTANCE_ID)).thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
         when(pisCommonPaymentDataRepository.findAll(any(Specification.class), eq(Pageable.unpaged())))
            .thenReturn(new PageImpl<>(Collections.emptyList(), PageRequest.of(PAGE_INDEX, ITEMS_PER_PAGE), 0));

        // When
        PageData<Collection<CmsPayment>> payments =
            cmsAspspPisExportServiceInternal.exportPaymentsByAccountId(WRONG_ASPSP_ACCOUNT_ID, CREATION_DATE_FROM,
                                                                       CREATION_DATE_TO, DEFAULT_SERVICE_INSTANCE_ID, null, null);

        // Then
        assertTrue(payments.getData().isEmpty());
        verify(pisCommonPaymentDataSpecification, times(1))
            .byAspspAccountIdAndCreationPeriodAndInstanceId(WRONG_ASPSP_ACCOUNT_ID, CREATION_DATE_FROM,
                                                            CREATION_DATE_TO, DEFAULT_SERVICE_INSTANCE_ID);
    }

    @Test
    void exportPaymentsByAccountId_failure_blankAspspAccountId() {
        // When
        PageData<Collection<CmsPayment>> payments =
            cmsAspspPisExportServiceInternal.exportPaymentsByAccountId(" ", CREATION_DATE_FROM,
                                                                       CREATION_DATE_TO, DEFAULT_SERVICE_INSTANCE_ID, PAGE_INDEX, ITEMS_PER_PAGE);

        // Then
        assertTrue(payments.getData().isEmpty());
        verify(pisCommonPaymentDataSpecification, never())
            .byPsuIdDataAndCreationPeriodAndInstanceId(any(), any(), any(), any());
    }

    private PsuIdData buildPsuIdData(String psuId) {
        return new PsuIdData(psuId, null, null, null, null);
    }

    private PsuIdData buildEmptyPsuIdData() {
        return new PsuIdData(null, null, null, null, null);
    }

    private PsuData buildPsuData() {
        return new PsuData(PSU_ID, null, null, null, null);
    }

    private CmsPayment buildCmsPayment() {
        CmsSinglePayment cmsPayment = new CmsSinglePayment(PAYMENT_PRODUCT);
        cmsPayment.setPaymentId(PAYMENT_ID);

        return cmsPayment;
    }

    private PisCommonPaymentData buildPisCommonPaymentData() {
        PisCommonPaymentData pisCommonPaymentData = new PisCommonPaymentData();
        pisCommonPaymentData.setTransactionStatus(TransactionStatus.RCVD);
        pisCommonPaymentData.setPsuDataList(Collections.singletonList(buildPsuData()));
        pisCommonPaymentData.setPaymentType(PaymentType.SINGLE);
        pisCommonPaymentData.setPaymentProduct(PAYMENT_PRODUCT);
        pisCommonPaymentData.setPayments(buildPisPaymentDataListForCommonData());
        pisCommonPaymentData.setPaymentId(PAYMENT_ID);
        pisCommonPaymentData.setCreationTimestamp(OffsetDateTime.of(2018, 10, 10, 10, 10, 10, 10, ZoneOffset.UTC));
        return pisCommonPaymentData;
    }

    private List<PisPaymentData> buildPisPaymentDataListForCommonData() {
        PisPaymentData pisPaymentData = new PisPaymentData();
        pisPaymentData.setPaymentId(PAYMENT_ID);
        pisPaymentData.setAmount(new BigDecimal("1000"));
        pisPaymentData.setCurrency(Currency.getInstance("EUR"));

        return Collections.singletonList(pisPaymentData);
    }
}
