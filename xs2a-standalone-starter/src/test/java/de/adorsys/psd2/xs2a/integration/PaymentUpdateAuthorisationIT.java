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


package de.adorsys.psd2.xs2a.integration;

import de.adorsys.psd2.consent.api.CmsResponse;
import de.adorsys.psd2.starter.Xs2aStandaloneStarter;
import de.adorsys.psd2.xs2a.config.CorsConfigurationProperties;
import de.adorsys.psd2.xs2a.config.WebConfig;
import de.adorsys.psd2.xs2a.config.Xs2aEndpointPathConstant;
import de.adorsys.psd2.xs2a.config.Xs2aInterfaceConfig;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.integration.builder.UrlBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import static org.mockito.BDDMockito.given;

@ActiveProfiles({"integration-test", "mock-qwac"})
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest(
    classes = Xs2aStandaloneStarter.class)
@ContextConfiguration(
    classes = {
        CorsConfigurationProperties.class,
        WebConfig.class,
        Xs2aEndpointPathConstant.class,
        Xs2aInterfaceConfig.class
    })
public class PaymentUpdateAuthorisationIT extends PaymentUpdateAuthorisationBase {
    @Before
    public void setUp() {
        before();
    }

    // Suppress "Tests should include assertions" Sonar rule as assertions are being performed in another method
    @SuppressWarnings("squid:S2699")
    @Test
    public void updatePaymentPsuData_failed_psu_authorisation_psu_request_are_different() throws Exception {
        given(pisAuthorisationServiceEncrypted.updatePisAuthorisationStatus(AUTHORISATION_ID, ScaStatus.FAILED))
            .willReturn(CmsResponse.<Boolean>builder()
                            .payload(true)
                            .build());
        updatePaymentPsuDataAndCheckForPsuCredentialsInvalidResponse(PSU_ID_1, PSU_ID_2);
    }

    // Suppress "Tests should include assertions" Sonar rule as assertions are being performed in another method
    @SuppressWarnings("squid:S2699")
    @Test
    public void updatePaymentPsuData_failed_no_psu_authorisation_no_psu_request() throws Exception {
        updatePaymentPsuDataAndCheckForFormatErrorResponse(null, null);
    }

    @Override
    String buildRequestUrl() {
        return UrlBuilder.buildPaymentUpdateAuthorisationUrl(SINGLE_PAYMENT_TYPE.getValue(), SEPA_PAYMENT_PRODUCT, PAYMENT_ID, AUTHORISATION_ID);
    }
}
