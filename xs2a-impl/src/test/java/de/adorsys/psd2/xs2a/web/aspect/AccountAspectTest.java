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

import de.adorsys.psd2.core.data.AccountAccess;
import de.adorsys.psd2.core.data.ais.AisConsent;
import de.adorsys.psd2.core.data.ais.AisConsentData;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.account.Xs2aAccountDetails;
import de.adorsys.psd2.xs2a.domain.account.Xs2aAccountDetailsHolder;
import de.adorsys.psd2.xs2a.domain.account.Xs2aAccountListHolder;
import de.adorsys.psd2.xs2a.service.link.AccountAspectService;
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
class AccountAspectTest {
    private static final String CONSENT_ID = "c966f143-f6a2-41db-9036-8abaeeef3af7";
    private static final String ACCOUNT_ID = "123-DEDE89370400440532013000-EUR";
    private static final String REQUEST_URI = "/v1/accounts";

    @InjectMocks
    private AccountAspect aspect;

    @Mock
    private AccountAspectService accountAspectService;

    private Xs2aAccountDetails accountDetails;
    private AisConsent aisConsent;
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
    void getAccountDetailsAspect() {
        ResponseObject<Xs2aAccountDetailsHolder> responseObject = ResponseObject.<Xs2aAccountDetailsHolder>builder()
                                            .body(new Xs2aAccountDetailsHolder(accountDetails, aisConsent))
                                            .build();

        aspect.getAccountDetailsAspect(responseObject, CONSENT_ID, ACCOUNT_ID, true, REQUEST_URI);
        verify(accountAspectService).getAccountDetailsAspect(responseObject);
    }

    @Test
    void getAccountDetailsListAspect() {
        ResponseObject<Xs2aAccountListHolder> responseObject = ResponseObject.<Xs2aAccountListHolder>builder()
                                            .body(new Xs2aAccountListHolder(Collections.singletonList(accountDetails), aisConsent))
                                            .build();

        aspect.getAccountDetailsListAspect(responseObject, CONSENT_ID, true, REQUEST_URI);
        verify(accountAspectService).getAccountDetailsListAspect(responseObject);
    }
}
