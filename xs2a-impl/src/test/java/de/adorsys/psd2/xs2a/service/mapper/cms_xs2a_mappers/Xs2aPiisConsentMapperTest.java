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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {Xs2aPiisConsentMapperImpl.class, ConsentDataMapper.class, Xs2aAccountConsentAuthorizationMapper.class})
class Xs2aPiisConsentMapperTest {
    private final JsonReader jsonReader = new JsonReader();

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
        //Given
        CmsConsent cmsConsent = jsonReader.getObjectFromFile("json/service/mapper/consent/piis/cms-consent.json", CmsConsent.class);
        byte[] piisConsentData = jsonReader.getBytesFromFile("json/service/mapper/consent/piis/piis-consent-data.json");
        cmsConsent.setConsentData(piisConsentData);
        PiisConsent expected = jsonReader.getObjectFromFile("json/service/mapper/consent/piis/piis-consent.json", PiisConsent.class);

        //When
        PiisConsent actual = xs2aPiisConsentMapper.mapToPiisConsent(cmsConsent);

        //Then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void mapToPiisConsent_nullInput() {
        //When
        PiisConsent actual = xs2aPiisConsentMapper.mapToPiisConsent(null);

        //Then
        assertThat(actual).isNull();
    }

    @Test
    void mapToCmsConsent() {
        //Given
        AccountReference accountReference = new AccountReference();
        accountReference.setIban("DE15500105172295759744");
        accountReference.setCurrency("EUR");
        de.adorsys.psd2.xs2a.core.profile.AccountReference reference = new de.adorsys.psd2.xs2a.core.profile.AccountReference(null, null, accountReference.getIban(), null, null, null, null, Currency.getInstance(accountReference.getCurrency()), null);
        PsuIdData psuIdData = jsonReader.getObjectFromFile("json/service/mapper/psu-id-data.json", PsuIdData.class);

        //When
        when(consentModelMapper.mapToXs2aAccountReferences(Collections.singletonList(accountReference)))
            .thenReturn(Collections.singletonList(reference));
        when(requestProviderService.getTppRedirectURI()).thenReturn("test tppRedirectUri");
        CreatePiisConsentRequest request = jsonReader.getObjectFromFile("json/piis/create-piis-consent.json", CreatePiisConsentRequest.class);
        TppInfo tppInfo = jsonReader.getObjectFromFile("json/service/mapper/tpp-info.json", TppInfo.class);
        CmsConsent actual = xs2aPiisConsentMapper.mapToCmsConsent(request, psuIdData, tppInfo);

        //Then
        CmsConsent expected = jsonReader.getObjectFromFile("json/service/mapper/consent/piis/cms-consent-creation.json", CmsConsent.class);
        assertThat(actual).isEqualTo(expected);
    }
}
