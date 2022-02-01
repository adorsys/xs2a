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

package de.adorsys.psd2.xs2a.service;

import de.adorsys.psd2.xs2a.domain.consent.CreateConsentReq;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdditionalInformationSupportedServiceTest {
    @InjectMocks
    private AdditionalInformationSupportedService service;
    @Mock
    private AspspProfileServiceWrapper aspspProfileService;

    private final JsonReader jsonReader = new JsonReader();


    @Test
    void checkSupportedAccountTrustedBeneficiariesInformation_supported() {
        // Given
        CreateConsentReq input =
            jsonReader.getObjectFromFile("json/service/create-consent-req-with-beneficiaries.json",
                CreateConsentReq.class);
        when(aspspProfileService.isTrustedBeneficiariesSupported()).thenReturn(true);

        // When
        CreateConsentReq actual = service.checkIfAdditionalInformationSupported(input);

        // Then
        assertThat(actual).isEqualTo(input);
    }

    @Test
    void checkSupportedAccountTrustedBeneficiariesInformation_notSupportedAndPresent() {
        // Given
        CreateConsentReq input =
            jsonReader.getObjectFromFile("json/service/create-consent-req-with-beneficiaries.json",
                CreateConsentReq.class);
        CreateConsentReq expected =
            jsonReader.getObjectFromFile("json/service/create-consent-req.json", CreateConsentReq.class);
        when(aspspProfileService.isTrustedBeneficiariesSupported()).thenReturn(false);

        // When
        CreateConsentReq actual = service.checkIfAdditionalInformationSupported(input);

        // Then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void checkSupportedAccountTrustedBeneficiariesInformation_notSupportedAndNotPresent() {
        // Given
        CreateConsentReq input =
            jsonReader.getObjectFromFile("json/service/create-consent-req.json", CreateConsentReq.class);
        when(aspspProfileService.isTrustedBeneficiariesSupported()).thenReturn(false);

        // When
        CreateConsentReq actual = service.checkIfAdditionalInformationSupported(input);

        // Then
        assertThat(actual).isEqualTo(input);
    }

    private static Stream<Arguments> params() {
        String createConsentReqPath = "json/service/create-consent-req.json";
        String createConsentReqOwnerNamePath = "json/service/create-consent-req-with-owner-name.json";
        String createConsentReqAvailAccountsPath = "json/service/create-consent-req-with-available-accounts.json";
        String createConsentReqAvailAccountsOwnerNamePath = "json/service/create-consent-req-with-available-accounts-owner-name.json";
        String createConsentReqBalancePath = "json/service/create-consent-req-with-balance.json";
        String createConsentReqBalanceOwnerNamePath = "json/service/create-consent-req-with-available-accounts-with-balance-owner-name.json";
        String createConsentReqAllPsd2Path = "json/service/create-consent-req-with-all-psd2.json";
        String createConsentReqAllPsd2OwnerNamePath = "json/service/create-consent-req-with-all-psd2-owner-name.json";

        return Stream.of(Arguments.arguments(createConsentReqPath, createConsentReqOwnerNamePath),
                         Arguments.arguments(createConsentReqAvailAccountsPath, createConsentReqAvailAccountsOwnerNamePath),
                         Arguments.arguments(createConsentReqBalancePath, createConsentReqBalanceOwnerNamePath),
                         Arguments.arguments(createConsentReqAllPsd2Path, createConsentReqAllPsd2OwnerNamePath)
        );
    }

    @ParameterizedTest
    @MethodSource("params")
    void checkSupportedAccountOwnerInformation(String expectedPath, String inputPath) {
        // Given
        CreateConsentReq expected = jsonReader.getObjectFromFile(expectedPath, CreateConsentReq.class);
        when(aspspProfileService.isAccountOwnerInformationSupported()).thenReturn(false);

        CreateConsentReq inputData = jsonReader.getObjectFromFile(inputPath, CreateConsentReq.class);

        // When
        CreateConsentReq actual = service.checkIfAdditionalInformationSupported(inputData);

        // Then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void checkSupportedAccountOwnerInformation_ownerNameSupported() {
        // Given
        when(aspspProfileService.isAccountOwnerInformationSupported()).thenReturn(true);

        CreateConsentReq inputData = jsonReader.getObjectFromFile("json/service/create-consent-req-with-owner-name.json", CreateConsentReq.class);

        // When
        CreateConsentReq actual = service.checkIfAdditionalInformationSupported(inputData);

        // Then
        assertThat(actual).isEqualTo(inputData);
    }
}
