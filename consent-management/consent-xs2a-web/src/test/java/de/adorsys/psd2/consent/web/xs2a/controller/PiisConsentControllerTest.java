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
import org.springframework.validation.Validator;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collections;
import java.util.Currency;
import java.util.List;

import static org.mockito.Mockito.mock;
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
                      .setValidator(mock(Validator.class))
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
