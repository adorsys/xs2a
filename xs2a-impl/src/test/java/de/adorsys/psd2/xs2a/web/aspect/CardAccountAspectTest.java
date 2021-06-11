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
