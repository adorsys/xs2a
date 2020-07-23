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

package de.adorsys.psd2.consent.repository.specification;

import de.adorsys.psd2.consent.domain.TppInfoEntity;
import de.adorsys.psd2.consent.domain.consent.ConsentEntity;
import de.adorsys.psd2.consent.repository.ConsentJpaRepository;
import de.adorsys.psd2.consent.repository.TppInfoRepository;
import de.adorsys.psd2.integration.test.BaseTest;
import de.adorsys.psd2.integration.test.TestDBConfiguration;
import de.adorsys.psd2.xs2a.core.consent.ConsentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ContextConfiguration(classes = TestDBConfiguration.class,
    initializers = {ConfirmationOfFundsConsentSpecificationIT.Initializer.class})
class ConfirmationOfFundsConsentSpecificationIT extends BaseTest {

    private static final String INSTANCE_ID = "UNDEFINED";

    @Autowired
    private ConfirmationOfFundsConsentSpecification confirmationOfFundsConsentSpecification;

    @Autowired
    private ConsentJpaRepository consentJpaRepository;
    @Autowired
    private TppInfoRepository tppInfoRepository;

    private TppInfoEntity tppInfo;
    private ConsentEntity piisAspspConsent;
    private ConsentEntity piisTppConsent;

    @BeforeEach
    void setUp() {
        clearData();

        tppInfo = tppInfoRepository.save(
            jsonReader.getObjectFromFile("json/specification/tpp-info-entity.json", TppInfoEntity.class));

        ConsentEntity consentEntity = jsonReader.getObjectFromFile("json/specification/consent-entity.json", ConsentEntity.class);
        consentEntity.getTppInformation().setTppInfo(tppInfo);

        consentEntity.setId(1L);
        consentEntity.setExternalId(UUID.randomUUID().toString());
        consentEntity.setConsentType(ConsentType.PIIS_ASPSP.getName());
        piisAspspConsent = consentJpaRepository.save(consentEntity);

        consentEntity.setId(2L);
        consentEntity.setExternalId(UUID.randomUUID().toString());
        consentEntity.setConsentType(ConsentType.PIIS_TPP.getName());
        piisTppConsent = consentJpaRepository.save(consentEntity);
    }

    @Test
    void byConsentIdAndInstanceId() {
        //piis aspsp consent
        Optional<ConsentEntity> actual = consentJpaRepository.findOne(
            confirmationOfFundsConsentSpecification.byConsentIdAndInstanceId(piisAspspConsent.getExternalId(), INSTANCE_ID));

        assertTrue(actual.isPresent());
        assertEquals(piisAspspConsent.getExternalId(), actual.get().getExternalId());
        assertEquals(INSTANCE_ID, actual.get().getInstanceId());
        assertEquals(ConsentType.PIIS_ASPSP.name(), actual.get().getConsentType());

        //piis tpp consent
        actual = consentJpaRepository.findOne(
            confirmationOfFundsConsentSpecification.byConsentIdAndInstanceId(piisTppConsent.getExternalId(), INSTANCE_ID));

        assertTrue(actual.isPresent());
        assertEquals(piisTppConsent.getExternalId(), actual.get().getExternalId());
        assertEquals(INSTANCE_ID, actual.get().getInstanceId());
        assertEquals(ConsentType.PIIS_TPP.name(), actual.get().getConsentType());
    }
}
