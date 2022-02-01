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

package de.adorsys.psd2.xs2a.service.consent;

import de.adorsys.psd2.consent.api.CmsError;
import de.adorsys.psd2.consent.api.CmsResponse;
import de.adorsys.psd2.consent.api.pis.CreatePisCommonPaymentResponse;
import de.adorsys.psd2.consent.api.pis.PisCommonPaymentResponse;
import de.adorsys.psd2.consent.api.pis.proto.PisPaymentInfo;
import de.adorsys.psd2.consent.api.service.PisCommonPaymentServiceEncrypted;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.core.tpp.TppRole;
import de.adorsys.psd2.xs2a.domain.pis.PaymentInitiationParameters;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class Xs2aPisCommonPaymentServiceTest {
    private static final String PRODUCT = "sepa-credit-transfers";
    private static final String PAYMENT_ID = "d6cb50e5-bb88-4bbf-a5c1-42ee1ed1df2c";
    private static final byte[] PAYMENT_DATA = new byte[16];
    private static final TppInfo TPP_INFO = buildTppInfo();
    private static final PaymentInitiationParameters PAYMENT_INITIATION_PARAMETERS = buildPaymentInitiationParameters();
    private static final PisPaymentInfo PIS_PAYMENT_INFO = buildPisPaymentInfo();
    private static final PsuIdData PSU_DATA = new PsuIdData("psuId", "psuIdType", "psuCorporateId", "psuCorporateIdType", "psuIpAddress");
    private static final CreatePisCommonPaymentResponse CREATE_PIS_COMMON_PAYMENT_RESPONSE = new CreatePisCommonPaymentResponse(PAYMENT_ID, null);
    private static final PisCommonPaymentResponse PIS_COMMON_PAYMENT_RESPONSE = new PisCommonPaymentResponse();

    @InjectMocks
    private Xs2aPisCommonPaymentService xs2aPisCommonPaymentService;

    @Mock
    private PisCommonPaymentServiceEncrypted pisCommonPaymentServiceEncrypted;

    @Test
    void createCommonPayment_by_request_success() {
        //Given
        when(pisCommonPaymentServiceEncrypted.createCommonPayment(PIS_PAYMENT_INFO))
            .thenReturn(CmsResponse.<CreatePisCommonPaymentResponse>builder().payload(CREATE_PIS_COMMON_PAYMENT_RESPONSE).build());

        //When
        CreatePisCommonPaymentResponse actualResponse = xs2aPisCommonPaymentService.createCommonPayment(PIS_PAYMENT_INFO);

        //Then
        assertThat(actualResponse).isEqualTo(CREATE_PIS_COMMON_PAYMENT_RESPONSE);
    }

    @Test
    void createCommonPayment_by_request_failed() {
        //Given
        when(pisCommonPaymentServiceEncrypted.createCommonPayment(PIS_PAYMENT_INFO))
            .thenReturn(CmsResponse.<CreatePisCommonPaymentResponse>builder().error(CmsError.TECHNICAL_ERROR).build());

        //When
        CreatePisCommonPaymentResponse actualResponse = xs2aPisCommonPaymentService.createCommonPayment(PIS_PAYMENT_INFO);

        //Then
        assertThat(actualResponse).isNull();
    }

    @Test
    void getPisCommonPaymentById_success() {
        //Given
        when(pisCommonPaymentServiceEncrypted.getCommonPaymentById(PAYMENT_ID))
            .thenReturn(CmsResponse.<PisCommonPaymentResponse>builder().payload(PIS_COMMON_PAYMENT_RESPONSE).build());

        //When
        Optional<PisCommonPaymentResponse> actualResponse = xs2aPisCommonPaymentService.getPisCommonPaymentById(PAYMENT_ID);

        //Then
        assertThat(actualResponse).isPresent().contains(PIS_COMMON_PAYMENT_RESPONSE);
    }

    @Test
    void getPisCommonPaymentById_failed() {
        //Given
        when(pisCommonPaymentServiceEncrypted.getCommonPaymentById(PAYMENT_ID))
            .thenReturn(CmsResponse.<PisCommonPaymentResponse>builder().error(CmsError.TECHNICAL_ERROR).build());

        //When
        Optional<PisCommonPaymentResponse> actualResponse = xs2aPisCommonPaymentService.getPisCommonPaymentById(PAYMENT_ID);

        //Then
        assertThat(actualResponse).isNotPresent();
    }

    @Test
    void updateMultilevelSca() {
        // Given
        when(pisCommonPaymentServiceEncrypted.updateMultilevelSca(PAYMENT_ID, true))
            .thenReturn(CmsResponse.<Boolean>builder().payload(true).build());

        // When
        boolean actualResponse = xs2aPisCommonPaymentService.updateMultilevelSca(PAYMENT_ID, true);

        // Then
        assertThat(actualResponse).isTrue();
    }

    private static TppInfo buildTppInfo() {
        TppInfo tppInfo = new TppInfo();
        tppInfo.setAuthorisationNumber("registrationNumber");
        tppInfo.setTppName("tppName");
        tppInfo.setTppRoles(Collections.singletonList(TppRole.PISP));
        tppInfo.setAuthorityId("authorityId");
        return tppInfo;
    }

    private static PisPaymentInfo buildPisPaymentInfo() {
        PisPaymentInfo request = new PisPaymentInfo();
        request.setPaymentProduct(PAYMENT_INITIATION_PARAMETERS.getPaymentProduct());
        request.setPaymentType(PAYMENT_INITIATION_PARAMETERS.getPaymentType());
        request.setTransactionStatus(TransactionStatus.RCVD);
        request.setPaymentData(Xs2aPisCommonPaymentServiceTest.PAYMENT_DATA);
        request.setTppInfo(TPP_INFO);
        request.setPsuDataList(Collections.singletonList(PAYMENT_INITIATION_PARAMETERS.getPsuData()));
        return request;
    }

    private static PaymentInitiationParameters buildPaymentInitiationParameters() {
        PaymentInitiationParameters parameters = new PaymentInitiationParameters();
        parameters.setPaymentProduct(PRODUCT);
        parameters.setPaymentType(PaymentType.SINGLE);
        parameters.setPsuData(PSU_DATA);
        return parameters;
    }
}
