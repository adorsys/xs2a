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

package de.adorsys.psd2.xs2a.service.mapper.cms_xs2a_mappers;

import de.adorsys.psd2.consent.api.ais.CmsConsent;
import de.adorsys.psd2.core.data.piis.v1.PiisConsent;
import de.adorsys.psd2.core.mapper.ConsentDataMapper;
import de.adorsys.psd2.model.AccountReference;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.domain.fund.CreatePiisConsentRequest;
import de.adorsys.psd2.xs2a.service.RequestProviderService;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.psd2.xs2a.service.validator.ValueValidatorService;
import de.adorsys.psd2.xs2a.web.mapper.ConsentModelMapper;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Collections;
import java.util.Currency;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {Xs2aPiisConsentMapperImpl.class, ConsentDataMapper.class})
class Xs2aPiisConsentMapperTest {
    private JsonReader jsonReader = new JsonReader();

    @Autowired
    private Xs2aPiisConsentMapper xs2aPiisConsentMapper;
    @MockBean
    private AspspProfileServiceWrapper aspspProfileService;
    @MockBean
    private ValueValidatorService valueValidatorService;
    @MockBean
    private RequestProviderService requestProviderService;
    @MockBean
    private ConsentModelMapper consentModelMapper;


    @Test
    void mapToPiisConsent() {
        CmsConsent cmsConsent = jsonReader.getObjectFromFile("json/service/mapper/consent/piis/cms-consent.json", CmsConsent.class);
        byte[] piisConsentData = jsonReader.getBytesFromFile("json/service/mapper/consent/piis/piis-consent-data.json");
        cmsConsent.setConsentData(piisConsentData);
        PiisConsent expectedXs2aConsent = jsonReader.getObjectFromFile("json/service/mapper/consent/piis/piis-consent.json", PiisConsent.class);

        PiisConsent result = xs2aPiisConsentMapper.mapToPiisConsent(cmsConsent);

        assertEquals(expectedXs2aConsent, result);
    }

    @Test
    void mapToCmsConsent() {
        //Given
        AccountReference accountReference = new AccountReference();
        accountReference.setIban("DE15500105172295759744");
        accountReference.setCurrency("EUR");
        de.adorsys.psd2.xs2a.core.profile.AccountReference reference = new de.adorsys.psd2.xs2a.core.profile.AccountReference(null, null, accountReference.getIban(), null, null, null, null, Currency.getInstance(accountReference.getCurrency()), null);
        when(consentModelMapper.mapToXs2aAccountReferences(Collections.singletonList(accountReference)))
            .thenReturn(Collections.singletonList(reference));
        PsuIdData psuIdData = jsonReader.getObjectFromFile("json/service/mapper/psu-id-data.json", PsuIdData.class);

        int allowedFrequencyPerDay = 0;
        //When

        CreatePiisConsentRequest request = jsonReader.getObjectFromFile("json/piis/create-piis-consent.json", CreatePiisConsentRequest.class);
        TppInfo tppInfo = jsonReader.getObjectFromFile("json/service/mapper/tpp-info.json", TppInfo.class);
        CmsConsent cmsConsent = xs2aPiisConsentMapper.mapToCmsConsent(request, psuIdData, tppInfo, allowedFrequencyPerDay);
        //Then
        CmsConsent cmsConsentExpected = jsonReader.getObjectFromFile("json/service/mapper/consent/piis/cms-consent-creation.json", CmsConsent.class);
        assertEquals(cmsConsentExpected, cmsConsent);
    }
}
