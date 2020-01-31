/*
 * Copyright 2018-2020 adorsys GmbH & Co KG
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

package de.adorsys.psd2.xs2a.web.aspect;

import de.adorsys.psd2.aspsp.profile.domain.AspspSettings;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.account.Xs2aCardAccountDetails;
import de.adorsys.psd2.xs2a.domain.account.Xs2aCardAccountDetailsHolder;
import de.adorsys.psd2.xs2a.domain.account.Xs2aCardAccountListHolder;
import de.adorsys.psd2.xs2a.domain.consent.AccountConsent;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aCreatePisCancellationAuthorisationResponse;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.psd2.xs2a.web.link.CardAccountDetailsLinks;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static de.adorsys.psd2.xs2a.core.domain.TppMessageInformation.of;
import static de.adorsys.psd2.xs2a.core.error.ErrorType.AIS_400;
import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.CONSENT_UNKNOWN_400;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CardAccountAspectTest {

    @Mock
    private AspspProfileServiceWrapper aspspProfileServiceWrapper;

    private Xs2aCardAccountDetails accountDetails;
    private AccountConsent accountConsent;
    private ResponseObject responseObject;
    private JsonReader jsonReader = new JsonReader();
    private CardAccountAspect aspect;

    @BeforeEach
    void setUp() {
        aspect = new CardAccountAspect(aspspProfileServiceWrapper);
        accountConsent = jsonReader.getObjectFromFile("json/aspect/account_consent.json", AccountConsent.class);
        accountDetails = jsonReader.getObjectFromFile("json/aspect/card_account_details.json", Xs2aCardAccountDetails.class);
    }

    @Test
    void getCardAccountDetailsAspect_success() {
        // Given
        AspspSettings aspspSettings = jsonReader.getObjectFromFile("json/aspect/aspsp-settings.json", AspspSettings.class);
        when(aspspProfileServiceWrapper.isForceXs2aBaseLinksUrl())
            .thenReturn(aspspSettings.getCommon().isForceXs2aBaseLinksUrl());
        when(aspspProfileServiceWrapper.getXs2aBaseLinksUrl())
            .thenReturn(aspspSettings.getCommon().getXs2aBaseLinksUrl());

        responseObject = ResponseObject.<Xs2aCardAccountDetailsHolder>builder()
                             .body(new Xs2aCardAccountDetailsHolder(accountDetails, accountConsent))
                             .build();
        // When
        ResponseObject actualResponse = aspect.getCardAccountDetails(responseObject);

        // Then
        assertNotNull(accountDetails.getLinks());
        assertTrue(accountDetails.getLinks() instanceof CardAccountDetailsLinks);

        assertFalse(actualResponse.hasError());
    }

    @Test
    void getAccountDetailsAspect_withError_shouldAddTextErrorMessage() {
        // Given
        responseObject = ResponseObject.<Xs2aCreatePisCancellationAuthorisationResponse>builder()
                             .fail(AIS_400, of(CONSENT_UNKNOWN_400))
                             .build();
        // When
        ResponseObject actualResponse = aspect.getCardAccountDetails(responseObject);

        // Then
        assertTrue(actualResponse.hasError());
    }

    @Test
    void getAccountDetailsListAspect_success() {
        // Given
        AspspSettings aspspSettings = jsonReader.getObjectFromFile("json/aspect/aspsp-settings.json", AspspSettings.class);
        when(aspspProfileServiceWrapper.isForceXs2aBaseLinksUrl())
            .thenReturn(aspspSettings.getCommon().isForceXs2aBaseLinksUrl());
        when(aspspProfileServiceWrapper.getXs2aBaseLinksUrl())
            .thenReturn(aspspSettings.getCommon().getXs2aBaseLinksUrl());

        responseObject = ResponseObject.<Xs2aCardAccountListHolder>builder()
                             .body(new Xs2aCardAccountListHolder(Collections.singletonList(accountDetails), accountConsent))
                             .build();
        // When
        ResponseObject actualResponse = aspect.getCardAccountList(responseObject);

        // Then
        assertNotNull(accountDetails.getLinks());
        assertTrue(accountDetails.getLinks() instanceof CardAccountDetailsLinks);

        assertFalse(actualResponse.hasError());
    }

    @Test
    void getAccountDetailsListAspect_withError_shouldAddTextErrorMessage() {
        // Given
        responseObject = ResponseObject.<Xs2aCreatePisCancellationAuthorisationResponse>builder()
                             .fail(AIS_400, of(CONSENT_UNKNOWN_400))
                             .build();
        // When
        ResponseObject actualResponse = aspect.getCardAccountList(responseObject);

        // Then
        assertTrue(actualResponse.hasError());
    }
}
