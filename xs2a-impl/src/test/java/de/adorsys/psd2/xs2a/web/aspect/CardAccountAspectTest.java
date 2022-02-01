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

package de.adorsys.psd2.xs2a.web.aspect;

import de.adorsys.psd2.core.data.ais.AisConsent;
import de.adorsys.psd2.core.data.ais.AisConsentData;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.account.*;
import de.adorsys.psd2.xs2a.service.link.CardAccountAspectService;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CardAccountAspectTest {
    @InjectMocks
    private CardAccountAspect aspect;

    @Mock
    private CardAccountAspectService cardAccountAspectService;
    @Mock
    private Xs2aCardTransactionsReportByPeriodRequest request;

    private Xs2aCardAccountDetails accountDetails;
    private AisConsent aisConsent;
    private JsonReader jsonReader = new JsonReader();


    @BeforeEach
    void setUp() {
        aisConsent = jsonReader.getObjectFromFile("json/aspect/ais-consent.json", AisConsent.class);

        aisConsent.setConsentData(AisConsentData.buildDefaultAisConsentData());

        accountDetails = jsonReader.getObjectFromFile("json/aspect/card_account_details.json", Xs2aCardAccountDetails.class);
    }

    @Test
    void getCardAccountList() {
        ResponseObject<Xs2aCardAccountListHolder> responseObject = ResponseObject.<Xs2aCardAccountListHolder>builder()
                                                                       .body(new Xs2aCardAccountListHolder(Collections.singletonList(accountDetails), aisConsent))
                                                                       .build();
        aspect.getCardAccountList(responseObject);
        verify(cardAccountAspectService).getCardAccountList(responseObject);
    }

    @Test
    void getCardAccountDetails() {
        ResponseObject<Xs2aCardAccountDetailsHolder> responseObject = ResponseObject.<Xs2aCardAccountDetailsHolder>builder()
                                                                          .body(new Xs2aCardAccountDetailsHolder(accountDetails, aisConsent))
                                                                          .build();
        aspect.getCardAccountDetails(responseObject);
        verify(cardAccountAspectService).getCardAccountDetails(responseObject);
    }

    @Test
    void getTransactionsReportByPeriod() {
        ResponseObject<Xs2aCardTransactionsReport> responseObject = ResponseObject.<Xs2aCardTransactionsReport>builder()
                                                                        .body(new Xs2aCardTransactionsReport())
                                                                        .build();
        aspect.getTransactionsReportByPeriod(responseObject, request);
        verify(cardAccountAspectService).getTransactionsReportByPeriod(responseObject, request);
    }
}
