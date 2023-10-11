/*
 * Copyright 2018-2023 adorsys GmbH & Co KG
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
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ActiveProfiles("integration-test")
@EnableJpaRepositories("de.adorsys.psd2.consent.repository")
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

        ConsentEntity piisAspspConsent = jsonReader.getObjectFromFile("json/specification/consent-entity.json", ConsentEntity.class);
        piisAspspConsent.getTppInformation().setTppInfo(tppInfo);

        piisAspspConsent.setId(1L);
        piisAspspConsent.setExternalId(UUID.randomUUID().toString());
        piisAspspConsent.setConsentType(ConsentType.PIIS_ASPSP.getName());
        piisAspspConsent.getAspspAccountAccesses().get(0).setConsent(piisAspspConsent);

        this.piisAspspConsent = consentJpaRepository.save(piisAspspConsent);

        ConsentEntity piisTppConsent = jsonReader.getObjectFromFile("json/specification/consent-entity.json", ConsentEntity.class);
        piisTppConsent.getTppInformation().setTppInfo(tppInfo);

        piisTppConsent.setId(2L);
        piisTppConsent.setExternalId(UUID.randomUUID().toString());
        piisTppConsent.setConsentType(ConsentType.PIIS_TPP.getName());
        piisTppConsent.getAspspAccountAccesses().get(0).setConsent(piisTppConsent);
        this.piisTppConsent = consentJpaRepository.save(piisTppConsent);
    }

    @Test
    void byConsentIdAndInstanceId() {
        // PIIS ASPSP consent
        Optional<ConsentEntity> actual = consentJpaRepository.findOne(
            confirmationOfFundsConsentSpecification.byConsentIdAndInstanceId(piisAspspConsent.getExternalId(), INSTANCE_ID));

        assertTrue(actual.isPresent());
        assertEquals(piisAspspConsent.getExternalId(), actual.get().getExternalId());
        assertEquals(INSTANCE_ID, actual.get().getInstanceId());
        assertEquals(ConsentType.PIIS_ASPSP.name(), actual.get().getConsentType());

        // PIIS TPP consent
        actual = consentJpaRepository.findOne(
            confirmationOfFundsConsentSpecification.byConsentIdAndInstanceId(piisTppConsent.getExternalId(), INSTANCE_ID));

        assertTrue(actual.isPresent());
        assertEquals(piisTppConsent.getExternalId(), actual.get().getExternalId());
        assertEquals(INSTANCE_ID, actual.get().getInstanceId());
        assertEquals(ConsentType.PIIS_TPP.name(), actual.get().getConsentType());
    }
}
