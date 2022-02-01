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


package de.adorsys.psd2.xs2a.integration;

import de.adorsys.psd2.consent.api.CmsResponse;
import de.adorsys.psd2.starter.Xs2aStandaloneStarter;
import de.adorsys.psd2.xs2a.config.CorsConfigurationProperties;
import de.adorsys.psd2.xs2a.config.WebConfig;
import de.adorsys.psd2.xs2a.config.Xs2aEndpointPathConstant;
import de.adorsys.psd2.xs2a.config.Xs2aInterfaceConfig;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.integration.builder.UrlBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.mockito.BDDMockito.given;

@ActiveProfiles({"integration-test", "mock-qwac"})
@ExtendWith(SpringExtension.class)
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
class PaymentUpdateAuthorisationIT extends PaymentUpdateAuthorisationBase {
    @BeforeEach
    void setUp() {
        before();
    }

    // Suppress "Tests should include assertions" Sonar rule as assertions are being performed in another method
    @SuppressWarnings("squid:S2699")
    @Test
    void updatePaymentPsuData_failed_psu_authorisation_psu_request_are_different() throws Exception {
        given(authorisationServiceEncrypted.updateAuthorisationStatus(AUTHORISATION_ID, ScaStatus.FAILED))
            .willReturn(CmsResponse.<Boolean>builder()
                            .payload(true)
                            .build());
        updatePaymentPsuDataAndCheckForPsuCredentialsInvalidResponse(PSU_ID_1, PSU_ID_2);
    }

    // Suppress "Tests should include assertions" Sonar rule as assertions are being performed in another method
    @SuppressWarnings("squid:S2699")
    @Test
    void updatePaymentPsuData_failed_no_psu_authorisation_no_psu_request() throws Exception {
        updatePaymentPsuDataAndCheckForFormatErrorResponse(null, null);
    }

    @Override
    String buildRequestUrl() {
        return UrlBuilder.buildPaymentUpdateAuthorisationUrl(SINGLE_PAYMENT_TYPE.getValue(), SEPA_PAYMENT_PRODUCT, PAYMENT_ID, AUTHORISATION_ID);
    }
}
