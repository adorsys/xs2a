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


package de.adorsys.psd2.consent.web.xs2a.controller;

import de.adorsys.psd2.consent.api.CmsResponse;
import de.adorsys.psd2.consent.api.ais.CmsConsent;
import de.adorsys.psd2.consent.api.service.PiisConsentService;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.profile.AccountReferenceSelector;
import de.adorsys.psd2.xs2a.core.profile.AccountReferenceType;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collections;
import java.util.Currency;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PiisConsentControllerTest {
    private static final Currency CURRENCY = Currency.getInstance("EUR");
    private static final String IBAN = "DE62500105179972514662";
    private static final String WRONG_IBAN = "FR7030066926176517166656113";

    @Mock
    private PiisConsentService piisConsentService;
    @InjectMocks
    private PiisConsentController piisConsentController;

    private MockMvc mockMvc;
    private JsonReader jsonReader = new JsonReader();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(piisConsentController)
                      .setMessageConverters(new MappingJackson2HttpMessageConverter())
                      .build();
    }

    @Test
    void getPiisConsentById() throws Exception {
        when(piisConsentService.getPiisConsentListByAccountIdentifier(CURRENCY, new AccountReferenceSelector(AccountReferenceType.IBAN, IBAN)))
            .thenReturn(CmsResponse.<List<CmsConsent>>builder().payload(Collections.singletonList(buildPiisConsent())).build());

        mockMvc.perform(MockMvcRequestBuilders.get(UriComponentsBuilder.fromPath("/api/v1/piis/consent/{account-reference-type}/{account-reference}")
                                                       .buildAndExpand(AccountReferenceType.IBAN, IBAN)
                                                       .toUriString())
                            .header("currency", CURRENCY))
            .andExpect(status().is(HttpStatus.OK.value()))
            .andExpect(content().json(jsonReader.getStringFromFile("json/controller/piis/cms-consent.json")));
    }

    @Test
    void getPiisConsentById_noCurrency() throws Exception {
        when(piisConsentService.getPiisConsentListByAccountIdentifier(null, new AccountReferenceSelector(AccountReferenceType.IBAN, IBAN)))
            .thenReturn(CmsResponse.<List<CmsConsent>>builder().payload(Collections.singletonList(buildPiisConsent())).build());

        mockMvc.perform(MockMvcRequestBuilders.get(UriComponentsBuilder.fromPath("/api/v1/piis/consent/{account-reference-type}/{account-reference}")
                                                       .buildAndExpand(AccountReferenceType.IBAN, IBAN)
                                                       .toUriString()))
            .andExpect(status().is(HttpStatus.OK.value()))
            .andExpect(content().json(jsonReader.getStringFromFile("json/controller/piis/cms-consent.json")));
    }

    @Test
    void getPiisConsentById_Failure_WrongConsentId() throws Exception {
        when(piisConsentService.getPiisConsentListByAccountIdentifier(CURRENCY, new AccountReferenceSelector(AccountReferenceType.IBAN, WRONG_IBAN)))
            .thenReturn(CmsResponse.<List<CmsConsent>>builder().payload(Collections.emptyList()).build());

        mockMvc.perform(MockMvcRequestBuilders.get(UriComponentsBuilder.fromPath("/api/v1/piis/consent/{account-reference-type}/{account-reference}")
                                                       .buildAndExpand(AccountReferenceType.IBAN, WRONG_IBAN)
                                                       .toUriString())
                            .header("currency", CURRENCY))
            .andExpect(status().is(HttpStatus.NOT_FOUND.value()))
            .andExpect(content().bytes(new byte[0]));
    }

    private CmsConsent buildPiisConsent() {
        CmsConsent piisConsent = new CmsConsent();
        piisConsent.setConsentStatus(ConsentStatus.VALID);
        return piisConsent;
    }
}
