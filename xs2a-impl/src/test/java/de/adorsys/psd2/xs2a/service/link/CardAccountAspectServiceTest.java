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
import de.adorsys.psd2.core.data.ais.AisConsent;
import de.adorsys.psd2.core.data.ais.AisConsentData;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.account.*;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aCreatePisCancellationAuthorisationResponse;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.psd2.xs2a.web.link.CardAccountDetailsLinks;
import de.adorsys.psd2.xs2a.web.link.TransactionsReportCardDownloadLinks;
import de.adorsys.psd2.xs2a.web.link.TransactionsReportCardLinks;
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
class CardAccountAspectServiceTest {

    @InjectMocks
    private CardAccountAspectService service;

    @Mock
    private AspspProfileServiceWrapper aspspProfileServiceWrapper;
    @Mock Xs2aCardTransactionsReportByPeriodRequest request;

    private Xs2aCardAccountDetails accountDetails;
    private AisConsent aisConsent;
    private ResponseObject responseObject;
    private JsonReader jsonReader = new JsonReader();


    @BeforeEach
    void setUp() {
        aisConsent = jsonReader.getObjectFromFile("json/aspect/ais-consent.json", AisConsent.class);
        aisConsent.setConsentData(AisConsentData.buildDefaultAisConsentData());

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
                             .body(new Xs2aCardAccountDetailsHolder(accountDetails, aisConsent))
                             .build();
        // When
        ResponseObject actualResponse = service.getCardAccountDetails(responseObject);

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
        ResponseObject actualResponse = service.getCardAccountDetails(responseObject);

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
                             .body(new Xs2aCardAccountListHolder(Collections.singletonList(accountDetails), aisConsent))
                             .build();
        // When
        ResponseObject actualResponse = service.getCardAccountList(responseObject);

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
        ResponseObject actualResponse = service.getCardAccountList(responseObject);

        // Then
        assertTrue(actualResponse.hasError());
    }

    @Test
    void getTransactionsReportByPeriod_success() {
        AspspSettings aspspSettings = jsonReader.getObjectFromFile("json/aspect/aspsp-settings.json", AspspSettings.class);
        when(aspspProfileServiceWrapper.isForceXs2aBaseLinksUrl())
            .thenReturn(aspspSettings.getCommon().isForceXs2aBaseLinksUrl());
        when(aspspProfileServiceWrapper.getXs2aBaseLinksUrl())
            .thenReturn(aspspSettings.getCommon().getXs2aBaseLinksUrl());

        responseObject = ResponseObject.<Xs2aCardTransactionsReport>builder()
                             .body(new Xs2aCardTransactionsReport())
                             .build();

        ResponseObject actualResponse = service.getTransactionsReportByPeriod(responseObject, request);

        assertNotNull(((Xs2aCardTransactionsReport)actualResponse.getBody()).getLinks());
        assertTrue(((Xs2aCardTransactionsReport)actualResponse.getBody()).getLinks() instanceof TransactionsReportCardDownloadLinks);

        assertNull(((Xs2aCardTransactionsReport)actualResponse.getBody()).getCardAccountReport());

        assertFalse(actualResponse.hasError());
    }

    @Test
    void getTransactionsReportByPeriod_cardAccountReportNotNull_success() {
        AspspSettings aspspSettings = jsonReader.getObjectFromFile("json/aspect/aspsp-settings.json", AspspSettings.class);
        when(aspspProfileServiceWrapper.isForceXs2aBaseLinksUrl())
            .thenReturn(aspspSettings.getCommon().isForceXs2aBaseLinksUrl());
        when(aspspProfileServiceWrapper.getXs2aBaseLinksUrl())
            .thenReturn(aspspSettings.getCommon().getXs2aBaseLinksUrl());

        Xs2aCardTransactionsReport xs2aCardTransactionsReport = new Xs2aCardTransactionsReport();
        xs2aCardTransactionsReport.setCardAccountReport(new Xs2aCardAccountReport(Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), null));
        responseObject = ResponseObject.<Xs2aCardTransactionsReport>builder()
                             .body(xs2aCardTransactionsReport)
                             .build();

        ResponseObject actualResponse = service.getTransactionsReportByPeriod(responseObject, request);

        assertNotNull(((Xs2aCardTransactionsReport)actualResponse.getBody()).getLinks());
        assertTrue(((Xs2aCardTransactionsReport)actualResponse.getBody()).getLinks() instanceof TransactionsReportCardDownloadLinks);

        assertNotNull(((Xs2aCardTransactionsReport)actualResponse.getBody()).getCardAccountReport().getLinks());
        assertTrue(((Xs2aCardTransactionsReport)actualResponse.getBody()).getCardAccountReport().getLinks() instanceof TransactionsReportCardLinks);

        assertFalse(actualResponse.hasError());
    }

    @Test
    void getTransactionsReportByPeriod_shouldAddTextErrorMessage() {
        responseObject = ResponseObject.<Xs2aCreatePisCancellationAuthorisationResponse>builder()
                             .fail(AIS_400, of(CONSENT_UNKNOWN_400))
                             .build();

        ResponseObject actualResponse = service.getTransactionsReportByPeriod(responseObject, request);

        assertTrue(actualResponse.hasError());
    }
}
