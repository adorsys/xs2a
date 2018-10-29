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

package de.adorsys.aspsp.xs2a.service.authorization.ais;

import de.adorsys.aspsp.xs2a.config.factory.AisScaStageAuthorisationFactory;
import de.adorsys.aspsp.xs2a.domain.consent.AccountConsentAuthorization;
import de.adorsys.aspsp.xs2a.domain.consent.CreateConsentAuthorizationResponse;
import de.adorsys.aspsp.xs2a.domain.consent.UpdateConsentPsuDataReq;
import de.adorsys.aspsp.xs2a.domain.consent.UpdateConsentPsuDataResponse;
import de.adorsys.aspsp.xs2a.service.authorization.ais.stage.AisScaStartAuthorisationStage;
import de.adorsys.aspsp.xs2a.service.consent.Xs2aAisConsentService;
import de.adorsys.aspsp.xs2a.service.mapper.consent.Xs2aAisConsentMapper;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Optional;

import static de.adorsys.aspsp.xs2a.config.factory.AisScaStageAuthorisationFactory.SERVICE_PREFIX;
import static de.adorsys.aspsp.xs2a.domain.consent.ConsentAuthorizationResponseLinkType.START_AUTHORISATION_WITH_PSU_AUTHENTICATION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class EmbeddedAisAuthorizationServiceTest {
    private static final ScaStatus STARTED_SCA_STATUS = ScaStatus.STARTED;
    private static final ScaStatus STARTED_XS2A_SCA_STATUS = ScaStatus.STARTED;
    private static final String PSU_ID = "Test psuId";
    private static final String CONSENT_ID = "Test consentId";
    private static final String AUTHORISATION_ID = "Test authorisationId";

    @InjectMocks
    private EmbeddedAisAuthorizationService authorizationService;

    @Mock
    private Xs2aAisConsentService aisConsentService;
    @Mock
    private Xs2aAisConsentMapper aisConsentMapper;
    @Mock
    private AisScaStageAuthorisationFactory scaStageAuthorisationFactory;
    @Mock
    private AisScaStartAuthorisationStage startAuthorisationStage;
    @Mock
    private UpdateConsentPsuDataReq updateConsentPsuDataRequest;
    @Mock
    private AccountConsentAuthorization consentAuthorization;
    @Mock
    private UpdateConsentPsuDataResponse updateConsentPsuDataResponse;

    @Before
    public void setUp() {
        when(consentAuthorization.getScaStatus())
            .thenReturn(STARTED_SCA_STATUS);

        when(scaStageAuthorisationFactory.getService(SERVICE_PREFIX + STARTED_SCA_STATUS.name()))
            .thenReturn(startAuthorisationStage);

        when(startAuthorisationStage.apply(updateConsentPsuDataRequest))
            .thenReturn(updateConsentPsuDataResponse);
    }

    @Test
    public void createConsentAuthorization_Success() {
        when(aisConsentService.createAisConsentAuthorization(CONSENT_ID, STARTED_XS2A_SCA_STATUS, PSU_ID))
            .thenReturn(Optional.of(AUTHORISATION_ID));

        Optional<CreateConsentAuthorizationResponse> actualResponseOptional = authorizationService.createConsentAuthorization(PSU_ID, CONSENT_ID);

        assertThat(actualResponseOptional.isPresent()).isTrue();

        CreateConsentAuthorizationResponse actualResponse = actualResponseOptional.get();

        assertThat(actualResponse.getConsentId()).isEqualTo(CONSENT_ID);
        assertThat(actualResponse.getAuthorizationId()).isEqualTo(AUTHORISATION_ID);
        assertThat(actualResponse.getScaStatus()).isEqualTo(STARTED_SCA_STATUS);
        assertThat(actualResponse.getResponseLinkType()).isEqualTo(START_AUTHORISATION_WITH_PSU_AUTHENTICATION);
    }

    @Test
    public void updateConsentPsuData_Failure_ResponseWithError() {
        when(updateConsentPsuDataResponse.hasError())
            .thenReturn(true);

        authorizationService.updateConsentPsuData(updateConsentPsuDataRequest, consentAuthorization);

        verify(aisConsentService, times(0)).updateConsentAuthorization(any(UpdateConsentPsuDataReq.class));
        assertThat(updateConsentPsuDataResponse).isNotNull();
    }

    @Test
    public void updateConsentPsuData_Success_ResponseWithoutError() {
        when(updateConsentPsuDataResponse.hasError())
            .thenReturn(false);

        when(aisConsentMapper.mapToSpiUpdateConsentPsuDataReq(updateConsentPsuDataResponse, updateConsentPsuDataRequest))
            .thenReturn(updateConsentPsuDataRequest);

        doNothing()
            .when(aisConsentService).updateConsentAuthorization(updateConsentPsuDataRequest);

        authorizationService.updateConsentPsuData(updateConsentPsuDataRequest, consentAuthorization);

        verify(aisConsentService).updateConsentAuthorization(updateConsentPsuDataRequest);
        assertThat(updateConsentPsuDataResponse).isNotNull();
    }
}
