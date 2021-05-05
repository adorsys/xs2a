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

package de.adorsys.psd2.xs2a.web.mapper;

import de.adorsys.psd2.core.data.piis.v1.PiisConsent;
import de.adorsys.psd2.model.*;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.domain.HrefType;
import de.adorsys.psd2.xs2a.domain.Links;
import de.adorsys.psd2.xs2a.domain.consent.ConsentStatusResponse;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aConfirmationOfFundsResponse;
import de.adorsys.psd2.xs2a.domain.fund.CreatePiisConsentRequest;
import de.adorsys.psd2.xs2a.service.mapper.AccountModelMapper;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PiisConsentModelMapperTest {
    private final static String SELF_LINK = "self";
    private final static String LOCALHOST_LINK = "http://localhost";
    private final static String CONSENT_STATUS = "received";
    private final static String CONSENT_ID = "S7tlYXaar8j7l5IMK89iNJB8SkG5ricoOaEYHyku_AO9BF6MIP29SN_tXtDvaQb3c8b_NsohCWlFlYN0ds8u89WFnjze07vwpAgFM45MlQk=_=_psGLvQpt9Q";
    private final static String PSU_MESSAGE = "This test message is created in ASPSP and directed to PSU";

    @InjectMocks
    private PiisConsentModelMapper piisConsentModelMapper;

    @Mock
    private HrefLinkMapper hrefLinkMapper;
    @Mock
    private AccountModelMapper accountModelMapper;
    @Mock
    private ConsentModelMapper consentModelMapper;

    private Xs2aConfirmationOfFundsResponse xs2aConfirmationOfFundsResponse;
    private JsonReader jsonReader;

    @BeforeEach
    void setUp() {
        jsonReader = new JsonReader();
        xs2aConfirmationOfFundsResponse = jsonReader.getObjectFromFile("json/service/mapper/piis-consent-model-mapper/xs2a-confirmation-of-funds-response.json", Xs2aConfirmationOfFundsResponse.class);
    }

    @Test
    void mapToConsentsConfirmationOfFundsResponse() {
        //Given
        when(hrefLinkMapper.mapToLinksMap(any(Links.class))).thenReturn(buildLinks());
        //When
        ConsentsConfirmationOfFundsResponse actual = piisConsentModelMapper.mapToConsentsConfirmationOfFundsResponse(xs2aConfirmationOfFundsResponse);
        //Then
        checkCommonFields(actual);
    }

    @Test
    void mapToConsentConfirmationOfFundsContentResponse() {
        PiisConsent piisConsent = jsonReader.getObjectFromFile("json/service/mapper/consent/piis/piis-consent.json", PiisConsent.class);

        de.adorsys.psd2.model.AccountReference accountReference = jsonReader.getObjectFromFile("json/service/mapper/account-reference.json", AccountReference.class);

        when(accountModelMapper.mapToAccountReference(piisConsent.getAccountReference())).thenReturn((accountReference));

        ConsentConfirmationOfFundsContentResponse actual = piisConsentModelMapper.mapToConsentConfirmationOfFundsContentResponse(piisConsent);

        ConsentConfirmationOfFundsContentResponse expected =
            jsonReader.getObjectFromFile("json/service/mapper/consent/piis/confrimation-of-funds-content-response.json", ConsentConfirmationOfFundsContentResponse.class);

        assertEquals(expected, actual);
    }

    @Test
    void toCreatePiisConsentRequest_null() {
        CreatePiisConsentRequest actual = piisConsentModelMapper.toCreatePiisConsentRequest(null);

        assertNull(actual);
    }

    @Test
    void toCreatePiisConsentRequest_Ok() {
        ConsentsConfirmationOfFunds consentsConfirmationOfFunds = jsonReader.getObjectFromFile("json/piis/create-piis-consent.json", ConsentsConfirmationOfFunds.class);

        CreatePiisConsentRequest actual = piisConsentModelMapper.toCreatePiisConsentRequest(consentsConfirmationOfFunds);

        CreatePiisConsentRequest expected = jsonReader.getObjectFromFile("json/service/mapper/piis-consent-model-mapper/piis-consent-request.json", CreatePiisConsentRequest.class);

        assertNotNull(actual);
        assertEquals(expected, actual);
    }

    @Test
    void mapToConsentConfirmationOfFundsStatusResponse() {
        ConsentStatusResponse consentStatusResponse = new ConsentStatusResponse(ConsentStatus.VALID, "message");

        ConsentConfirmationOfFundsStatusResponse actual = piisConsentModelMapper.mapToConsentConfirmationOfFundsStatusResponse(consentStatusResponse);

        ConsentConfirmationOfFundsStatusResponse expected = jsonReader.getObjectFromFile("json/service/mapper/consent/piis/confrimation-of-funds-status-response.json", ConsentConfirmationOfFundsStatusResponse.class);

        assertEquals(expected, actual);
    }

    private void checkCommonFields(ConsentsConfirmationOfFundsResponse actual) {
        assertNotNull(actual);
        assertEquals(CONSENT_STATUS, actual.getConsentStatus().toString());
        assertEquals(CONSENT_ID, actual.getConsentId());
        assertEquals(PSU_MESSAGE, actual.getPsuMessage());
        assertFalse(actual.getLinks().isEmpty());

        assertNotNull(actual.getLinks().get(SELF_LINK));
        HrefType selfMap = (HrefType) actual.getLinks().get(SELF_LINK);
        assertEquals(LOCALHOST_LINK, selfMap.getHref());
    }

    private Map<String, HrefType> buildLinks() {
        return Collections.singletonMap(SELF_LINK, new HrefType(LOCALHOST_LINK));
    }
}
