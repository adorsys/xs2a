/*
 * Copyright 2018-2019 adorsys GmbH & Co KG
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

package de.adorsys.psd2.consent.service.mapper;

import de.adorsys.psd2.consent.domain.PsuData;
import de.adorsys.psd2.consent.domain.TppInfoEntity;
import de.adorsys.psd2.consent.domain.account.AisConsent;
import de.adorsys.psd2.consent.domain.account.AisConsentAuthorization;
import de.adorsys.psd2.consent.domain.payment.PisAuthorization;
import de.adorsys.psd2.consent.domain.payment.PisCommonPaymentData;
import de.adorsys.psd2.consent.psu.api.CmsPsuAuthorisation;
import de.adorsys.psd2.consent.reader.JsonReader;
import de.adorsys.psd2.xs2a.core.pis.PaymentAuthorisationType;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.OffsetDateTime;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {CmsPsuAuthorisationMapperImpl.class})
public class CmsPsuAuthorisationMapperTest {

    private static final String EXTERNAL_ID = "123";
    private static final String OK_REDIRECT_URI = "OK redirect";
    private static final String NOK_REDIRECT_URI = "Not OK redirect";
    private static final OffsetDateTime EXPIRATION_TIMESTAMP = OffsetDateTime.now().plusDays(1);
    private static final String PSU_ID = "PSU ID";
    private static final String PSU_ID_TYPE = "PSU ID type";

    private CmsPsuAuthorisationMapper cmsPsuAuthorisationMapper = new CmsPsuAuthorisationMapperImpl();

    private JsonReader jsonReader;

    @Before
    public void init() {
        jsonReader = new JsonReader();
    }

    @Test
    public void mapToCmsPsuAuthorisationPis_success() {
        // Given
        PisAuthorization pisAuthorization = buildPisAuthorization();

        // When
        CmsPsuAuthorisation actual = cmsPsuAuthorisationMapper.mapToCmsPsuAuthorisationPis(pisAuthorization);

        // Then
        CmsPsuAuthorisation expected = jsonReader.getObjectFromFile("json/service/mapper/cms-psu-authorisation.json", CmsPsuAuthorisation.class);
        expected.setAuthorisationExpirationTimestamp(EXPIRATION_TIMESTAMP);

        assertEquals(expected, actual);
    }

    @Test
    public void mapToCmsPsuAuthorisationAis_success() {
        // Given
        AisConsentAuthorization aisConsentAuthorization = buildAisConsentAuthorization();

        // When
        CmsPsuAuthorisation actual = cmsPsuAuthorisationMapper.mapToCmsPsuAuthorisationAis(aisConsentAuthorization);

        // Then
        CmsPsuAuthorisation expected = jsonReader.getObjectFromFile("json/service/mapper/cms-psu-authorisation.json", CmsPsuAuthorisation.class);
        expected.setAuthorisationType(null);
        expected.setAuthorisationExpirationTimestamp(EXPIRATION_TIMESTAMP);
        assertEquals(expected, actual);
    }

    @NotNull
    private PisAuthorization buildPisAuthorization() {
        PisAuthorization pisAuthorization = new PisAuthorization();
        pisAuthorization.setExternalId(EXTERNAL_ID);
        pisAuthorization.setScaStatus(ScaStatus.RECEIVED);
        pisAuthorization.setPsuData(new PsuData(PSU_ID, PSU_ID_TYPE, "", ""));
        pisAuthorization.setAuthorizationType(PaymentAuthorisationType.CREATED);
        pisAuthorization.setAuthorisationExpirationTimestamp(EXPIRATION_TIMESTAMP);
        pisAuthorization.setScaApproach(ScaApproach.EMBEDDED);
        pisAuthorization.setTppOkRedirectUri(OK_REDIRECT_URI);
        pisAuthorization.setTppNokRedirectUri(NOK_REDIRECT_URI);
        return pisAuthorization;
    }

    @NotNull
    private AisConsentAuthorization buildAisConsentAuthorization() {
        AisConsentAuthorization authorization = new AisConsentAuthorization();
        authorization.setExternalId(EXTERNAL_ID);
        authorization.setScaStatus(ScaStatus.RECEIVED);
        authorization.setPsuData(new PsuData("PSU ID", "PSU ID type", "", ""));
        authorization.setAuthorisationExpirationTimestamp(EXPIRATION_TIMESTAMP);
        authorization.setScaApproach(ScaApproach.EMBEDDED);
        authorization.setTppOkRedirectUri(OK_REDIRECT_URI);
        authorization.setTppNokRedirectUri(NOK_REDIRECT_URI);
        return authorization;
    }
}
