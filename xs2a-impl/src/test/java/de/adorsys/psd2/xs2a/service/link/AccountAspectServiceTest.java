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

package de.adorsys.psd2.xs2a.service.link;

import de.adorsys.psd2.aspsp.profile.domain.AspspSettings;
import de.adorsys.psd2.core.data.AccountAccess;
import de.adorsys.psd2.core.data.ais.AisConsent;
import de.adorsys.psd2.core.data.ais.AisConsentData;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.account.Xs2aAccountDetails;
import de.adorsys.psd2.xs2a.domain.account.Xs2aAccountDetailsHolder;
import de.adorsys.psd2.xs2a.domain.account.Xs2aAccountListHolder;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aCreatePisCancellationAuthorisationResponse;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.psd2.xs2a.web.link.AccountDetailsLinks;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static de.adorsys.psd2.xs2a.core.domain.TppMessageInformation.of;
import static de.adorsys.psd2.xs2a.core.error.ErrorType.AIS_400;
import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.CONSENT_UNKNOWN_400;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountAspectServiceTest {

    @InjectMocks
    private AccountAspectService service;

    @Mock
    private AspspProfileServiceWrapper aspspProfileServiceWrapper;

    private Xs2aAccountDetails accountDetails;
    private AisConsent aisConsent;
    private ResponseObject responseObject;
    private JsonReader jsonReader = new JsonReader();

    @BeforeEach
    void setUp() {
        aisConsent = jsonReader.getObjectFromFile("json/aspect/ais-consent.json", AisConsent.class);
        AccountAccess accountAccess = jsonReader.getObjectFromFile("json/aspect/account-access.json", AccountAccess.class);
        aisConsent.setTppAccountAccesses(accountAccess);
        aisConsent.setAspspAccountAccesses(accountAccess);

        aisConsent.setConsentData(new AisConsentData(null, null, null,
                                                     false));

        accountDetails = jsonReader.getObjectFromFile("json/aspect/account_details.json", Xs2aAccountDetails.class);
    }

    @Test
    void getAccountDetailsAspect_success() {
        // Given
        AspspSettings aspspSettings = jsonReader.getObjectFromFile("json/aspect/aspsp-settings.json", AspspSettings.class);
        when(aspspProfileServiceWrapper.isForceXs2aBaseLinksUrl())
            .thenReturn(aspspSettings.getCommon().isForceXs2aBaseLinksUrl());
        when(aspspProfileServiceWrapper.getXs2aBaseLinksUrl())
            .thenReturn(aspspSettings.getCommon().getXs2aBaseLinksUrl());

        responseObject = ResponseObject.<Xs2aAccountDetailsHolder>builder()
                             .body(new Xs2aAccountDetailsHolder(accountDetails, aisConsent))
                             .build();
        // When
        ResponseObject actualResponse = service.getAccountDetailsAspect(responseObject);

        // Then
        assertNotNull(accountDetails.getLinks());
        assertTrue(accountDetails.getLinks() instanceof AccountDetailsLinks);

        assertFalse(actualResponse.hasError());
    }

    @Test
    void getAccountDetailsAspect_withError_shouldAddTextErrorMessage() {
        // Given
        responseObject = ResponseObject.<Xs2aCreatePisCancellationAuthorisationResponse>builder()
                             .fail(AIS_400, of(CONSENT_UNKNOWN_400))
                             .build();
        // When
        ResponseObject actualResponse = service.getAccountDetailsAspect(responseObject);

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

        responseObject = ResponseObject.<Xs2aAccountListHolder>builder()
                             .body(new Xs2aAccountListHolder(Collections.singletonList(accountDetails), aisConsent))
                             .build();
        // When
        ResponseObject actualResponse = service.getAccountDetailsListAspect(responseObject);

        // Then
        assertNotNull(accountDetails.getLinks());
        assertTrue(accountDetails.getLinks() instanceof AccountDetailsLinks);
        assertFalse(actualResponse.hasError());
    }

    @Test
    void getAccountDetailsListAspect_withError_shouldAddTextErrorMessage() {
        // Given
        responseObject = ResponseObject.<Xs2aCreatePisCancellationAuthorisationResponse>builder()
                             .fail(AIS_400, of(CONSENT_UNKNOWN_400))
                             .build();
        // When
        ResponseObject actualResponse = service.getAccountDetailsListAspect(responseObject);

        // Then
        assertTrue(actualResponse.hasError());
    }
}
