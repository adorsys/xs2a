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
/*


package de.adorsys.aspsp.xs2a.service.authorization.ais.stage;

import de.adorsys.aspsp.xs2a.domain.MessageErrorCode;
import de.adorsys.aspsp.xs2a.domain.consent.UpdateConsentPsuDataReq;
import de.adorsys.aspsp.xs2a.domain.consent.UpdateConsentPsuDataResponse;
import de.adorsys.aspsp.xs2a.service.consent.AisConsentDataService;
import de.adorsys.aspsp.xs2a.service.consent.AisConsentService;
import de.adorsys.aspsp.xs2a.service.mapper.consent.Xs2aAisConsentMapper;
import de.adorsys.aspsp.xs2a.service.mapper.spi_xs2a_mappers.SpiResponseStatusToXs2aMessageErrorCodeMapper;
import de.adorsys.aspsp.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiPsuDataMapper;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiAccountConsent;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiAuthenticationObject;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiAuthorizationCodeResult;
import de.adorsys.psd2.xs2a.spi.domain.consent.AspspConsentData;
import de.adorsys.psd2.xs2a.spi.domain.psu.SpiPsuData;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponseStatus;
import de.adorsys.psd2.xs2a.spi.service.AisConsentSpi;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static de.adorsys.aspsp.xs2a.domain.consent.ConsentAuthorizationResponseLinkType.START_AUTHORISATION_WITH_TRANSACTION_AUTHORISATION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
TODO Rework it after the task merged to develop https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/466
@RunWith(MockitoJUnitRunner.class)
public class AisScaMethodSelectedStageTest {
    private static final String CONSENT_ID = "Test consentId";
    private static final String TEST_AUTHENTICATION_METHOD_ID = SpiAuthenticationObject.SMS_OTP.name();
    private static final SpiAuthenticationObject SCA_METHOD = SpiAuthenticationObject.SMS_OTP;
    private static final ScaStatus METHOD_SELECTED_SCA_STATUS = ScaStatus.SCAMETHODSELECTED;
    private static final SpiResponseStatus RESPONSE_STATUS = SpiResponseStatus.LOGICAL_FAILURE;
    private static final MessageErrorCode ERROR_CODE = MessageErrorCode.FORMAT_ERROR;
    private static final SpiPsuData SPI_PSU_DATA = new SpiPsuData(null, null, null, null);
    private static final AspspConsentData ASPSP_CONSENT_DATA = new AspspConsentData(new byte[0], "Some Consent ID");

    @InjectMocks
    private AisScaMethodSelectedStage scaMethodSelectedStage;

    @Mock
    private AisConsentService aisConsentService;
    @Mock
    private AisConsentDataService aisConsentDataService;
    @Mock
    private AisConsentSpi aisConsentSpi;
    @Mock
    private Xs2aAisConsentMapper aisConsentMapper;
    @Mock
    private SpiResponseStatusToXs2aMessageErrorCodeMapper messageErrorCodeMapper;
    @Mock
    private Xs2aToSpiPsuDataMapper psuDataMapper;
    @Mock
    private UpdateConsentPsuDataReq request;
    @Mock
    private SpiAccountConsent accountConsent;

    @Before
    public void setUp() {
        when(request.getConsentId())
            .thenReturn(CONSENT_ID);

        when(request.getAuthenticationMethodId())
            .thenReturn(TEST_AUTHENTICATION_METHOD_ID);

        when(aisConsentService.getAccountConsentById(CONSENT_ID))
            .thenReturn(accountConsent);

        when(psuDataMapper.mapToSpiPsuData(any(PsuIdData.class)))
            .thenReturn(SPI_PSU_DATA);

        when(aisConsentDataService.getAspspConsentDataByConsentId(CONSENT_ID))
            .thenReturn(ASPSP_CONSENT_DATA);

        doNothing()
            .when(aisConsentDataService).updateAspspConsentData(ASPSP_CONSENT_DATA);
    }

    @Test
    public void apply_Success() {
        when(aisConsentSpi.requestAuthorisationCode(SPI_PSU_DATA, SCA_METHOD, accountConsent, ASPSP_CONSENT_DATA))
            .thenReturn(buildSuccessSpiResponse());

        UpdateConsentPsuDataResponse actualResponse = scaMethodSelectedStage.apply(request);

        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.getChosenScaMethod()).isEqualTo(TEST_AUTHENTICATION_METHOD_ID);
        assertThat(actualResponse.getScaStatus()).isEqualTo(METHOD_SELECTED_SCA_STATUS);
        assertThat(actualResponse.getResponseLinkType()).isEqualTo(START_AUTHORISATION_WITH_TRANSACTION_AUTHORISATION);
    }

    @Test
    public void apply_Failure_SpiResponseWithError() {
        when(aisConsentSpi.requestAuthorisationCode(SPI_PSU_DATA, SCA_METHOD, accountConsent, ASPSP_CONSENT_DATA))
            .thenReturn(buildErrorSpiResponse());

        when(messageErrorCodeMapper.mapToMessageErrorCode(RESPONSE_STATUS))
            .thenReturn(ERROR_CODE);

        UpdateConsentPsuDataResponse actualResponse = scaMethodSelectedStage.apply(request);

        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.getErrorCode()).isEqualTo(ERROR_CODE);
    }

    // Needed because SpiResponse is final, so it's impossible to mock it
    private SpiResponse<SpiAuthorizationCodeResult> buildSuccessSpiResponse() {
        return SpiResponse.<SpiAuthorizationCodeResult>builder()
                   .payload(new SpiAuthorizationCodeResult())
                   .aspspConsentData(ASPSP_CONSENT_DATA)
                   .success();
    }

    // Needed because SpiResponse is final, so it's impossible to mock it
    private SpiResponse<SpiAuthorizationCodeResult> buildErrorSpiResponse() {
        return SpiResponse.<SpiAuthorizationCodeResult>builder()
                   .aspspConsentData(ASPSP_CONSENT_DATA)
                   .fail(RESPONSE_STATUS);
    }
}
*/
