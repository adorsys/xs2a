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

import de.adorsys.psd2.consent.api.pis.CmsBasePaymentResponse;
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
        CmsBasePaymentResponse expectedPayment = buildCmsBasePaymentResponse();
        when(cmsPsuPisMapper.mapPaymentDataToCmsPayment(buildPisCommonPaymentData()))
            .thenReturn(buildCmsBasePaymentResponse());

        // When
        PageData<Collection<CmsBasePaymentResponse>> payments =
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
        CmsBasePaymentResponse expectedPayment = buildCmsBasePaymentResponse();
        when(cmsPsuPisMapper.mapPaymentDataToCmsPayment(buildPisCommonPaymentData()))
            .thenReturn(buildCmsBasePaymentResponse());

        // When
        PageData<Collection<CmsBasePaymentResponse>> payments =
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
        PageData<Collection<CmsBasePaymentResponse>> payments =
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
        PageData<Collection<CmsBasePaymentResponse>> payments =
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
        CmsBasePaymentResponse expectedPayment = buildCmsBasePaymentResponse();
        when(cmsPsuPisMapper.mapPaymentDataToCmsPayment(buildPisCommonPaymentData()))
            .thenReturn(buildCmsBasePaymentResponse());

        // When
        PageData<Collection<CmsBasePaymentResponse>> payments =
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
        CmsBasePaymentResponse expectedPayment = buildCmsBasePaymentResponse();
        when(cmsPsuPisMapper.mapPaymentDataToCmsPayment(buildPisCommonPaymentData()))
            .thenReturn(buildCmsBasePaymentResponse());

        // When
        PageData<Collection<CmsBasePaymentResponse>> payments =
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
        PageData<Collection<CmsBasePaymentResponse>> payments =
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
        PageData<Collection<CmsBasePaymentResponse>> payments =
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
        PageData<Collection<CmsBasePaymentResponse>> payments =
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
        CmsBasePaymentResponse expectedPayment = buildCmsBasePaymentResponse();
        when(cmsPsuPisMapper.mapPaymentDataToCmsPayment(buildPisCommonPaymentData()))
            .thenReturn(buildCmsBasePaymentResponse());

        // When
        PageData<Collection<CmsBasePaymentResponse>> payments =
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
        CmsBasePaymentResponse expectedPayment = buildCmsBasePaymentResponse();
        when(cmsPsuPisMapper.mapPaymentDataToCmsPayment(buildPisCommonPaymentData()))
            .thenReturn(buildCmsBasePaymentResponse());

        // When
        PageData<Collection<CmsBasePaymentResponse>> payments =
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
        PageData<Collection<CmsBasePaymentResponse>> payments =
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
        PageData<Collection<CmsBasePaymentResponse>> payments =
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

    private CmsBasePaymentResponse buildCmsBasePaymentResponse() {
        CmsSinglePayment CmsBasePaymentResponse = new CmsSinglePayment(PAYMENT_PRODUCT);
        CmsBasePaymentResponse.setPaymentId(PAYMENT_ID);

        return CmsBasePaymentResponse;
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
