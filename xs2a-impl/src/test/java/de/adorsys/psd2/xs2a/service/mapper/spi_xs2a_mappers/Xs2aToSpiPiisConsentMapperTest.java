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

package de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers;

import de.adorsys.psd2.core.data.piis.v1.PiisConsent;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.domain.consent.ConsentAuthorisationsParameters;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiScaConfirmation;
import de.adorsys.psd2.xs2a.spi.domain.piis.SpiPiisConsent;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {Xs2aToSpiPiisConsentMapperImpl.class, Xs2aToSpiPsuDataMapper.class})
class Xs2aToSpiPiisConsentMapperTest {
    private final JsonReader jsonReader = new JsonReader();

    @Autowired
    private Xs2aToSpiPiisConsentMapper xs2aToSpiPiisConsentMapper;

    private static Stream<Arguments> params() {
        String piisConsentNullTppPath = "json/service/mapper/spi_xs2a_mappers/piis/piis-consent-null-tpp-info-auth-number.json";
        String spiPiisConsentNullTppPath = "json/service/mapper/spi_xs2a_mappers/piis/spi-piis-consent-null-tpp-auth-number-expected.json";
        String piisConsentPath = "json/service/mapper/spi_xs2a_mappers/piis/piis-consent.json";
        String spiPiisConsentPath = "json/service/mapper/spi_xs2a_mappers/piis/spi-piis-consent.json";
        String piisConsentNullConsentDataPath = "json/service/mapper/spi_xs2a_mappers/piis/piis-consent-null-consent-data-and-tpp-info.json";
        String spiPiisConsentNullConsentDataPath = "json/service/mapper/spi_xs2a_mappers/piis/spi-piis-consent-no-card-info-expected.json";
        String piisConsentEmptyConsentData = "json/service/mapper/spi_xs2a_mappers/piis/piis-consent-empty-consent-data.json";
        String spiPiisConsentEmptyConsentData = "json/service/mapper/spi_xs2a_mappers/piis/spi-piis-consent-expected.json";

        return Stream.of(Arguments.arguments(piisConsentNullTppPath, spiPiisConsentNullTppPath),
            Arguments.arguments(piisConsentPath, spiPiisConsentPath),
            Arguments.arguments(piisConsentNullConsentDataPath, spiPiisConsentNullConsentDataPath),
            Arguments.arguments(piisConsentEmptyConsentData, spiPiisConsentEmptyConsentData)
        );
    }

    @ParameterizedTest
    @MethodSource("params")
    void mapToSpiPiisConsent(String xs2aConsentPath, String expectedPath) {
        //Given
        PiisConsent xs2aConsent = jsonReader.getObjectFromFile(xs2aConsentPath, PiisConsent.class);
        SpiPiisConsent expected = jsonReader.getObjectFromFile(expectedPath, SpiPiisConsent.class);

        //When
        SpiPiisConsent actual = xs2aToSpiPiisConsentMapper.mapToSpiPiisConsent(xs2aConsent);

        //Then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void mapToSpiPiisConsent_null() {
        //When
        SpiPiisConsent actual = xs2aToSpiPiisConsentMapper.mapToSpiPiisConsent(null);

        //Then
        assertThat(actual).isNull();
    }

    @Test
    void toSpiScaConfirmation() {
        //Given
        ConsentAuthorisationsParameters updateAuthorisationRequest =
            jsonReader.getObjectFromFile("json/service/mapper/consent/update-consent-psu-data-req.json",
                ConsentAuthorisationsParameters.class);
        PsuIdData psuIdData =
            jsonReader.getObjectFromFile("json/service/mapper/psu-id-data.json",
                PsuIdData.class);
        SpiScaConfirmation expected =
            jsonReader.getObjectFromFile("json/service/mapper/spi-sca-confirmation.json",
                SpiScaConfirmation.class);

        //When
        SpiScaConfirmation actual = xs2aToSpiPiisConsentMapper.toSpiScaConfirmation(updateAuthorisationRequest, psuIdData);

        //Then
        assertThat(actual).isEqualTo(expected);
    }
}
