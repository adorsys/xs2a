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

package de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers;

import de.adorsys.psd2.core.data.piis.v1.PiisConsent;
import de.adorsys.psd2.xs2a.spi.domain.piis.SpiPiisConsent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface Xs2aToSpiPiisConsentMapper {
    @Mapping(target = "account", expression = "java(piisConsent.getAccountReference())")
    @Mapping(target = "cardExpiryDate", source = "consentData.cardExpiryDate")
    @Mapping(target = "cardInformation", source = "consentData.cardInformation")
    @Mapping(target = "cardNumber", source = "consentData.cardNumber")
    @Mapping(target = "psuData", expression = "java(piisConsent.getPsuIdData())")
    @Mapping(target = "registrationInformation", source = "consentData.registrationInformation")
    @Mapping(target = "requestDateTime", source = "creationTimestamp")
    @Mapping(target = "tppAuthorisationNumber", source = "consentTppInformation.tppInfo.authorisationNumber")
    SpiPiisConsent mapToSpiPiisConsent(PiisConsent piisConsent);
}
