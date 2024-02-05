/*
 * Copyright 2018-2024 adorsys GmbH & Co KG
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
 * contact us at sales@adorsys.com.
 */

package de.adorsys.psd2.xs2a.web.controller.tb;

import de.adorsys.psd2.model.TrustedBeneficiariesList;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.account.Xs2aTrustedBeneficiariesList;
import de.adorsys.psd2.xs2a.service.ais.TrustedBeneficiariesService;
import de.adorsys.psd2.xs2a.service.mapper.ResponseMapper;
import de.adorsys.psd2.xs2a.service.mapper.TrustedBeneficiariesModelMapper;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ResponseErrorMapper;
import de.adorsys.psd2.xs2a.web.controller.util.RequestUriHandler;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletRequest;

import static de.adorsys.psd2.xs2a.core.domain.TppMessageInformation.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrustedBeneficiariesControllerTest {

    private static final String ACCOUNT_ID = "33333-999999999";
    private static final String WRONG_ACCOUNT_ID = "Wrong account id";
    private static final String CONSENT_ID = "12345";
    private static final String BENEFICIARIES_JSON = "json/service/mapper/trusted-beneficiaries-model-mapper/trusted-beneficiaries-list.json";
    private static final String XS2A_BENEFICIARIES_JSON = "json/service/mapper/trusted-beneficiaries-model-mapper/xs2a-trusted-beneficiaries-list.json";
    private static final String REQUEST_URI = "/accounts";
    private static final String WRONG_CONSENT_ID = "Wrong consent id";
    private static final MessageError MESSAGE_ERROR_AIS_404 = new MessageError(ErrorType.AIS_404, of(MessageErrorCode.RESOURCE_UNKNOWN_404));

    @InjectMocks
    private TrustedBeneficiariesController trustedBeneficiariesController;

    @Mock
    private ResponseMapper responseMapper;
    @Mock
    private HttpServletRequest request;
    @Mock
    private ResponseErrorMapper responseErrorMapper;
    @Mock
    private TrustedBeneficiariesModelMapper trustedBeneficiariesModelMapper;
    @Mock
    private TrustedBeneficiariesService trustedBeneficiariesService;
    @Mock
    private RequestUriHandler requestUriHandler;

    private final JsonReader jsonReader = new JsonReader();

    @Test
    void getTrustedBeneficiaries_Fail() {
        // Given
        ResponseObject<Xs2aTrustedBeneficiariesList> xs2aResponse = ResponseObject.<Xs2aTrustedBeneficiariesList>builder()
                                                                        .fail(MESSAGE_ERROR_AIS_404)
                                                                        .build();

        when(trustedBeneficiariesService.getTrustedBeneficiaries(WRONG_CONSENT_ID, WRONG_ACCOUNT_ID, REQUEST_URI)).thenReturn(xs2aResponse);
        when(request.getRequestURI()).thenReturn(REQUEST_URI);
        when(requestUriHandler.trimEndingSlash(REQUEST_URI)).thenReturn(REQUEST_URI);
        when(responseErrorMapper.generateErrorResponse(MESSAGE_ERROR_AIS_404))
            .thenReturn(new ResponseEntity<>(HttpStatus.NOT_FOUND));

        // When
        ResponseEntity<?> actual = trustedBeneficiariesController.listOfTrustedBeneficiaries(WRONG_ACCOUNT_ID, null, WRONG_CONSENT_ID, null, null);
        // Then
        assertThat(actual.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void getTrustedBeneficiaries_ResultTest() {
        // Given
        Xs2aTrustedBeneficiariesList xs2aBeneficiaries = jsonReader.getObjectFromFile(XS2A_BENEFICIARIES_JSON, Xs2aTrustedBeneficiariesList.class);
        ResponseObject<Xs2aTrustedBeneficiariesList> xs2aResponse = ResponseObject.<Xs2aTrustedBeneficiariesList>builder()
                                                                        .body(xs2aBeneficiaries)
                                                                        .build();

        when(trustedBeneficiariesService.getTrustedBeneficiaries(CONSENT_ID, ACCOUNT_ID, REQUEST_URI)).thenReturn(xs2aResponse);
        when(request.getRequestURI()).thenReturn(REQUEST_URI);
        when(requestUriHandler.trimEndingSlash(REQUEST_URI)).thenReturn(REQUEST_URI);

        TrustedBeneficiariesList expected = jsonReader.getObjectFromFile(BENEFICIARIES_JSON, TrustedBeneficiariesList.class);

        doReturn(new ResponseEntity<>(expected, HttpStatus.OK)).when(responseMapper).ok(any(), any());

        // When
        TrustedBeneficiariesList actual = (TrustedBeneficiariesList) trustedBeneficiariesController.listOfTrustedBeneficiaries(ACCOUNT_ID, null, CONSENT_ID, null, null).getBody();
        // Then
        assertThat(actual).isEqualTo(expected);
    }
}
