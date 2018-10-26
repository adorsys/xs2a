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

package de.adorsys.aspsp.xs2a.service.authorization.ais.stage;

import de.adorsys.aspsp.xs2a.domain.MessageErrorCode;
import de.adorsys.aspsp.xs2a.domain.consent.UpdateConsentPsuDataReq;
import de.adorsys.aspsp.xs2a.domain.consent.UpdateConsentPsuDataResponse;
import de.adorsys.aspsp.xs2a.service.consent.AisConsentDataService;
import de.adorsys.aspsp.xs2a.service.consent.AisConsentService;
import de.adorsys.aspsp.xs2a.service.mapper.consent.Xs2aAisConsentMapper;
import de.adorsys.aspsp.xs2a.service.mapper.spi_xs2a_mappers.SpiResponseStatusToXs2aMessageErrorCodeMapper;
import de.adorsys.aspsp.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiPsuDataMapper;
import de.adorsys.psd2.consent.api.CmsScaMethod;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiAccountConsent;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiAuthorisationStatus;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiAuthorizationCodeResult;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiScaMethod;
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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static de.adorsys.aspsp.xs2a.domain.consent.ConsentAuthorizationResponseLinkType.START_AUTHORISATION_WITH_AUTHENTICATION_METHOD_SELECTION;
import static de.adorsys.aspsp.xs2a.domain.consent.ConsentAuthorizationResponseLinkType.START_AUTHORISATION_WITH_TRANSACTION_AUTHORISATION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AisScaStartAuthorisationStageTest {
    private static final String CONSENT_ID = "Test consentId";
    private static final String PASSWORD = "Test password";
    private static final String PSU_ID = "Test psuId";
    private static final SpiResponseStatus RESPONSE_STATUS = SpiResponseStatus.LOGICAL_FAILURE;
    private static final ScaStatus FAILED_SCA_STATUS = ScaStatus.FAILED;
    private static final ScaStatus AUTHENTICATED_SCA_STATUS = ScaStatus.PSUAUTHENTICATED;
    private static final ScaStatus METHOD_SELECTED_SCA_STATUS = ScaStatus.SCAMETHODSELECTED;
    private static final MessageErrorCode FORMAT_ERROR_CODE = MessageErrorCode.FORMAT_ERROR;
    private static final MessageErrorCode SCA_METHOD_UNKNOWN_ERROR_CODE = MessageErrorCode.SCA_METHOD_UNKNOWN;
    private static final MessageErrorCode PSU_CREDENTIALS_INVALID_ERROR_CODE = MessageErrorCode.PSU_CREDENTIALS_INVALID;
    private static final SpiPsuData SPI_PSU_DATA = new SpiPsuData(PSU_ID, null, null, null);
    private static final PsuIdData PSU_ID_DATA = new PsuIdData(PSU_ID, null, null, null);
    private static final AspspConsentData ASPSP_CONSENT_DATA = new AspspConsentData(new byte[0], "Some Consent ID");
    private static final List<SpiScaMethod> MULTIPLE_SPI_SCA_METHODS = Arrays.asList(SpiScaMethod.SMS_OTP, SpiScaMethod.PHOTO_OTP);
    private static final List<CmsScaMethod> MULTIPLE_CMS_SCA_METHODS = Arrays.asList(CmsScaMethod.SMS_OTP, CmsScaMethod.PHOTO_OTP);
    private static final List<SpiScaMethod> ONE_SPI_SCA_METHOD = Collections.singletonList(SpiScaMethod.SMS_OTP);
    private static final List<SpiScaMethod> NONE_SPI_SCA_METHOD = Collections.emptyList();

    @InjectMocks
    private AisScaStartAuthorisationStage scaStartAuthorisationStage;

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

        when(aisConsentService.getAccountConsentById(CONSENT_ID))
            .thenReturn(accountConsent);

        when(psuDataMapper.mapToSpiPsuData(any(PsuIdData.class)))
            .thenReturn(SPI_PSU_DATA);

        when(request.getPassword())
            .thenReturn(PASSWORD);

        when(aisConsentDataService.getAspspConsentDataByConsentId(CONSENT_ID))
            .thenReturn(ASPSP_CONSENT_DATA);

        doNothing()
            .when(aisConsentDataService).updateAspspConsentData(ASPSP_CONSENT_DATA);
    }

    @Test
    public void apply_Failure_AuthorisationStatusSpiResponseFailed() {
        when(aisConsentSpi.authorisePsu(SPI_PSU_DATA, PASSWORD, accountConsent, ASPSP_CONSENT_DATA))
            .thenReturn(buildErrorSpiResponse(SpiAuthorisationStatus.FAILURE));

        UpdateConsentPsuDataResponse actualResponse = scaStartAuthorisationStage.apply(request);

        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.getScaStatus()).isEqualTo(FAILED_SCA_STATUS);
        assertThat(actualResponse.getErrorCode()).isEqualTo(PSU_CREDENTIALS_INVALID_ERROR_CODE);
    }

    @Test
    public void apply_MultipleAvailableScaMethods_Success() {
        when(request.getPsuData())
            .thenReturn(PSU_ID_DATA);

        when(aisConsentSpi.authorisePsu(SPI_PSU_DATA, PASSWORD, accountConsent, ASPSP_CONSENT_DATA))
            .thenReturn(buildSuccessSpiResponse(SpiAuthorisationStatus.SUCCESS));

        when(aisConsentSpi.requestAvailableScaMethods(SPI_PSU_DATA, accountConsent, ASPSP_CONSENT_DATA))
            .thenReturn(buildSuccessSpiResponse(MULTIPLE_SPI_SCA_METHODS));

        when(aisConsentMapper.mapToCmsScaMethods(MULTIPLE_SPI_SCA_METHODS))
            .thenReturn(MULTIPLE_CMS_SCA_METHODS);

        UpdateConsentPsuDataResponse actualResponse = scaStartAuthorisationStage.apply(request);

        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.getPsuId()).isEqualTo(PSU_ID);
        assertThat(actualResponse.getAvailableScaMethods()).isEqualTo(MULTIPLE_CMS_SCA_METHODS);
        assertThat(actualResponse.getScaStatus()).isEqualTo(AUTHENTICATED_SCA_STATUS);
        assertThat(actualResponse.getResponseLinkType()).isEqualTo(START_AUTHORISATION_WITH_AUTHENTICATION_METHOD_SELECTION);
    }

    @Test
    public void apply_OneAvailableScaMethod_Success() {
        when(request.getPsuData())
            .thenReturn(PSU_ID_DATA);

        when(aisConsentSpi.authorisePsu(SPI_PSU_DATA, PASSWORD, accountConsent, ASPSP_CONSENT_DATA))
            .thenReturn(buildSuccessSpiResponse(SpiAuthorisationStatus.SUCCESS));

        when(aisConsentSpi.requestAvailableScaMethods(SPI_PSU_DATA, accountConsent, ASPSP_CONSENT_DATA))
            .thenReturn(buildSuccessSpiResponse(ONE_SPI_SCA_METHOD));

        SpiScaMethod scaMethod = ONE_SPI_SCA_METHOD.get(0);

        when(aisConsentSpi.requestAuthorisationCode(SPI_PSU_DATA, scaMethod, accountConsent, ASPSP_CONSENT_DATA))
            .thenReturn(buildSuccessSpiResponse(new SpiAuthorizationCodeResult()));

        UpdateConsentPsuDataResponse actualResponse = scaStartAuthorisationStage.apply(request);

        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.getPsuId()).isEqualTo(PSU_ID);
        assertThat(actualResponse.getChosenScaMethod()).isEqualTo(scaMethod.name());
        assertThat(actualResponse.getScaStatus()).isEqualTo(METHOD_SELECTED_SCA_STATUS);
        assertThat(actualResponse.getResponseLinkType()).isEqualTo(START_AUTHORISATION_WITH_TRANSACTION_AUTHORISATION);
    }

    @Test
    public void apply_OneAvailableScaMethod_Failure_ResponseWithError() {
        when(aisConsentSpi.authorisePsu(SPI_PSU_DATA, PASSWORD, accountConsent, ASPSP_CONSENT_DATA))
            .thenReturn(buildSuccessSpiResponse(SpiAuthorisationStatus.SUCCESS));

        when(aisConsentSpi.requestAvailableScaMethods(SPI_PSU_DATA, accountConsent, ASPSP_CONSENT_DATA))
            .thenReturn(buildSuccessSpiResponse(ONE_SPI_SCA_METHOD));

        SpiScaMethod scaMethod = ONE_SPI_SCA_METHOD.get(0);

        when(aisConsentSpi.requestAuthorisationCode(SPI_PSU_DATA, scaMethod, accountConsent, ASPSP_CONSENT_DATA))
            .thenReturn(buildErrorSpiResponse(new SpiAuthorizationCodeResult()));

        when(messageErrorCodeMapper.mapToMessageErrorCode(RESPONSE_STATUS))
            .thenReturn(FORMAT_ERROR_CODE);

        UpdateConsentPsuDataResponse actualResponse = scaStartAuthorisationStage.apply(request);

        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.getErrorCode()).isEqualTo(FORMAT_ERROR_CODE);
    }

    @Test
    public void apply_NoneAvailableScaMethods_Failure_WrongScenarioAccordingToSpecification() {
        when(request.getPsuData())
            .thenReturn(PSU_ID_DATA);

        when(aisConsentSpi.authorisePsu(SPI_PSU_DATA, PASSWORD, accountConsent, ASPSP_CONSENT_DATA))
            .thenReturn(buildSuccessSpiResponse(SpiAuthorisationStatus.SUCCESS));

        when(aisConsentSpi.requestAvailableScaMethods(SPI_PSU_DATA, accountConsent, ASPSP_CONSENT_DATA))
            .thenReturn(buildSuccessSpiResponse(NONE_SPI_SCA_METHOD));

        UpdateConsentPsuDataResponse actualResponse = scaStartAuthorisationStage.apply(request);

        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.getPsuId()).isEqualTo(PSU_ID);
        assertThat(actualResponse.getScaStatus()).isEqualTo(FAILED_SCA_STATUS);
        assertThat(actualResponse.getErrorCode()).isEqualTo(SCA_METHOD_UNKNOWN_ERROR_CODE);
    }

    // Needed because SpiResponse is final, so it's impossible to mock it
    private <T> SpiResponse<T> buildSuccessSpiResponse(T payload) {
        return SpiResponse.<T>builder()
                   .payload(payload)
                   .aspspConsentData(ASPSP_CONSENT_DATA)
                   .success();
    }

    // Needed because SpiResponse is final, so it's impossible to mock it
    private <T> SpiResponse<T> buildErrorSpiResponse(T payload) {
        return SpiResponse.<T>builder()
                   .payload(payload)
                   .aspspConsentData(ASPSP_CONSENT_DATA)
                   .fail(RESPONSE_STATUS);
    }
}
