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

package de.adorsys.psd2.consent.service.mapper;

import de.adorsys.psd2.consent.api.ais.AisAccountAccess;
import de.adorsys.psd2.consent.api.ais.CmsAisAccountConsent;
import de.adorsys.psd2.consent.domain.AuthorisationEntity;
import de.adorsys.psd2.consent.domain.consent.ConsentEntity;
import de.adorsys.psd2.consent.service.AisConsentUsageService;
import de.adorsys.psd2.core.data.AccountAccess;
import de.adorsys.psd2.core.data.ais.AisConsent;
import de.adorsys.psd2.core.mapper.ConsentDataMapper;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(value = {SpringExtension.class, MockitoExtension.class})
@ContextConfiguration(classes = {
    TppInfoMapperImpl.class,
    AuthorisationTemplateMapperImpl.class,
    PsuDataMapper.class,
    ConsentDataMapper.class,
    ConsentTppInformationMapperImpl.class,
    AccessMapper.class
})
class AisConsentMapperTest {
    private static final Map<String, Integer> USAGE_COUNTER = Collections.singletonMap("/accounts", 9);

    @Autowired
    private AuthorisationTemplateMapper authorisationTemplateMapper;
    @Autowired
    private PsuDataMapper psuDataMapper;
    @Autowired
    private TppInfoMapper tppInfoMapper;
    @Mock
    private AisConsentUsageService aisConsentUsageService;
    @Autowired
    private ConsentDataMapper consentDataMapper;
    @Autowired
    private ConsentTppInformationMapper consentTppInformationMapper;
    @Autowired
    private AccessMapper accessMapper;

    private static final JsonReader jsonReader = new JsonReader();

    private AisConsentMapper aisConsentMapper;

    private static ConsentEntity consentGlobal;
    private static CmsAisAccountConsent aisConsentGlobal;
    private static ConsentEntity consentAvailableAccount;
    private static CmsAisAccountConsent aisConsentAvailableAccount;
    private static ConsentEntity consent;
    private static CmsAisAccountConsent aisConsent;

    @BeforeAll
    static void beforeAll() {
        consentGlobal = jsonReader.getObjectFromFile("json/service/mapper/ais-consent-mapper/consent-entity-global-account-access.json",
                                                     ConsentEntity.class);
        aisConsentGlobal = jsonReader.getObjectFromFile("json/service/mapper/ais-consent-mapper/cms-ais-account-consent-global-account.json",
                                                        CmsAisAccountConsent.class);
        consentAvailableAccount = jsonReader.getObjectFromFile("json/service/mapper/ais-consent-mapper/consent-entity-all-available-account-with-balance.json",
                                                               ConsentEntity.class);
        aisConsentAvailableAccount = jsonReader.getObjectFromFile("json/service/mapper/ais-consent-mapper/cms-ais-account-consent-available-account-with-balance.json",
                                                                  CmsAisAccountConsent.class);
        consent = jsonReader.getObjectFromFile("json/service/mapper/ais-consent-mapper/consent-entity.json",
                                               ConsentEntity.class);
        aisConsent = jsonReader.getObjectFromFile("json/service/mapper/ais-consent-mapper/cms-ais-account-consent.json",
                                                  CmsAisAccountConsent.class);
    }

    @BeforeEach
    void setUp() {
        aisConsentMapper = new AisConsentMapper(psuDataMapper,
                                                tppInfoMapper,
                                                aisConsentUsageService,
                                                authorisationTemplateMapper,
                                                consentDataMapper,
                                                consentTppInformationMapper,
                                                accessMapper);
    }

    @Test
    void mapToCmsAisAccountConsent_emptyAuthorisations() {
        ConsentEntity consent = jsonReader
                                    .getObjectFromFile("json/service/mapper/ais-consent-mapper/consent-entity-empty-authorisations.json",
                                                       ConsentEntity.class);

        List<AuthorisationEntity> authorisations = Collections.emptyList();

        CmsAisAccountConsent result = aisConsentMapper.mapToCmsAisAccountConsent(consent, authorisations);

        CmsAisAccountConsent expected = jsonReader
                                            .getObjectFromFile("json/service/mapper/ais-consent-mapper/cms-ais-account-consent-empty-authorisations.json",
                                                               CmsAisAccountConsent.class);

        assertEquals(expected, result);
    }

    @ParameterizedTest
    @MethodSource("consents")
    void mapToCmsAisAccountConsent(ConsentEntity input, CmsAisAccountConsent expected) {
        List<AuthorisationEntity> authorisations = Collections.singletonList(buildAisConsentAuthorisation());

        when(aisConsentUsageService.getUsageCounterMap(input)).thenReturn(USAGE_COUNTER);
        CmsAisAccountConsent actual = aisConsentMapper.mapToCmsAisAccountConsent(input, authorisations);

        assertEquals(expected, actual);
    }

    private static Stream<Arguments> consents() {
        return Stream.of(Arguments.arguments(consentGlobal, aisConsentGlobal),
                         Arguments.arguments(consentAvailableAccount, aisConsentAvailableAccount),
                         Arguments.arguments(consent, aisConsent)
        );
    }

    @Test
    void mapToAccountAccess() {
        AisAccountAccess aisAccountAccess = jsonReader
                                                .getObjectFromFile("json/service/mapper/ais-consent-mapper/ais-account-access.json",
                                                                   AisAccountAccess.class);

        AccountAccess result = aisConsentMapper.mapToAccountAccess(aisAccountAccess);

        AccountAccess expected = jsonReader.
                                               getObjectFromFile("json/service/mapper/ais-consent-mapper/aspsp-account-access.json",
                                                                 AccountAccess.class);

        assertEquals(expected, result);
    }

    @Test
    void mapToAisConsent_emptyAuthorisations() {
        ConsentEntity consent = jsonReader
                                    .getObjectFromFile("json/service/mapper/ais-consent-mapper/consent-entity-tpp-aspsp-access.json",
                                                       ConsentEntity.class);

        List<AuthorisationEntity> authorisations = Collections.emptyList();

        when(aisConsentUsageService.getUsageCounterMap(consent)).thenReturn(USAGE_COUNTER);
        AisConsent result = aisConsentMapper.mapToAisConsent(consent, authorisations);

        AisConsent expected = jsonReader.
                                            getObjectFromFile("json/service/mapper/ais-consent-mapper/ais-consent-empty-authorisations.json",
                                                              AisConsent.class);

        assertEquals(expected, result);
    }

    @Test
    void mapToAisConsent() {
        ConsentEntity consent = jsonReader
                                    .getObjectFromFile("json/service/mapper/ais-consent-mapper/consent-entity-tpp-aspsp-access.json",
                                                       ConsentEntity.class);

        List<AuthorisationEntity> authorisations = Collections.singletonList(buildAisConsentAuthorisation());

        when(aisConsentUsageService.getUsageCounterMap(consent)).thenReturn(USAGE_COUNTER);
        AisConsent result = aisConsentMapper.mapToAisConsent(consent, authorisations);

        AisConsent expected = jsonReader
                                  .getObjectFromFile("json/service/mapper/ais-consent-mapper/ais-consent-with-authorisations.json",
                                                     AisConsent.class);

        assertEquals(expected, result);
    }

    private AuthorisationEntity buildAisConsentAuthorisation() {
        return jsonReader
                   .getObjectFromFile("json/service/mapper/ais-consent-mapper/authorisation-entity.json",
                                      AuthorisationEntity.class);
    }
}
